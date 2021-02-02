package com.example.customeprintservice.print;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.customeprintservice.R;
import com.example.customeprintservice.adapter.PrintPreviewAdapter;
import com.example.customeprintservice.jipp.PrintUtils;
import com.example.customeprintservice.room.SelectedFile;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.pdf.PdfRenderer;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static android.os.ParcelFileDescriptor.MODE_READ_ONLY;

public class PrintPreview extends AppCompatActivity {
    private PrintPreviewAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_print_preview);
        Bundle bundle = getIntent().getExtras();

        String filePath = bundle.getString("filePath", "");
        File file = new File(filePath);
        SelectedFile selectedFile = new SelectedFile();
        selectedFile.setFileName(file.getName());
        selectedFile.setFilePath(filePath);
        selectedFile.setFromApi(false);
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        Date date = new Date();
        String strDate = dateFormat.format(date);
        selectedFile.setFileSelectedDate(strDate);
        try {
            renderPageUsingDefaultPdfRendererFile(file);
        } catch (IOException e) {
            e.printStackTrace();
        }


    }


    public void renderPageUsingDefaultPdfRendererFile(File file) throws IOException {
        ParcelFileDescriptor fileDescriptor = ParcelFileDescriptor.open(file, MODE_READ_ONLY);
        PdfRenderer renderer = new PdfRenderer(fileDescriptor);
        final int pageCount = renderer.getPageCount();
        PrintUtils printUtils = new PrintUtils();
        Bitmap pageImage = null;
        int pagePrintCounter = 0;
        int threadSleepInMilliSecs = 3000;
        int timeThreshold = threadSleepInMilliSecs * 40;
        int totalTimeThreadSleep = 0;
        ArrayList<File> files =new ArrayList<File>();
        while (pagePrintCounter < pageCount) {
            String path = "/storage/self/primary/sample" + pagePrintCounter + ".jpg";
            File renderFile = new File(path);

            PdfRenderer.Page page = renderer.openPage(pagePrintCounter);
            Bitmap bitmap = Bitmap.createBitmap(page.getWidth(), page.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            canvas.drawColor(Color.WHITE);
            canvas.drawBitmap(bitmap, 0, 0, null);
            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
            page.close();

            FileOutputStream out = new FileOutputStream(renderFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            Log.v("Saved Image - ", renderFile.getAbsolutePath());
            files.add(renderFile);

            out.flush();
            out.close();
            pagePrintCounter++;
        }

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        mAdapter = new PrintPreviewAdapter(files);
       // LinearLayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false);
      //  recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);
        }
}