package com.example.customeprintservice.print;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.customeprintservice.MainActivity;
import com.example.customeprintservice.R;
import com.example.customeprintservice.adapter.PrintPreviewAdapter;
import com.example.customeprintservice.jipp.PrintRenderUtils;
import com.example.customeprintservice.jipp.PrinterModel;
import com.example.customeprintservice.prefs.LoginPrefs;
import com.example.customeprintservice.room.SelectedFile;
import com.example.customeprintservice.signin.SignInCompany;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.pdf.PdfRenderer;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static android.os.ParcelFileDescriptor.MODE_READ_ONLY;

public class PrintPreview extends AppCompatActivity {
    private PrintPreviewAdapter mAdapter;
    public int noOfCopy=1;
    private RadioGroup radioGroup;
    private RadioButton radioButton;
    public PrinterModel selectedPrinterModel =null;
    public String filePath =null;
    public Context context;
    public static ArrayList<SelectedFile> list = new ArrayList<SelectedFile>();
    public ArrayList<SelectedFile> localDocumentSharedPreflist = new ArrayList<SelectedFile>();
    public ArrayList<PrinterModel>serverSecurePrinterListWithDetailsSharedPreflist= new ArrayList<PrinterModel>();
    public String selectPrinter=null;
    int startPageIndex=0;
    int endPageIndex=0;
    int totalPageCount=0;
    int noOfCopies=1;
    Dialog dialog;
    View v = null;
    private NumberPicker picker1,picker2;
    private String[] pickerVals,pickerVals2;
    Logger logger = LoggerFactory.getLogger(PrintPreview.class);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_print_preview);
        Bundle bundle = getIntent().getExtras();
        TextView copies = (TextView) findViewById(R.id.copies);
        String copyNo ="Copies "+noOfCopy;
        copies.setText(copyNo);
        context=this;
        list.clear();
        getSupportActionBar().hide();
        EditText pagesCount=(EditText) findViewById(R.id.pagesCount);
        RadioButton radioBtnForPage =(RadioButton) findViewById(R.id.rb_page);
        Button addCopy = (Button) findViewById(R.id.plus);
        Button minusCopy = (Button) findViewById(R.id.minus);
        FloatingActionButton selectDocumentFloatingButton = (FloatingActionButton) findViewById(R.id.selectDocumentFloatingButton);

        if (LoginPrefs.Companion.getOCTAToken(this) == null) {
            Intent intent1 = new Intent(getApplicationContext(), SignInCompany.class);
            startActivity(intent1);
        }

        filePath = bundle.getString("filePath", "");
        File file = new File(filePath);
        SelectedFile selectedFile = new SelectedFile();
        selectedFile.setFileName(file.getName());
        selectedFile.setFilePath(filePath);
        selectedFile.setFromApi(false);
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        Date date = new Date();
        String strDate = dateFormat.format(date);
        selectedFile.setFileSelectedDate(strDate);
        Log.d("file name",file.getName());
        logger.info("Devnco_Android file name"+file.getName());
        if(file.getName().contains(".pdf")) {
            try {
                renderPageUsingDefaultPdfRendererFile(file);
                pagesCount.setVisibility(View.VISIBLE);
                radioBtnForPage.setVisibility(View.VISIBLE);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else if(file.getName().contains(".docx") || file.getName().contains(".doc")){

        }else{
            pagesCount.setVisibility(View.INVISIBLE);
            radioBtnForPage.setVisibility(View.INVISIBLE);
            jpgOrPngImagePreview(file);
        }
        Spinner dynamicSpinner = (Spinner) findViewById(R.id.dynamic_spinner);
        ArrayList<String> items = new ArrayList<String>();
        items.add("select printer");
        SharedPreferences prefs1 = PreferenceManager.getDefaultSharedPreferences(context);
        Gson gson1 = new Gson();
        String json2 = prefs1.getString("prefServerSecurePrinterListWithDetails", null);
        Type type1 = new TypeToken<ArrayList<PrinterModel>>() {}.getType();
        if (json2 != null) {
            serverSecurePrinterListWithDetailsSharedPreflist = gson1.fromJson(json2, type1);
            for(int i=0;i<serverSecurePrinterListWithDetailsSharedPreflist.size();i++){
                PrinterModel printerModel= serverSecurePrinterListWithDetailsSharedPreflist.get(i);
                items.add(printerModel.getServiceName());
            }
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, items);
        dynamicSpinner.setAdapter(adapter);


        dynamicSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectPrinter=parent.getItemAtPosition(position).toString();
                for(int i=0;i<serverSecurePrinterListWithDetailsSharedPreflist.size();i++){
                    PrinterModel printerModel= serverSecurePrinterListWithDetailsSharedPreflist.get(i);
                     if(printerModel.getServiceName().toString().equals(parent.getItemAtPosition(position).toString())){
                       selectedPrinterModel=printerModel;
                      }

                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });





        addCopy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                noOfCopy=noOfCopy+1;
                noOfCopies=noOfCopy;
                String copyNo ="Copies "+noOfCopy;
                copies.setText(copyNo);
            }
        });

        minusCopy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(noOfCopy>1) {
                    noOfCopy = noOfCopy - 1;
                    noOfCopies=noOfCopy;
                    String copyNo = "Copies " + noOfCopy;
                    copies.setText(copyNo);
                }
            }
        });

        Spinner staticSpinner = (Spinner) findViewById(R.id.static_spinner);
        String[] printItems = new String[] {"Monochrome", "Color"};
        ArrayAdapter<String> staticAdapter  = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, printItems);
        staticSpinner.setAdapter(staticAdapter);


        staticSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,int position, long id) {

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        radioGroup = (RadioGroup) findViewById(R.id.rg);
        RadioButton radioBtn =(RadioButton) findViewById(R.id.rb_all);
        RadioButton radioBtnPage =(RadioButton) findViewById(R.id.rb_page);
        radioGroup.check(radioBtn.getId());
        TextView cancel =(TextView) findViewById(R.id.cancel);

/*
        radioBtnForPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("tag","Click on page radiobutton");
                pickPrinterPageDialog();
            }
        });

*/


        selectDocumentFloatingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int selectedId = radioGroup.getCheckedRadioButtonId();
                radioButton = (RadioButton) findViewById(selectedId);
                Boolean PageIndexCorrect=true;
                if(radioButton.getText().toString().equals("page")) {
                    String pagecount =pagesCount.getText().toString();
                    String[] pageIndex = pagecount.split("-", 0);
                    startPageIndex= Integer.parseInt(pageIndex[0]);
                    endPageIndex= Integer.parseInt(pageIndex[1]);
                     if(startPageIndex>endPageIndex){
                         Toast.makeText(context, "please check pages number", Toast.LENGTH_LONG).show();
                         PageIndexCorrect=false;
                     }
                     if(endPageIndex>totalPageCount){
                        Toast.makeText(context, "please check pages number", Toast.LENGTH_LONG).show();
                        PageIndexCorrect=false;
                     }
                     if(startPageIndex<=0){
                         Toast.makeText(context, "please check pages number", Toast.LENGTH_LONG).show();
                         PageIndexCorrect=false;
                     }

                }

                if(selectedPrinterModel ==null ){
                    Toast.makeText(context, "please select printer", Toast.LENGTH_LONG).show();
                }
                if(selectPrinter.toString().equals("select printer")){
                    Toast.makeText(context, "please select printer", Toast.LENGTH_LONG).show();
                }
                if(radioButton.getText().toString().equals("All") && selectedPrinterModel !=null && filePath !=null  && !selectPrinter.toString().equals("select printer")) {
                    dialogPromptPrinter();
                }
                else if(radioButton.getText().toString().equals("page") && selectedPrinterModel !=null && filePath !=null  && !selectPrinter.toString().equals("select printer") && PageIndexCorrect==true) {
                    dialogPromptPrinter();
                }
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               onBackPressed();

            }
        });
    }





    public void renderPageUsingDefaultPdfRendererFile(File file) throws IOException {
        ParcelFileDescriptor fileDescriptor = ParcelFileDescriptor.open(file, MODE_READ_ONLY);
        PdfRenderer renderer = new PdfRenderer(fileDescriptor);
        final int pageCount = renderer.getPageCount();
        totalPageCount=pageCount;
        int pagePrintCounter = 0;
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
            files.add(renderFile);

            out.flush();
            out.close();
            pagePrintCounter++;
        }

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        mAdapter = new PrintPreviewAdapter(files);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);
        }
    public void jpgOrPngImagePreview(File file){
            ArrayList<File> files =new ArrayList<File>();
            files.add(file);
            RecyclerView recyclerView = findViewById(R.id.recyclerView);
            mAdapter = new PrintPreviewAdapter(files);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false);
            recyclerView.setLayoutManager(linearLayoutManager);
            recyclerView.setItemAnimator(new DefaultItemAnimator());
            recyclerView.setAdapter(mAdapter);
        }


    private void dialogPromptPrinter(){
        Dialog dialog1 = new Dialog(context);
        View v  = LayoutInflater.from(context).inflate(R.layout.dialog_printer_prompt, null);
        dialog1.setContentView(v);
        dialog1.setCancelable(false);
        Button hold = dialog1.findViewById(R.id.hold);
        Button release= dialog1.findViewById(R.id.release);
        dialog1.setCanceledOnTouchOutside(true);
        Window window = dialog1.getWindow();
        assert window != null;
        window.setLayout(AbsListView.LayoutParams.WRAP_CONTENT, AbsListView.LayoutParams.WRAP_CONTENT);
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.gravity = Gravity.CENTER;
        window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        window.setDimAmount(0.5f);
        window.setAttributes(wlp);
        dialog1.show();

        hold.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                list.clear();
                SelectedFile selectedFile = new SelectedFile();
                File file = new File(filePath);
                selectedFile.setFileName(file.getName());
                selectedFile.setFilePath(filePath);
                selectedFile.setFromApi(false);
                selectedFile.setJobSize(getFileSizeKiloBytes(file));
                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                Date date = new Date();
                String strDate = dateFormat.format(date);
                selectedFile.setFileSelectedDate(strDate);
                list.add(selectedFile);


                SharedPreferences prefs1 = PreferenceManager.getDefaultSharedPreferences(context);
                Gson gson1 = new Gson();
                String json2 = prefs1.getString("localdocumentlist", null);
                Type type1 = new TypeToken<ArrayList<SelectedFile>>() {
                }.getType();
                localDocumentSharedPreflist = gson1.fromJson(json2, type1);
                if (localDocumentSharedPreflist != null) {
                    list.addAll(localDocumentSharedPreflist);
                }
                SharedPreferences.Editor editor1 = prefs1.edit();

                String convertedJson = gson1.toJson(list);
                editor1.putString("localdocumentlist", convertedJson);
                editor1.apply();
                Toast.makeText(context, "file added", Toast.LENGTH_LONG).show();
                dialog1.cancel();
                Intent myIntent = new Intent(context, MainActivity.class);
                startActivity(myIntent);
            }
        });

        release.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                File file = new File(filePath);
                ArrayList<URI> ippUri =new ArrayList<URI>();
                if(selectedPrinterModel != null && selectedPrinterModel.getPrinterHost() != null){
                    String printerHost = selectedPrinterModel.getPrinterHost().toString();
                    ippUri.add(URI.create("ipp:/"+printerHost+":631/ipp/print"));
                    ippUri.add(URI.create("ipp:/"+printerHost+":631/ipp/printer"));
                    ippUri.add(URI.create("ipp:/"+printerHost+":631/ipp/lp"));
                    ippUri.add(URI.create("ipp:/"+printerHost+"/printer"));
                    ippUri.add(URI.create("ipp:/"+printerHost+"/ipp/print"));
                    ippUri.add(URI.create("http:/"+printerHost+":631/ipp"));
                    ippUri.add(URI.create("http:/"+printerHost+":631/ipp/print"));
                    ippUri.add(URI.create("http:/"+printerHost+":631/ipp/printer"));
                    ippUri.add(URI.create("http:/"+printerHost+":631/print"));
                    ippUri.add(URI.create("http:/"+printerHost+"/ipp/print"));
                    ippUri.add(URI.create("http:/"+printerHost));
                    ippUri.add(URI.create("http:/"+printerHost+":631/printers/lp1"));
                    ippUri.add(URI.create("https:/"+printerHost));
                    ippUri.add(URI.create("https:/"+printerHost+":443/ipp/print"));
                    ippUri.add(URI.create("ipps:/"+printerHost+":443/ipp/print"));
                    ippUri.add(URI.create("http:/"+printerHost+":631/ipp/lp"));
                }

                if(selectedPrinterModel != null && filePath != null && selectedPrinterModel.getPrinterHost() != null){
                    BottomNavigationActivityForServerPrint.selectedServerFile.clear();
                    SelectedFile selectedFile=new SelectedFile();
                    selectedFile.setFileName(file.getName());
                    selectedFile.setFilePath(file.getAbsolutePath());
                    BottomNavigationActivityForServerPrint.selectedServerFile.add(selectedFile);

                    BottomNavigationActivityForServerPrint.selectedPrinter.setPrinterHost(selectedPrinterModel.getPrinterHost());
                    BottomNavigationActivityForServerPrint.selectedPrinter.setServiceName(selectedPrinterModel.getServiceName());
                    BottomNavigationActivityForServerPrint.selectedPrinter.setId(selectedPrinterModel.getId());
                }



                if(file.getName().contains(".pdf")) {
                    if (radioButton.getText().toString().equals("All") && selectedPrinterModel != null && filePath != null && selectedPrinterModel.getPrinterHost() != null) {
                        String finalLocalurl = "http" + ":/" + selectedPrinterModel.getPrinterHost().toString() + ":631/ipp/print";
                        PrintRenderUtils printRenderUtils = new PrintRenderUtils();
                        printRenderUtils.renderPageUsingDefaultPdfRendererForSelectedPages(file, finalLocalurl, context, 0, totalPageCount, noOfCopies,ippUri);
                        Toast.makeText(context, "print release", Toast.LENGTH_LONG).show();
                        dialog1.cancel();
                       // moveTaskToBack(true);
                    }
                    if (radioButton.getText().toString().equals("page") && selectedPrinterModel != null && filePath != null && selectedPrinterModel.getPrinterHost() != null) {

                        String finalLocalurl = "http" + ":/" + selectedPrinterModel.getPrinterHost().toString() + ":631/ipp/print";
                        PrintRenderUtils printRenderUtils = new PrintRenderUtils();
                        printRenderUtils.renderPageUsingDefaultPdfRendererForSelectedPages(file, finalLocalurl, context, startPageIndex, endPageIndex, noOfCopies,ippUri);
                        Toast.makeText(context, "print release", Toast.LENGTH_LONG).show();
                        dialog1.cancel();
                     //   moveTaskToBack(true);
                    }
                }else if(file.getName().contains(".docx") || file.getName().contains(".doc")){

                }else{
                    if(radioButton.getText().toString().equals("All") && selectedPrinterModel !=null && filePath !=null && selectedPrinterModel.getPrinterHost() != null) {

                        String finalLocalurl = "http" + ":/" + selectedPrinterModel.getPrinterHost().toString() + ":631/ipp/print";
                        PrintRenderUtils printRenderUtils = new PrintRenderUtils();
                        printRenderUtils.printNoOfCOpiesJpgOrPngFiles(file, finalLocalurl, context, noOfCopies,ippUri);
                        Toast.makeText(context, "print release", Toast.LENGTH_LONG).show();
                        dialog1.cancel();
                      //  moveTaskToBack(true);

                    }
                }


            }

        });

    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    private static String getFileSizeKiloBytes(File file) {
        DecimalFormat df = new DecimalFormat("#.##");
        String formatted = df.format(file.length() / 1024);
        return formatted+ "KB";
    }

