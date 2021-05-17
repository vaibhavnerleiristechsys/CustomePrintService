package com.example.customeprintservice.print;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.customeprintservice.MainActivity;
import com.example.customeprintservice.R;
import com.example.customeprintservice.adapter.FragmentPrinterAlphabetsListAdapter;
import com.example.customeprintservice.adapter.FragmentPrinterListAdapter;
import com.example.customeprintservice.adapter.PrintPreviewAdapter;
import com.example.customeprintservice.jipp.PrintActivity;
import com.example.customeprintservice.jipp.PrintRenderUtils;
import com.example.customeprintservice.jipp.PrintUtils;
import com.example.customeprintservice.jipp.PrinterModel;
import com.example.customeprintservice.prefs.LoginPrefs;
import com.example.customeprintservice.room.SelectedFile;
import com.example.customeprintservice.signin.SignInCompany;
import com.example.customeprintservice.utils.DataDogLogger;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.pdf.PdfRenderer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelFileDescriptor;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
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
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;

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
    public ArrayList<PrinterModel>deployedSecurePrinterListWithDetailsSharedPreflist= new ArrayList<PrinterModel>();
    public String selectPrinter=null;
    int startPageIndex=0;
    int endPageIndex=0;
    int totalPageCount=1;
    int noOfCopies=1;
    Dialog dialog;
    View v = null;
    public boolean isColor=false;
    public String  orientationValue="portrait";
    private NumberPicker picker1,picker2;
    private String[] pickerVals,pickerVals2;
    public Spinner orientationSpinner;
    //Logger logger = LoggerFactory.getLogger(PrintPreview.class);
    RecyclerView printerRecyclerView;
    TextView selectPrinterText;
    ImageView selectprinterarrow;

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
            @SuppressLint("WrongConstant") SharedPreferences prefs = context.getSharedPreferences("MySharedPref", Context.MODE_APPEND);
            String IsLdap = prefs.getString("IsLdap", "");
            if(!IsLdap.equals("LDAP")) {
                Intent intent1 = new Intent(getApplicationContext(), SignInCompany.class);
                startActivity(intent1);
            }
        }

        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                new IntentFilter("selected print preview printer"));

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
        DataDogLogger.getLogger().i("Devnco_Android file name"+file.getName());
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
     //   Spinner dynamicSpinner = (Spinner) findViewById(R.id.dynamic_spinner);
         orientationSpinner = (Spinner) findViewById(R.id.orientation_spinner);
        ArrayList<String> items = new ArrayList<String>();
        items.add("select printer");
        SharedPreferences prefs1 = PreferenceManager.getDefaultSharedPreferences(context);
        Gson gson1 = new Gson();
        Type type1 = new TypeToken<ArrayList<PrinterModel>>() {}.getType();


        String json = prefs1.getString("deployedPrintersListForPrintPreivew", null);
        if (json != null) {
            deployedSecurePrinterListWithDetailsSharedPreflist = gson1.fromJson(json, type1);
        }

        String json2 = prefs1.getString("prefServerSecurePrinterListWithDetails", null);
        if (json2 != null) {
            serverSecurePrinterListWithDetailsSharedPreflist = gson1.fromJson(json2, type1);

        }
        if(deployedSecurePrinterListWithDetailsSharedPreflist!=null && deployedSecurePrinterListWithDetailsSharedPreflist.size()>0){
            serverSecurePrinterListWithDetailsSharedPreflist.addAll(deployedSecurePrinterListWithDetailsSharedPreflist);
        }

        for(int i=0;i<serverSecurePrinterListWithDetailsSharedPreflist.size();i++){
            PrinterModel printerModel= serverSecurePrinterListWithDetailsSharedPreflist.get(i);
            items.add(printerModel.getServiceName());
        }

      //  ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, items);
      //  dynamicSpinner.setAdapter(adapter);


