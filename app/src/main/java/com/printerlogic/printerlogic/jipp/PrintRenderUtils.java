package com.printerlogic.printerlogic.jipp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.pdf.PdfRenderer;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.widget.Toast;

import com.printerlogic.printerlogic.model.JobsModel;
import com.printerlogic.printerlogic.print.PrintReleaseFragment;
import com.printerlogic.printerlogic.utils.DataDogLogger;
import com.hp.jipp.model.Sides;
import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.pdmodel.PDPageTree;
import com.tom_roush.pdfbox.rendering.PDFRenderer;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
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
                                           // Toast.makeText(context, expMessage, Toast.LENGTH_LONG).show();
                                            DataDogLogger.getLogger().e("Devnco_Android Exceptom - "+ expMessage);
                                        }
                                    });

                            break;
                        } else {

                             map = printUtils.print(finalUri, renderFile, context, "", versionNumber,"","",true, Sides.oneSided);
                            DataDogLogger.getLogger().i("Devnco_Android print status:"+map.get("status").toString());
                            if (map.get("status").equals("server-error-busy")) {
                                Thread.sleep(threadSleepInMilliSecs);
                                totalTimeThreadSleep = totalTimeThreadSleep + threadSleepInMilliSecs;
                                if (totalTimeThreadSleep > timeThreshold) {
                                    String expMessage = "The printer is unresponsive. Aborting ";
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
                    Log.v("Saved Image - ", exp.toString());
                    DataDogLogger.getLogger().i("Devnco_Android Saved Image - "+ expMessage.toString());
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
                                        String errorPageString ="Printing failed on page no. ";
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
                                                            com.printerlogic.printerlogic.MainActivity mainActivity=new com.printerlogic.printerlogic.MainActivity();
                                                            mainActivity.addNotification(context,"Successfully All Pages Printed.");
                                                            Toast.makeText(context, "Successfully All Pages Printed.", Toast.LENGTH_LONG).show();
                                                        }
                                                    });
                                        }else{
                                            String errorString=errorPageString;
                                            com.printerlogic.printerlogic.MainActivity mainActivity=new com.printerlogic.printerlogic.MainActivity();
                                            mainActivity.addNotification(context,errorString);
                                            Toast.makeText(context, errorString, Toast.LENGTH_LONG).show();
                                            DataDogLogger.getLogger().e("Devnco_Android printer:"+ "error in renderPageUsingDefaultPdfRenderer:"+errorString);
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

                        Map map = printUtils.print(finalUri, renderFile, context, "","0x200","","",true,Sides.oneSided);

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


    public void renderPageUsingDefaultPdfRendererForSelectedPages(File file, String printerString, Context context,int startIndex,int endIndex,int noOfCopies, ArrayList<URI> ippUri,int TotalPageCount,boolean isColor,String orientationValue,String paperSize) {
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
                                File renderFile;
                                    String path = "/storage/self/primary/sample" + pagePrintCounter + ".jpg";
                                     renderFile = new File(path);
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
                                    out.flush();
                                    out.close();

                                if(isColor==false){
                                    renderFile = convertColorToMonochromeForPdf(renderFile);
                                }


                                if (!renderFile.exists()) {
                                    String expMessage = "Exception occurred while rendering: file not created ";
                                    DataDogLogger.getLogger().e("Devnco_Android printer:"+ expMessage);

                                    break;
                                } else {
                                    String ippUriFinal = finalUri.toString();

                                     map = printUtils.print(finalUri, renderFile, context, "",versionNumber,orientationValue,paperSize,isColor,Sides.oneSided);
                                    DataDogLogger.getLogger().i("Devnco_Android print status:"+map.get("status").toString());

                                    if (map.get("status").equals("server-error-busy")) {
                                        Thread.sleep(threadSleepInMilliSecs);
                                        totalTimeThreadSleep = totalTimeThreadSleep + threadSleepInMilliSecs;
                                        if (totalTimeThreadSleep > timeThreshold) {
                                            String expMessage = "The printer is unresponsive. Aborting ";
                                            Log.i("printer", expMessage);
                                            DataDogLogger.getLogger().e("Devnco_Android printer:"+ expMessage);
                                            break;
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

                            } else {
                                pagePrintCounter++;
                                totalTimeThreadSleep = 0;
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

                            DataDogLogger.getLogger().e("Devnco_Android printer:"+ " status in renderPageUsingDefaultPdfRendererForSelectedPages:"+resultMap.get("status").toString());
                    }

                } catch (Exception exp) {
                    String expMessage = "Exception occurred while rendering: " + exp.toString();
                    DataDogLogger.getLogger().e("Devnco_Android Exception - "+expMessage);

                    DataDogLogger.getLogger().e("Devnco_Android printer:"+ "Exception in renderPageUsingDefaultPdfRendererForSelectedPages:"+exp.toString());
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
                                            String errorPageString ="Printing failed on page no. ";
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
                                                                com.printerlogic.printerlogic.MainActivity mainActivity=new com.printerlogic.printerlogic.MainActivity();
                                                                mainActivity.addNotification(context,"Successfully All Pages Printed.");
                                                                Toast.makeText(context, "Successfully All Pages Printed.", Toast.LENGTH_LONG).show();
                                                            }
                                                        });
                                            }else{
                                                String errorString=errorPageString;
                                                new Handler(Looper.getMainLooper()).post(
                                                        new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                com.printerlogic.printerlogic.MainActivity mainActivity=new com.printerlogic.printerlogic.MainActivity();
                                                                mainActivity.addNotification(context,errorString);
                                                                Toast.makeText(context, errorString, Toast.LENGTH_LONG).show();
                                                            }
                                                        });
                                                DataDogLogger.getLogger().e("Devnco_Android printer:"+ "Exception in renderPageUsingDefaultPdfRendererForSelectedPages:"+ errorString);
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


    public void printNoOfCOpiesJpgOrPngAndPdfFiles(File file, String printerString, Context context, int noOfCopies, ArrayList<URI> ippUri,boolean isColor,String orientationValue,String paperSize,String sideSupported) {
        PrintUtils.jobstatusList.clear();
        new Thread() {

            public void run()    //Anonymous class overriding run() method of Thread class
            {
                try {
                    URI finalUri = URI.create(printerString);
                    PrintUtils printUtils = new PrintUtils();
                    DataDogLogger.getLogger().i("Devnco_Android print jpg or png - fileName: "+ file.getAbsolutePath());
                    DataDogLogger.getLogger().i("Devnco_Android print jpg or png -host: "+ printerString);
                    DataDogLogger.getLogger().i("Devnco_Android print jpg or png -No of Copies: "+ noOfCopies);
                    DataDogLogger.getLogger().i("Devnco_Android print jpg or png -isColor: "+ isColor);
                    DataDogLogger.getLogger().i("Devnco_Android print jpg or png -orientation: "+ orientationValue);
                    DataDogLogger.getLogger().i("Devnco_Android print jpg or png -paper size: "+ paperSize);
                    DataDogLogger.getLogger().i("Devnco_Android print jpg or png -sides: "+ sideSupported);



                 Map<String, String> resultMap = printUtils.getAttributesCall(ippUri,context);

                    String attributeStatus ="attribute status:"+resultMap.get("status");
                    DataDogLogger.getLogger().i("Devnco_Android "+ attributeStatus);

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
                           if(file.getName().contains(".pdf")){
                                map = printUtils.print(finalUri, file, context, "", versionNumber,orientationValue,paperSize,isColor,sideSupported);
                            }else{
                               if(isColor == true) {
                                   map = printUtils.print(finalUri, file, context, "", versionNumber,orientationValue,paperSize,isColor,sideSupported);
                               }else{
                                   map = printUtils.print(finalUri, convertColorToMonochrome(file), context, "", versionNumber,orientationValue,paperSize,isColor,sideSupported);
                               }
                           }

                            String print ="print status:"+map.get("status").toString();
                            DataDogLogger.getLogger().i("Devnco_Android "+print);

                            if (map.get("status").equals("server-error-busy")) {
                                Thread.sleep(threadSleepInMilliSecs);
                                totalTimeThreadSleep = totalTimeThreadSleep + threadSleepInMilliSecs;
                                if (totalTimeThreadSleep > timeThreshold) {
                                    String expMessage = "The printer is unresponsive. Aborting ";
                                    DataDogLogger.getLogger().e("Devnco_Android printer:"+ "Exception in printNoOfCOpiesJpgOrPngAndPdfFiles:"+expMessage);

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

                        DataDogLogger.getLogger().e("Devnco_Android printer:"+ " printNoOfCOpiesJpgOrPngAndPdfFiles:"+resultMap.get("status").toString());

                    }
                } catch (Exception exp) {
                    String expMessage = "Exception occurred while rendering: " + exp.toString();
                    // Toast.makeText(context, expMessage, Toast.LENGTH_LONG).show();
                    DataDogLogger.getLogger().e("Devnco_Android Exception - "+expMessage);
                    Log.v("Saved Image - ", exp.toString());
                    exp.printStackTrace();
                }




                final Handler handler =  new Handler(Looper.getMainLooper());
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        try {
                int CompletedPageCounter =0;
                String errorPageString ="Printing failed on page no. ";
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
                                    com.printerlogic.printerlogic.MainActivity mainActivity=new com.printerlogic.printerlogic.MainActivity();
                                    mainActivity.addNotification(context,"Successfully All Pages Printed.");
                                    Toast.makeText(context, "Successfully All Pages Printed.", Toast.LENGTH_LONG).show();
                                }
                            });
                }else{
                    String errorString=errorPageString;
                    new Handler(Looper.getMainLooper()).post(
                            new Runnable() {
                                @Override
                                public void run() {
                                    com.printerlogic.printerlogic.MainActivity mainActivity=new com.printerlogic.printerlogic.MainActivity();
                                    mainActivity.addNotification(context,errorString);
                                    Toast.makeText(context, errorString, Toast.LENGTH_LONG).show();
                                }
                            });
                    DataDogLogger.getLogger().e("Devnco_Android printer:" + errorString);
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
        int width, height;
        height = bitmap.getHeight();
        width = bitmap.getWidth();

        Bitmap bmpGrayscale = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bmpGrayscale);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0);
        ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
        paint.setColorFilter(f);
        c.drawBitmap(bitmap, 0, 0, paint);
        //return bmpGrayscale;

        String path = "/storage/self/primary/sample" + " monochrome" + ".jpg";
        File renderFile = new File(path);
        FileOutputStream out = new FileOutputStream(renderFile);
        bmpGrayscale.compress(Bitmap.CompressFormat.JPEG, 100, out);

        DataDogLogger.getLogger().i("Devnco_Android monochrome Image - "+ renderFile.getAbsolutePath());
        Log.i("monochrome Image-", renderFile.getAbsolutePath());
        out.flush();
        out.close();
        return renderFile;
    }


    private File convertColorToMonochromeForPdf(File colorFile) throws IOException {

        String filePath = colorFile.getPath();
        Bitmap bitmap = BitmapFactory.decodeFile(filePath);
        int width, height;
        height = bitmap.getHeight();
        width = bitmap.getWidth();

        Bitmap bmpGrayscale = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bmpGrayscale);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0);
        ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
        paint.setColorFilter(f);
        c.drawBitmap(bitmap, 0, 0, paint);
        //return bmpGrayscale;

        String path = colorFile.getPath();
        File renderFile = new File(path);
        FileOutputStream out = new FileOutputStream(renderFile);
        bmpGrayscale.compress(Bitmap.CompressFormat.JPEG, 100, out);

        DataDogLogger.getLogger().i("Devnco_Android monochrome Image - "+ renderFile.getAbsolutePath());
        Log.i("monochrome Image-", renderFile.getAbsolutePath());
        out.flush();
        out.close();
        return renderFile;
    }


    public void renderPageUsingDefaultPdfRendererForSelectedPagesForTwoSidedPrint(File file, String printerString, Context context,int startIndex,int endIndex,int noOfCopies, ArrayList<URI> ippUri,int TotalPageCount,boolean isColor,String orientationValue,String paperSize) {
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
                            boolean isOddFlag=false;
                            if(startIndexOfPage  % 2 != 0){
                                isOddFlag=true;
                            }
                            int counter = 0;
                            while (pagePrintCounter < pageCount) {
                                Map<String, String> map = null;
                                if (startIndexOfPage <= pagePrintCounter && endIndexOfPage >= pagePrintCounter) {

                                    if(isOddFlag==false){
                                        if ( pagePrintCounter % 2 == 0 ) {
                                            System.out.println("print even page::"+pagePrintCounter);

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
                                            out.flush();
                                            out.close();

                                            if(isColor==false){
                                                renderFile = convertColorToMonochromeForPdf(renderFile);
                                            }

                                            if (!renderFile.exists()) {
                                                String expMessage = "Exception occurred while rendering: file not created ";
                                                DataDogLogger.getLogger().e("Devnco_Android printer:" + expMessage);

                                                break;
                                            } else {
                                                String ippUriFinal = finalUri.toString();

                                                map = printUtils.print(finalUri, renderFile, context, "", versionNumber, orientationValue, paperSize,isColor,Sides.oneSided);
                                                DataDogLogger.getLogger().i("Devnco_Android print status:" + map.get("status").toString());

                                                if (map.get("status").equals("server-error-busy")) {
                                                    Thread.sleep(threadSleepInMilliSecs);
                                                    totalTimeThreadSleep = totalTimeThreadSleep + threadSleepInMilliSecs;
                                                    if (totalTimeThreadSleep > timeThreshold) {
                                                        String expMessage = "The printer is unresponsive. Aborting ";
                                                        Log.i("printer", expMessage);
                                                        DataDogLogger.getLogger().e("Devnco_Android printer:" + expMessage);
                                                        break;
                                                    }
                                                } else {
                                                  //  pagePrintCounter++;
                                                    totalTimeThreadSleep = 0;
                                                }
                                                String jobIdString = map.get("jobId");
                                                URI uri = finalUri;
                                                int pageCountNo = pagePrintCounter;
                                                JobsModel jobModel = new JobsModel();

                                                if (jobIdString != null) {
                                                    int jobId = Integer.parseInt(jobIdString);
                                                    jobModel.setJobId(jobId);
                                                    jobModel.setUsedUri(uri);
                                                    jobModel.setPageNo(pageCountNo);
                                                    jobIdList.add(jobModel);
                                                }
                                            }

                                        }
                                    }
                                  //  if(isOddFlag==true){
                                    else{
                                        if(pagePrintCounter % 2 != 0) {
                                            System.out.println("print odd page::" + pagePrintCounter);

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
                                            out.flush();
                                            out.close();

                                            if(isColor==false){
                                                renderFile = convertColorToMonochromeForPdf(renderFile);
                                            }


                                            if (!renderFile.exists()) {
                                                String expMessage = "Exception occurred while rendering: file not created ";

                                                DataDogLogger.getLogger().e("Devnco_Android printer:" + expMessage);

                                                break;
                                            } else {
                                                String ippUriFinal = finalUri.toString();

                                                map = printUtils.print(finalUri, renderFile, context, "", versionNumber, orientationValue, paperSize,isColor,Sides.oneSided);
                                                DataDogLogger.getLogger().i("Devnco_Android print status:" + map.get("status").toString());

                                                if (map.get("status").equals("server-error-busy")) {
                                                    Thread.sleep(threadSleepInMilliSecs);
                                                    totalTimeThreadSleep = totalTimeThreadSleep + threadSleepInMilliSecs;
                                                    if (totalTimeThreadSleep > timeThreshold) {
                                                        String expMessage = "The printer is unresponsive. Aborting ";
                                                        Log.i("printer", expMessage);
                                                        DataDogLogger.getLogger().e("Devnco_Android printer:" + expMessage);
                                                        break;
                                                    }
                                                } else {
                                                   // pagePrintCounter++;
                                                    totalTimeThreadSleep = 0;
                                                }
                                                String jobIdString = map.get("jobId");
                                                URI uri = finalUri;
                                                int pageCountNo = pagePrintCounter;
                                                JobsModel jobModel = new JobsModel();

                                                if (jobIdString != null) {
                                                    int jobId = Integer.parseInt(jobIdString);
                                                    jobModel.setJobId(jobId);
                                                    jobModel.setUsedUri(uri);
                                                    jobModel.setPageNo(pageCountNo);
                                                    jobIdList.add(jobModel);
                                                }
                                            }


                                        }}

                                    if(totalTimeThreadSleep ==0) {
                                        if (pagePrintCounter == pageCount || pagePrintCounter == endIndexOfPage) {
                                            if (counter > 0) {
                                                break;
                                            }
                                            if (startIndexOfPage % 2 != 0) {
                                                isOddFlag = false;
                                            } else {
                                                isOddFlag = true;
                                            }
                                            counter++;
                                            Thread.sleep(30000);
                                            ToneGenerator toneGen1 = new ToneGenerator(AudioManager.STREAM_RING, 100);
                                            toneGen1.startTone(ToneGenerator.TONE_CDMA_PIP,550);
                                            Thread.sleep(20000);
                                            pagePrintCounter = 0;
                                        }
                                        pagePrintCounter++;
                                    }
                                } else {
                                    pagePrintCounter++;
                                    totalTimeThreadSleep = 0;
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

                        DataDogLogger.getLogger().e("Devnco_Android  - "+resultMap.get("status").toString());
                    }

                } catch (Exception exp) {
                    String expMessage = "Exception occurred while rendering: " + exp.toString();
                    DataDogLogger.getLogger().e("Devnco_Android Exception - "+expMessage);

                    Log.v("Saved Image - ", exp.toString());

                    exp.printStackTrace();
                }
            /*    Log.d("print done ", "print all pages done ");
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
                                                            //Toast.makeText(context, "Successfully All Pages Printed.", Toast.LENGTH_LONG).show();
                                                        }
                                                    });
                                        }else{
                                            String errorString=errorPageString;
                                            new Handler(Looper.getMainLooper()).post(
                                                    new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            //Toast.makeText(context, errorString, Toast.LENGTH_LONG).show();
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


*/

            }
        }.start();

    }

}