/*
    private void pickPrinterPageDialog() {
        dialog = new Dialog(context);
        v = LayoutInflater.from(context).inflate(R.layout.dialog_page_range_picker, null);
        dialog.setContentView(v);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(true);
        Window window = dialog.getWindow();
        assert window != null;
        window.setLayout(AbsListView.LayoutParams.MATCH_PARENT, AbsListView.LayoutParams.WRAP_CONTENT);
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.gravity = Gravity.BOTTOM;
        window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        window.setDimAmount(0.5f);
        window.setAttributes(wlp);

        picker1 = dialog.findViewById(R.id.numberpicker_main_picker);
        TextView txtCancel =dialog.findViewById(R.id.txtCancel);

        txtCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.cancel();
            }
        });

        picker1.setMaxValue(4);
        picker1.setMinValue(0);
        pickerVals  = new String[] {"1", "2", "3", "4", "5"};
        picker1.setDisplayedValues(pickerVals);

        picker1.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int i, int i1) {
                int valuePicker1 = picker1.getValue();
                Log.d("picker value", pickerVals[valuePicker1]);
            }
        });

        picker2 = dialog.findViewById(R.id.numberpicker_main_picker2);
        picker2.setMaxValue(4);
        picker2.setMinValue(0);
        pickerVals2  = new String[] {"1", "2", "3", "4", "5"};
        picker2.setDisplayedValues(pickerVals);

        picker2.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int i, int i1) {
                int valuePicker1 = picker2.getValue();
                Log.d("picker value", pickerVals2[valuePicker1]);
            }
        });

        dialog.show();
    }
*/

}