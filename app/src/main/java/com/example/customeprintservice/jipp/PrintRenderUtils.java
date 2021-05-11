package com.example.customeprintservice.jipp;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.widget.Toast;

import com.example.customeprintservice.model.JobsModel;
import com.example.customeprintservice.print.PrintReleaseFragment;
import com.example.customeprintservice.print.PrintersFragment;
import com.example.customeprintservice.utils.DataDogLogger;
import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.pdmodel.PDPageTree;
import com.tom_roush.pdfbox.rendering.PDFRenderer;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;

import static android.os.ParcelFileDescriptor.MODE_READ_ONLY;

public class PrintRenderUtils {
   // Logger logger = LoggerFactory.getLogger(PrintUtils.class);

    public void renderPageUsingDefaultPdfRenderer(File file, String printerString, Context context,String hostAddress,int colorMode) {
        ArrayList<JobsModel> jobIdList=new ArrayList<>();
        PrintUtils.jobstatusList.clear();
        new Thread() {

            public void run()    //Anonymous class overriding run() method of Thread class
            {
                int totalPageCount = 0;

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
                        ippUri.add(URI.create("http:/"+printerHost+":631/ipp/lp"));
                    }



                    PrintUtils printUtils = new PrintUtils();
                    Map<String, String> resultMap = printUtils.getAttributesCall(ippUri,context);
                    if (!resultMap.containsKey("status")) {
                        // show error
                        new Handler(Looper.getMainLooper()).post(
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(context, " get Attribute failed", Toast.LENGTH_LONG).show();
                                    }
                                });

                        // show toast
                    } else if(resultMap.get("status").equals("successful-ok")) {
                        URI finalUri = URI.create(resultMap.get("finalUri"));
                        String versionNumber =resultMap.get("versionNumber");

                    ParcelFileDescriptor fileDescriptor = ParcelFileDescriptor.open(file, MODE_READ_ONLY);
                    PdfRenderer renderer = new PdfRenderer(fileDescriptor);
                    final int pageCount = renderer.getPageCount();
                    totalPageCount=renderer.getPageCount();
                    int pagePrintCounter = 0;
                    int threadSleepInMilliSecs = 3000;
                    int timeThreshold = threadSleepInMilliSecs * 40;
                    int totalTimeThreadSleep = 0;

                    while (pagePrintCounter < pageCount) {
                        Map<String, String> map = null;
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
                        DataDogLogger.getLogger().i("Devnco_Android Saved Image - "+ renderFile.getAbsolutePath());
                        out.flush();
                        out.close();


                        if (!renderFile.exists()) {
                            String expMessage = "Exception occurred while rendering: file not created ";
                            new Handler(Looper.getMainLooper()).post(
                                    new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(context, expMessage, Toast.LENGTH_LONG).show();
                                        }
                                    });

                            break;
                        } else {

                             map = printUtils.print(finalUri, renderFile, context, "", versionNumber,"");
                            DataDogLogger.getLogger().i("Devnco_Android print status:"+map.get("status").toString());
                            if (map.get("status").equals("server-error-busy")) {
                                Thread.sleep(threadSleepInMilliSecs);
                                totalTimeThreadSleep = totalTimeThreadSleep + threadSleepInMilliSecs;
                                if (totalTimeThreadSleep > timeThreshold) {
                                    String expMessage = "The printer is unresponsive. Aborting ";
                                    new Handler(Looper.getMainLooper()).post(
                                            new Runnable() {
                                                @Override
                                                public void run() {
                                                    Toast.makeText(context, expMessage, Toast.LENGTH_LONG).show();
                                                }
                                            });

                                    Log.i("printer", expMessage);
                                    DataDogLogger.getLogger().e("Devnco_Android printer:"+ expMessage);
                                    break;
                                }
                            } else {
                                pagePrintCounter++;
                                totalTimeThreadSleep = 0;
                            }
                        }