/*
        dynamicSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectPrinter=parent.getItemAtPosition(position).toString();
                for(int i=0;i<serverSecurePrinterListWithDetailsSharedPreflist.size();i++){
                    PrinterModel printerModel= serverSecurePrinterListWithDetailsSharedPreflist.get(i);
                     if(printerModel.getServiceName().toString().equals(parent.getItemAtPosition(position).toString())){
                       selectedPrinterModel=printerModel;
                         getAttributeResponse(selectedPrinterModel.getPrinterHost().toString());
                      }

                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


*/


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
                String printType = parent.getItemAtPosition(position).toString();
              Log.d("printType=",printType);
              if(printType.contains("Color")){
                  isColor=true;
                }
                if(printType.contains("Monochrome")){
                    isColor=false;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        orientationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,int position, long id) {
                String orientationType = parent.getItemAtPosition(position).toString();
                Log.d("orientationType=",orientationType);
                if(orientationType.contains("landscape")){
                    orientationValue="landscape";
                }else{
                    orientationValue="portrait";
                }
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
         selectPrinterText =(TextView) findViewById(R.id.selectPrinterText);
        selectprinterarrow =(ImageView) findViewById(R.id.selectprinterarrow);

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
                    if(pageIndex.length==2){
                        startPageIndex= Integer.parseInt(pageIndex[0]);
                        endPageIndex= Integer.parseInt(pageIndex[1]);
                    }else{
                        Toast.makeText(context, "please check pages number", Toast.LENGTH_LONG).show();
                        PageIndexCorrect=false;
                    }
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
                    String secure_release = selectedPrinterModel.getSecure_release();
                    if(selectedPrinterModel.getIsPullPrinter().equals("1")){
                        dialogPromptPrinter("alwaysHold");
                    }else{
                        if(secure_release.equals("0") || secure_release.equals("1") || secure_release.equals("2")){

                            Toast.makeText(context, "print release", Toast.LENGTH_LONG).show();
                            dialogPromptPrinter("alwaysPrint");

                        }else if(secure_release.equals("3") || secure_release.equals("4")){
                            Toast.makeText(context, "print hold", Toast.LENGTH_LONG).show();
                            dialogPromptPrinter("alwaysHold");

                        }
                        else if(secure_release.equals("5") || secure_release.equals("6")){

                            Toast.makeText(context, "print release", Toast.LENGTH_LONG).show();
                            dialogPromptPrinter("alwaysPrompt");


                        }
                    }
                    //dialogPromptPrinter();
                }
                else if(radioButton.getText().toString().equals("page") && selectedPrinterModel !=null && filePath !=null  && !selectPrinter.toString().equals("select printer") && PageIndexCorrect==true) {
                    String secure_release = selectedPrinterModel.getSecure_release();
                    if(selectedPrinterModel.getIsPullPrinter().equals("1")){
                        dialogPromptPrinter("alwaysHold");
                    }else{
                        if(secure_release.equals("0") || secure_release.equals("1") || secure_release.equals("2")){

                            Toast.makeText(context, "print release", Toast.LENGTH_LONG).show();
                            dialogPromptPrinter("alwaysPrint");

                        }else if(secure_release.equals("3") || secure_release.equals("4")){
                            Toast.makeText(context, "print hold", Toast.LENGTH_LONG).show();
                            dialogPromptPrinter("alwaysHold");

                        }
                        else if(secure_release.equals("5") || secure_release.equals("6")){

                            Toast.makeText(context, "print release", Toast.LENGTH_LONG).show();
                            dialogPromptPrinter("alwaysPrompt");


                        }
                    }
                    //dialogPromptPrinter();
                }
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               onBackPressed();

            }
        });


     selectPrinterText.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Collections.sort(serverSecurePrinterListWithDetailsSharedPreflist, new Comparator<PrinterModel>() {
                @Override
                public int compare(PrinterModel item, PrinterModel t1) {
                    String s1 = item.getServiceName();
                    String s2 = t1.getServiceName();
                    return s1.compareToIgnoreCase(s2);
                }

            });

            selectePrinterDialog(serverSecurePrinterListWithDetailsSharedPreflist);
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


    private void dialogPromptPrinter(String state){
        Dialog dialog1 = new Dialog(context);
        View v  = LayoutInflater.from(context).inflate(R.layout.dialog_printer_prompt, null);
        dialog1.setContentView(v);
        dialog1.setCancelable(false);
        Button hold = dialog1.findViewById(R.id.hold);
        Button release= dialog1.findViewById(R.id.release);
        Button cancel =dialog1.findViewById(R.id.cancel);

        if(state.equals("alwaysHold")){
            release.setVisibility(View.GONE);
        }else if(state.equals("alwaysPrint")){
            hold.setVisibility(View.GONE);
        }else if(state.equals("alwaysPrompt")){

        }


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

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            dialog1.cancel();
            }
            });



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
                String json2 = prefs1.getString("holdlocaldocumentlist", null);
                Type type1 = new TypeToken<ArrayList<SelectedFile>>() {
                }.getType();
                localDocumentSharedPreflist = gson1.fromJson(json2, type1);
                if (localDocumentSharedPreflist != null) {
                    list.addAll(localDocumentSharedPreflist);
                }
                SharedPreferences.Editor editor1 = prefs1.edit();

                String convertedJson = gson1.toJson(list);
                editor1.putString("holdlocaldocumentlist", convertedJson);
                editor1.apply();
                Toast.makeText(context, "file added", Toast.LENGTH_LONG).show();
                dialog1.cancel();
                moveTaskToBack(true);
              //  Intent myIntent = new Intent(context, MainActivity.class);
               // startActivity(myIntent);

                String FileName =file.getName().toString();
               Long fileSize= file.length();
                PrintReleaseFragment printReleaseFragment =new PrintReleaseFragment();
                String  pageCount =String.valueOf(totalPageCount);
                if(selectedPrinterModel != null && selectedPrinterModel.getPrinterHost() != null) {
                   String printerId=selectedPrinterModel.getId();
                   String isPullPrinter=selectedPrinterModel.getIsPullPrinter().toString();
                   Log.d("isPullPrinter :",isPullPrinter);
                    printReleaseFragment.sendHeldJob(context,  FileName, fileSize.toString(), pageCount,printerId,isPullPrinter,"");
                }
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
                    ippUri.add(URI.create("ipp:/"+printerHost+"/ipp"));
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
                        printRenderUtils.renderPageUsingDefaultPdfRendererForSelectedPages(file, finalLocalurl, context, 0, totalPageCount, noOfCopies,ippUri,totalPageCount,isColor);
                        Toast.makeText(context, "print release", Toast.LENGTH_LONG).show();
                        dialog1.cancel();
                        moveTaskToBack(true);
                    }
                    if (radioButton.getText().toString().equals("page") && selectedPrinterModel != null && filePath != null && selectedPrinterModel.getPrinterHost() != null) {

                        String finalLocalurl = "http" + ":/" + selectedPrinterModel.getPrinterHost().toString() + ":631/ipp/print";
                        PrintRenderUtils printRenderUtils = new PrintRenderUtils();
                        printRenderUtils.renderPageUsingDefaultPdfRendererForSelectedPages(file, finalLocalurl, context, startPageIndex, endPageIndex, noOfCopies,ippUri,totalPageCount,isColor);
                        Toast.makeText(context, "print release", Toast.LENGTH_LONG).show();
                        dialog1.cancel();
                        moveTaskToBack(true);
                    }
                }else if(file.getName().contains(".docx") || file.getName().contains(".doc")){

                }else{
                    if(radioButton.getText().toString().equals("All") && selectedPrinterModel !=null && filePath !=null && selectedPrinterModel.getPrinterHost() != null) {

                        String finalLocalurl = "http" + ":/" + selectedPrinterModel.getPrinterHost().toString() + ":631/ipp/print";
                        PrintRenderUtils printRenderUtils = new PrintRenderUtils();
                        printRenderUtils.printNoOfCOpiesJpgOrPngFiles(file, finalLocalurl, context, noOfCopies,ippUri,isColor,orientationValue);
                        Toast.makeText(context, "print release", Toast.LENGTH_LONG).show();
                        dialog1.cancel();
                        moveTaskToBack(true);

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

    public void getAttributeResponse(String hostAddress) {
        new Thread() {

            public void run()    //Anonymous class overriding run() method of Thread class
            {
                try {
                    ArrayList<URI> ippUri = new ArrayList<URI>();

                    if (hostAddress != null) {
                        String printerHost = hostAddress;
                        ippUri.add(URI.create("ipp:/" + printerHost + ":631/ipp/print"));
                        ippUri.add(URI.create("ipp:/" + printerHost + ":631/ipp/printer"));
                        ippUri.add(URI.create("ipp:/" + printerHost + ":631/ipp/lp"));
                        ippUri.add(URI.create("ipp:/" + printerHost + "/printer"));
                        ippUri.add(URI.create("ipp:/" + printerHost + "/ipp"));
                        ippUri.add(URI.create("ipp:/" + printerHost + "/ipp/print"));
                        ippUri.add(URI.create("http:/" + printerHost + ":631/ipp"));
                        ippUri.add(URI.create("http:/" + printerHost + ":631/ipp/print"));
                        ippUri.add(URI.create("http:/" + printerHost + ":631/ipp/printer"));
                        ippUri.add(URI.create("http:/" + printerHost + ":631/print"));
                        ippUri.add(URI.create("http:/" + printerHost + "/ipp/print"));
                        ippUri.add(URI.create("http:/" + printerHost));
                        ippUri.add(URI.create("http:/" + printerHost + ":631/printers/lp1"));
                        ippUri.add(URI.create("https:/" + printerHost));
                        ippUri.add(URI.create("https:/" + printerHost + ":443/ipp/print"));
                        ippUri.add(URI.create("ipps:/" + printerHost + ":443/ipp/print"));
                        ippUri.add(URI.create("http:/" + printerHost + ":631/ipp/lp"));
                    }
                    PrintUtils printUtils = new PrintUtils();
                    Map<String, String> resultMap = printUtils.getAttributesCall(ippUri, context);
                    if (!resultMap.containsKey("status")) {
                        new Handler(Looper.getMainLooper()).post(
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(context, " get Attribute failed", Toast.LENGTH_LONG).show();
                                    }
                                });
                    }
                    if(resultMap.get("status").equals("successful-ok")){
                          String orientation =resultMap.get("orientation-requested-supported");
                          String sides =resultMap.get("sides-supported");
                          String color =resultMap.get("print-color-mode-supported");
                          String media=resultMap.get("media-supported");
                          String[] orientationSupported = orientation.split("=");
                          String[] sidesSupported = sides.split("=");
                          String[] colorSupported = color.split("=");
                          String[] mediaSupported = media.split("=");
                        String orientationStrigified="";
                        String sidesStrigified="";
                        String colorStrigified="";
                        String mediaStrigified="";
                        List<String> orientationSupportList =new ArrayList<String>();
                        List<String> sidesSupportList =new ArrayList();
                        List<String> colorSupportList =new ArrayList();
                        List<String> mediaSupportList =new ArrayList();
                          if(orientationSupported.length>1){
                              orientationStrigified  =orientationSupported[1];
                              if(orientationStrigified.contains(",")){
                                  String removeSplChar =orientationStrigified.replaceAll("\\[", "");
                                  String removeSplChar1 =removeSplChar.replaceAll("\\]", "");
                                  String removeSplChar2 =removeSplChar1.replaceAll(" ","");
                                  String[] orientationSupportedArray  = removeSplChar2.split(",");
                                  orientationSupportList = Arrays.asList(orientationSupportedArray);
                              }else{
                                  orientationSupportList.add(orientationStrigified);
                              }

                          }if(sidesSupported.length>1){
                             sidesStrigified =sidesSupported[1];
                             if(sidesStrigified.contains(",")){
                                 String removeSplChar =sidesStrigified.replaceAll("\\[", "");
                                 String removeSplChar1 =removeSplChar.replaceAll("\\]", "");
                                 String removeSplChar2 =removeSplChar1.replaceAll(" ","");
                                 String[]  sidesSupportedArray  = removeSplChar2.split(",");
                                 sidesSupportList = Arrays.asList(sidesSupportedArray);
                             }else{
                                 sidesSupportList.add(sidesStrigified);
                             }
                          }
                          if(colorSupported.length>1){
                             colorStrigified =colorSupported[1];
                              if(colorStrigified.contains(",")){
                                  String removeSplChar =colorStrigified.replaceAll("\\[", "");
                                  String removeSplChar1 =removeSplChar.replaceAll("\\]", "");
                                  String removeSplChar2=removeSplChar1.replaceAll(" ","");
                                  String[] colorSupportedArray  = removeSplChar2.split(",");
                                  colorSupportList = Arrays.asList(colorSupportedArray);
                              }else{
                                  colorSupportList.add(colorStrigified);
                              }
                           }
                          if(mediaSupported.length>1){
                             mediaStrigified =mediaSupported[1];
                              if(mediaStrigified.contains(",")){
                                  String removeSplChar =mediaStrigified.replaceAll("\\[", "");
                                  String removeSplChar1 =removeSplChar.replaceAll("\\]", "");
                                  String removeSplChar2 =removeSplChar1.replaceAll(" ","");
                                  String[] mediaSupportedArray  = removeSplChar2.split(",");
                                  mediaSupportList = Arrays.asList(mediaSupportedArray);
                              }else{
                                  mediaSupportList.add(mediaStrigified);

                              }
                           }

                          Log.d("orientationSupported:",orientationStrigified);
                          Log.d("sidesSupported:",sidesStrigified);
                          Log.d("colorSupported:",colorStrigified);
                          Log.d("mediaSupported:",mediaStrigified);
                        ArrayList<String> items = new ArrayList<String>();

                          for(int i=0;i<orientationSupportList.size();i++){
                            Log.d("orientationSupported:",orientationSupportList.get(i));
                            items.add(orientationSupportList.get(i));
                          }
                          for(int i=0;i<sidesSupportList.size();i++){
                            Log.d("sidesSupported:",sidesSupportList.get(i));
                          }
                          for(int i=0;i<colorSupportList.size();i++){
                            Log.d("colorSupported:",colorSupportList.get(i));
                          }
                          for(int i=0;i<mediaSupportList.size();i++){
                            Log.d("mediaSupported:",mediaSupportList.get(i));
                          }
                          items.add("landscape");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(),android.R.layout.simple_spinner_item, items);
                                orientationSpinner.setAdapter(adapter);
                            }
                        });



                    }

                } catch (Exception exp) {
                    DataDogLogger.getLogger().e("Devnco_Android Exception - " + exp.getMessage());
                    new Handler(Looper.getMainLooper()).post(
                            new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(context, exp.getMessage(), Toast.LENGTH_LONG).show();
                                }
                            });
                }
            }
        }.start();

    }




    private void selectePrinterDialog(ArrayList<PrinterModel> list) {
        dialog = new Dialog(context);
        v = LayoutInflater.from(context).inflate(R.layout.dialog_printpreview_select_printer, null);
        dialog.setContentView(v);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(true);
        Window window = dialog.getWindow();
        assert window != null;
        window.setLayout(AbsListView.LayoutParams.MATCH_PARENT, AbsListView.LayoutParams.MATCH_PARENT);
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.gravity = Gravity.BOTTOM;
        window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        window.setAttributes(wlp);




        printerRecyclerView = dialog.findViewById(R.id.dialogSelectPrinterRecyclerView);
        ImageView imgCancel = dialog.findViewById(R.id.imgDialogSelectPrinterCancel);
        printerRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        printerRecyclerView.setAdapter(new FragmentPrinterListAdapter(context,list,"printpreview"));
        printerRecyclerView.setItemViewCacheSize(50);

        imgCancel.setOnClickListener(v ->
                dialog.cancel()
        );

        dialog.show();
    }

    public BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(dialog != null) {
                dialog.cancel();
            }
            String printerName  = intent.getStringExtra("printer name");
            selectPrinter=printerName;
            selectPrinterText.setText(printerName);
            selectprinterarrow.setVisibility(View.GONE);
            for(int i=0;i<serverSecurePrinterListWithDetailsSharedPreflist.size();i++){
                PrinterModel printerModel= serverSecurePrinterListWithDetailsSharedPreflist.get(i);
                if(printerModel.getServiceName().toString().equals(printerName.toString())){
                    selectedPrinterModel=printerModel;
                    if(selectedPrinterModel.getPrinterHost() !=null) {
                        if(selectedPrinterModel.getIsPullPrinter().equals("0")) {
                            getAttributeResponse(selectedPrinterModel.getPrinterHost().toString());
                        }
                    }
                }

            }
        }
    };


    public  static void setJobId(Context context,String JobId,String fileName){
        SharedPreferences prefs1 = PreferenceManager.getDefaultSharedPreferences(context);
        ArrayList<SelectedFile> documentSharedPreflist = new ArrayList<SelectedFile>();
        Gson gson1 = new Gson();
        String json2 = prefs1.getString("holdlocaldocumentlist", null);
        Type type1 = new TypeToken<ArrayList<SelectedFile>>() {
        }.getType();
        documentSharedPreflist = gson1.fromJson(json2, type1);
        if(documentSharedPreflist != null) {
            for (int i = 0; i < documentSharedPreflist.size(); i++) {
                SelectedFile selectedPrefFile = documentSharedPreflist.get(i);
                if (selectedPrefFile.getFileName().contains(fileName)) {
                    selectedPrefFile.setJobId(JobId);
                }
            }
        }

        SharedPreferences.Editor editor1 = prefs1.edit();
        String convertedJson = gson1.toJson(documentSharedPreflist);
        editor1.putString("holdlocaldocumentlist", convertedJson);
        editor1.apply();
    }

}