                        String jobIdString =map.get("jobId");
                        URI uri =finalUri;
                        int pageCountNo=pagePrintCounter;
                        if(jobIdString != null) {
                            JobsModel jobModel =new JobsModel();
                            final Handler handler = new Handler(Looper.getMainLooper());
                                        int jobId = Integer.parseInt(jobIdString);
                                        jobModel.setJobId(jobId);
                                        jobModel.setUsedUri(uri);
                                        jobModel.setPageNo(pageCountNo);
                                        jobIdList.add(jobModel);
                        }
                    }

                    Log.v("Saved Image - ", "page print counter: " + pagePrintCounter);
                    DataDogLogger.getLogger().i("Devnco_Android Saved Image - "+ "page print counter: " + pagePrintCounter);
                    PrintReleaseFragment printReleaseFrament = new PrintReleaseFragment();
                    printReleaseFrament.sendMetaData(context,pageCount,colorMode);
                }
                } catch (Exception exp) {
                    String expMessage = "Exception occurred while rendering: " + exp.toString();

                    new Handler(Looper.getMainLooper()).post(
                            new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(context, expMessage, Toast.LENGTH_LONG).show();
                                }});


                    Log.v("Saved Image - ", exp.toString());
                    DataDogLogger.getLogger().i("Devnco_Android Saved Image - "+ exp.toString());
                    exp.printStackTrace();
                }

                int finalTotalPageCount =totalPageCount;
                final Handler handler =  new Handler(Looper.getMainLooper());
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Collections.sort(jobIdList, new Comparator<JobsModel>() {
                                @Override
                                public int compare(JobsModel item, JobsModel t1) {
                                    return Integer.compare(item.getJobId(), t1.getJobId());
                                }

                            });

                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        for(int i=0;i<jobIdList.size();i++){
                                            JobsModel jobModel = jobIdList.get(i);
                                            PrintUtils printUtils = new PrintUtils();
                                            printUtils.getJobsStatus(jobModel.getUsedUri(), context, jobModel.getJobId(), jobModel.getPageNo());
                                            Log.d("print Status done ", "print Status done");
                                            Thread.sleep(5000);
                                        }

                                        int CompletedPageCounter =0;
                                        String errorPageString ="Printing error in page no. ";
                                        for(int i=0;i<PrintUtils.jobstatusList.size();i++){
                                            JobsModel jobmodel = PrintUtils.jobstatusList.get(i);
                                            if(jobmodel.getJobStatus().toLowerCase().contains("completed")){
                                                CompletedPageCounter++;
                                            }else{

                                                errorPageString=errorPageString+jobmodel.getPageNo()+",";
                                            }
                                        }

                                        if(finalTotalPageCount==CompletedPageCounter){
                                            new Handler(Looper.getMainLooper()).post(
                                                    new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            Toast.makeText(context, "Successfully All Pages Printed.", Toast.LENGTH_LONG).show();
                                                        }
                                                    });
                                        }else{
                                            String errorString=errorPageString;
                                            new Handler(Looper.getMainLooper()).post(
                                                    new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            Toast.makeText(context, errorString, Toast.LENGTH_LONG).show();
                                                        }
                                                    });
                                        }


                                    } catch (InterruptedException | IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }, 10000);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }, 60000);
            }

        }.start();


    }


    public void renderPage(File file, String printerString, Context context) {
        new Thread() {

            public void run()    //Anonymous class overriding run() method of Thread class
            {

                try {
                    URI finalUri = URI.create(printerString);
                    InputStream pdfInputStream =
                            new BufferedInputStream(new FileInputStream(file));
                    PDDocument document = PDDocument.load(pdfInputStream);
                    PDPageTree pageTree = document.getPages();
                    PDFRenderer renderer = new PDFRenderer(document);
                    int totalNoOfPages = pageTree.getCount();
                    PrintUtils printUtils = new PrintUtils();
                    Bitmap pageImage = null;
                    int pagePrintCounter = 0;

                    while (pagePrintCounter < totalNoOfPages) {
                        String path = "/storage/self/primary/sample" + pagePrintCounter + ".jpg";
                        File renderFile = new File(path);

                        if (!renderFile.exists()) {
                            // make sure that do all this stuff if only the file hasnt been created
                            pageImage =
                                    renderer.renderImage(pagePrintCounter, 1F, Bitmap.Config.RGB_565);

                            FileOutputStream fileOut = new FileOutputStream(renderFile);
                            long bytes = pageImage.getByteCount();
                            boolean result = pageImage.compress(Bitmap.CompressFormat.JPEG, 100, fileOut);
                            fileOut.close();
                        }

                        Map map = printUtils.print(finalUri, renderFile, context, "","0x200","");

                        if (map.get("status") == "server-error-busy") {
                            Thread.sleep(5000);
                        } else {
                            pagePrintCounter++;
                        }
                    }

                } catch (Exception exp) {
                    exp.printStackTrace();
                }
            }

        }.start();


    }


    public void renderPageUsingDefaultPdfRendererForSelectedPages(File file, String printerString, Context context,int startIndex,int endIndex,int noOfCopies, ArrayList<URI> ippUri,int TotalPageCount,boolean isColor) {
      ArrayList<JobsModel> jobIdList=new ArrayList<>();
        PrintUtils.jobstatusList.clear();
        new Thread() {

            public void run()    //Anonymous class overriding run() method of Thread class
            {
                try {
                    Log.d("startIndex", String.valueOf(startIndex));
                    Log.d("endIndex", String.valueOf(endIndex));

                    ParcelFileDescriptor fileDescriptor = ParcelFileDescriptor.open(file, MODE_READ_ONLY);
                    PdfRenderer renderer = new PdfRenderer(fileDescriptor);
                    final int pageCount = renderer.getPageCount();
                    URI finalUri = URI.create(printerString);
                    PrintUtils printUtils = new PrintUtils();
                    Bitmap pageImage = null;
                    Map<String, String> resultMap = printUtils.getAttributesCall(ippUri,context);
                    String attributeStatus ="attrribute status:"+resultMap.get("status");
                    DataDogLogger.getLogger().i("Devnco_Android attrribute status:"+ attributeStatus);

                    if (!resultMap.containsKey("status")) {
                        // show error
                        new Handler(Looper.getMainLooper()).post(
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(context, " get Attribute failed", Toast.LENGTH_LONG).show();
                                    }
                                });

                        // show toast
                    } else
                        if(resultMap.get("status").equals("successful-ok")) {
                        finalUri = URI.create(resultMap.get("finalUri"));
                        String versionNumber =resultMap.get("versionNumber");
                    for (int i = 0; i < noOfCopies; i++) {
                        int pagePrintCounter = 0;
                        int threadSleepInMilliSecs = 3000;
                        int timeThreshold = threadSleepInMilliSecs * 40;
                        int totalTimeThreadSleep = 0;
                        int startIndexOfPage = startIndex - 1;
                        int endIndexOfPage = endIndex - 1;
                        int counter = 0;
                        while (pagePrintCounter < pageCount) {
                            Map<String, String> map = null;
                            if (startIndexOfPage <= pagePrintCounter && endIndexOfPage >= pagePrintCounter) {

                                String path = "/storage/self/primary/sample" + pagePrintCounter + ".jpg";
                                File renderFile = new File(path);
                                PdfRenderer.Page page = renderer.openPage(pagePrintCounter);
                                Bitmap bitmap = Bitmap.createBitmap(page.getWidth(), page.getHeight(), Bitmap.Config.ARGB_8888);
                                Canvas canvas = new Canvas(bitmap);
                                if(isColor==true) {
                                    canvas.drawColor(Color.WHITE);
                                    canvas.drawBitmap(bitmap, 0, 0, null);
                                }else{
                                    ColorMatrix colorMatrix = new ColorMatrix();
                                    colorMatrix.setSaturation(0);
                                    Paint paint = new Paint();
                                    paint.setColorFilter(new ColorMatrixColorFilter(colorMatrix));
                                    canvas.drawBitmap(bitmap, 0, 0, paint);
                                }
                                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
                                page.close();

                                FileOutputStream out = new FileOutputStream(renderFile);
                                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                                Log.v("Saved Image - ", renderFile.getAbsolutePath());
                                out.flush();
                                out.close();


                                if (!renderFile.exists()) {
                                    String expMessage = "Exception occurred while rendering: file not created ";
                                    new Handler(Looper.getMainLooper()).post(
                                            new Runnable() {
                                                @Override
                                                public void run() {
                                                    Toast.makeText(context, expMessage, Toast.LENGTH_LONG).show();
                                                }
                                            });

                                    break;
                                } else {
                                    String ippUriFinal = finalUri.toString();

                                     map = printUtils.print(finalUri, renderFile, context, "",versionNumber,"");
                                    DataDogLogger.getLogger().i("Devnco_Android print status:"+map.get("status").toString());

                                    if (map.get("status").equals("server-error-busy")) {
                                        Thread.sleep(threadSleepInMilliSecs);
                                        totalTimeThreadSleep = totalTimeThreadSleep + threadSleepInMilliSecs;
                                        if (totalTimeThreadSleep > timeThreshold) {
                                            String expMessage = "The printer is unresponsive. Aborting ";
                                            new Handler(Looper.getMainLooper()).post(
                                                    new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            Toast.makeText(context, expMessage, Toast.LENGTH_LONG).show();
                                                        }
                                                    });

                                            Log.i("printer", expMessage);
                                            DataDogLogger.getLogger().e("Devnco_Android printer:"+ expMessage);
                                            break;
                                        }
                                    } else {
                                        pagePrintCounter++;
                                        totalTimeThreadSleep = 0;
                                    }
                                }

                            } else {
                                pagePrintCounter++;
                                totalTimeThreadSleep = 0;
                            }

                            
                            String jobIdString =map.get("jobId");
                            URI uri =finalUri;
                            int pageCountNo=pagePrintCounter;
                            JobsModel jobModel =new JobsModel();

                            if(jobIdString != null) {
                                            int jobId = Integer.parseInt(jobIdString);
                                            jobModel.setJobId(jobId);
                                            jobModel.setUsedUri(uri);
                                            jobModel.setPageNo(pageCountNo);
                                            jobIdList.add(jobModel);
                            }
                        }
                        Log.v("Saved Image - ", "page print counter: " + pagePrintCounter);
                    }
                        PrintReleaseFragment printReleaseFrament =new PrintReleaseFragment();
                    int colorMode=0;
                    if(isColor ==true){
                        colorMode =1;
                    }else{
                        colorMode=0;
                    }
                        printReleaseFrament.sendMetaData(context,TotalPageCount,colorMode);
                }else{
                        new Handler(Looper.getMainLooper()).post(
                                new Runnable() {
                                    @Override
                                    public void run() {
                                       Toast.makeText(context, resultMap.get("status").toString(), Toast.LENGTH_LONG).show();
                                    }
                                });
                    }

                } catch (Exception exp) {
                    String expMessage = "Exception occurred while rendering: " + exp.toString();
                    DataDogLogger.getLogger().e("Devnco_Android Exception - "+expMessage);
                    new Handler(Looper.getMainLooper()).post(
                            new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(context, expMessage, Toast.LENGTH_LONG).show();
                                }});


                    Log.v("Saved Image - ", exp.toString());

                    exp.printStackTrace();
                }
                Log.d("print done ", "print all pages done ");
              final Handler handler =  new Handler(Looper.getMainLooper());
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Collections.sort(jobIdList, new Comparator<JobsModel>() {
                                @Override
                                public int compare(JobsModel item, JobsModel t1) {
                                    return Integer.compare(item.getJobId(), t1.getJobId());
                                }

                            });
                                handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            for(int i=0;i<jobIdList.size();i++){
                                            JobsModel jobModel = jobIdList.get(i);
                                            PrintUtils printUtils = new PrintUtils();
                                            printUtils.getJobsStatus(jobModel.getUsedUri(), context, jobModel.getJobId(), jobModel.getPageNo());
                                            Log.d("print Status done ", "print Status done");
                                            Thread.sleep(5000);
                                        }


                                            int CompletedPageCounter =0;
                                            String errorPageString ="Printing error in page no. ";
                                            for(int i=0;i<PrintUtils.jobstatusList.size();i++){
                                                JobsModel jobmodel = PrintUtils.jobstatusList.get(i);
                                                if(jobmodel.getJobStatus().toLowerCase().contains("completed")){
                                                    CompletedPageCounter++;
                                                }else{

                                                    errorPageString=errorPageString+jobmodel.getPageNo()+",";
                                                }
                                            }

                                            if(TotalPageCount==CompletedPageCounter){
                                                new Handler(Looper.getMainLooper()).post(
                                                        new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                Toast.makeText(context, "Successfully All Pages Printed.", Toast.LENGTH_LONG).show();
                                                            }
                                                        });
                                            }else{
                                                String errorString=errorPageString;
                                                new Handler(Looper.getMainLooper()).post(
                                                        new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                Toast.makeText(context, errorString, Toast.LENGTH_LONG).show();
                                                            }
                                                        });
                                            }


                                        } catch (InterruptedException | IOException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }, 10000);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }, 60000);




            }
        }.start();

    }


    public void printNoOfCOpiesJpgOrPngFiles(File file, String printerString, Context context, int noOfCopies, ArrayList<URI> ippUri,boolean isColor,String orientationValue) {
        PrintUtils.jobstatusList.clear();
        new Thread() {

            public void run()    //Anonymous class overriding run() method of Thread class
            {
                try {
                    URI finalUri = URI.create(printerString);
                    PrintUtils printUtils = new PrintUtils();
                 Map<String, String> resultMap = printUtils.getAttributesCall(ippUri,context);

                    String attributeStatus ="attribute status:"+resultMap.get("status");
                    DataDogLogger.getLogger().i("Devnco_Android attribute status:"+ attributeStatus);

                    if (!resultMap.containsKey("status")) {
                        // show error
                        new Handler(Looper.getMainLooper()).post(
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(context, " get Attribute failed", Toast.LENGTH_LONG).show();
                                    }
                                });

                        // show toast
                         } else if(resultMap.get("status").equals("successful-ok")) {
                        finalUri = URI.create(resultMap.get("finalUri"));
                       String versionNumber =resultMap.get("versionNumber");
                    for (int i = 0; i < noOfCopies; i++) {
                        int pagePrintCounter = 0;
                        int threadSleepInMilliSecs = 3000;
                        int timeThreshold = threadSleepInMilliSecs * 40;
                        int totalTimeThreadSleep = 0;
                        while (pagePrintCounter < 1) {
                            String ippUriFinal = finalUri.toString();

                            Map<String, String> map;
                            if(isColor == true) {
                                map = printUtils.print(finalUri, file, context, "", versionNumber,orientationValue);
                            }else{
                                map = printUtils.print(finalUri, convertColorToMonochrome(file), context, "", versionNumber,orientationValue);
                            }
                            String print ="print status:"+map.get("status").toString();
                            DataDogLogger.getLogger().i("Devnco_Android print status:"+map.get("status").toString());

                            if (map.get("status").equals("server-error-busy")) {
                                Thread.sleep(threadSleepInMilliSecs);
                                totalTimeThreadSleep = totalTimeThreadSleep + threadSleepInMilliSecs;
                                if (totalTimeThreadSleep > timeThreshold) {
                                    String expMessage = "The printer is unresponsive. Aborting ";
                                    new Handler(Looper.getMainLooper()).post(
                                            new Runnable() {
                                                @Override
                                                public void run() {
                                                    Toast.makeText(context, expMessage, Toast.LENGTH_LONG).show();
                                                }
                                            });

                                    Log.i("printer", expMessage);
                                    break;
                                }
                            } else {
                                pagePrintCounter++;
                                totalTimeThreadSleep = 0;
                            }

                            String jobIdString =map.get("jobId");
                            URI uri =finalUri;
                            if(jobIdString != null) {
                                final Handler handler =  new Handler(Looper.getMainLooper());
                                handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            int jobId = Integer.parseInt(jobIdString);
                                            printUtils.getJobsStatus(uri, context, jobId,1);
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }, 60000);
                            }
                        }
                        Log.v("Saved Image - ", "page print counter: " + pagePrintCounter);

                    }
                        int colorMode=0;
                        if(isColor ==true){
                            colorMode =1;
                        }else{
                            colorMode=0;
                        }

                        PrintReleaseFragment printReleaseFrament =new PrintReleaseFragment();
                        try {
                            printReleaseFrament.sendMetaData(context, 1, colorMode);
                        }catch (Exception e){
                            Log.i("printer", "Exception in send MetaData ------>>>" + e.getMessage());
                        }
                }else{
                        new Handler(Looper.getMainLooper()).post(
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(context, resultMap.get("status").toString(), Toast.LENGTH_LONG).show();
                                    }
                                });

                    }
                } catch (Exception exp) {
                    String expMessage = "Exception occurred while rendering: " + exp.toString();
                    // Toast.makeText(context, expMessage, Toast.LENGTH_LONG).show();
                    DataDogLogger.getLogger().e("Devnco_Android Exception - "+expMessage);
                    new Handler(Looper.getMainLooper()).post(
                            new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(context, expMessage, Toast.LENGTH_LONG).show();
                                }});


                    Log.v("Saved Image - ", exp.toString());
                    exp.printStackTrace();
                }




                final Handler handler =  new Handler(Looper.getMainLooper());
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        try {
                int CompletedPageCounter =0;
                String errorPageString ="Printing error in page no. ";
                for(int i=0;i<PrintUtils.jobstatusList.size();i++){
                    JobsModel jobmodel = PrintUtils.jobstatusList.get(i);
                    if(jobmodel.getJobStatus().toLowerCase().contains("completed")){
                        CompletedPageCounter++;
                    }else{

                        errorPageString=errorPageString+jobmodel.getPageNo()+",";
                    }
                }

                if(noOfCopies==CompletedPageCounter){
                    new Handler(Looper.getMainLooper()).post(
                            new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(context, "Successfully All Pages Printed.", Toast.LENGTH_LONG).show();
                                }
                            });
                }else{
                    String errorString=errorPageString;
                    new Handler(Looper.getMainLooper()).post(
                            new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(context, errorString, Toast.LENGTH_LONG).show();
                                }
                            });
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }, 70000);





            }

        }.start();


    }

    private File convertColorToMonochrome(File colorFile) throws IOException {

        String filePath = colorFile.getPath();
        Bitmap bitmap = BitmapFactory.decodeFile(filePath);
        Bitmap mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(mutableBitmap);
        ColorMatrix colorMatrix = new ColorMatrix();
        colorMatrix.setSaturation(0);
        Paint paint = new Paint();
        paint.setColorFilter(new ColorMatrixColorFilter(colorMatrix));
        canvas.drawBitmap(mutableBitmap, 0, 0, paint);
        FileOutputStream out = new FileOutputStream(colorFile);
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
        //      logger.info("Devnco_Android Saved Image - "+ renderFile.getAbsolutePath());
        out.flush();
        out.close();
        return colorFile;
    }
}
