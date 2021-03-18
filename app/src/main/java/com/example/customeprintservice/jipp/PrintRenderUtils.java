package com.example.customeprintservice.jipp;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.pdf.PdfRenderer;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.widget.Toast;

import com.example.customeprintservice.print.PrintReleaseFragment;
import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.pdmodel.PDPageTree;
import com.tom_roush.pdfbox.rendering.PDFRenderer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Map;

import static android.os.ParcelFileDescriptor.MODE_READ_ONLY;

public class PrintRenderUtils {
    Logger logger = LoggerFactory.getLogger(PrintUtils.class);

    public void renderPageUsingDefaultPdfRenderer(File file, String printerString, Context context,String hostAddress) {
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

                    int pagePrintCounter = 0;
                    int threadSleepInMilliSecs = 3000;
                    int timeThreshold = threadSleepInMilliSecs * 40;
                    int totalTimeThreadSleep = 0;

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
                        logger.info("Devnco_Android Saved Image - "+ renderFile.getAbsolutePath());
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

                            Map map = printUtils.print(finalUri, renderFile, context, "", versionNumber);


                            if (map.get("status") == null || map.get("status").equals("getAttributefailed")) {
                                String expMessage = "The get attributes call failed ";
                                new Handler(Looper.getMainLooper()).post(
                                        new Runnable() {
                                            @Override
                                            public void run() {
                                                Toast.makeText(context, expMessage, Toast.LENGTH_LONG).show();
                                            }
                                        });

                                Log.i("printer", expMessage);
                                logger.info("Devnco_Android printer"+ expMessage);
                                break;
                            }

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
                                    logger.info("Devnco_Android printer:"+ expMessage);
                                    break;
                                }
                            } else {
                                pagePrintCounter++;
                                totalTimeThreadSleep = 0;
                            }
                        }
                    }

                    Log.v("Saved Image - ", "page print counter: " + pagePrintCounter);
                    logger.info("Devnco_Android Saved Image - "+ "page print counter: " + pagePrintCounter);
                    PrintReleaseFragment printReleaseFrament = new PrintReleaseFragment();
                    printReleaseFrament.sendMetaData(context);
                }
                } catch (Exception exp) {
                    String expMessage = "Exception occurred while rendering: " + exp.toString();
                   // Toast.makeText(context, expMessage, Toast.LENGTH_LONG).show();
                    new Handler(Looper.getMainLooper()).post(
                            new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(context, expMessage, Toast.LENGTH_LONG).show();
                                }});


                    Log.v("Saved Image - ", exp.toString());
                    logger.info("Devnco_Android Saved Image - "+ exp.toString());
                    exp.printStackTrace();
                }

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

                        Map map = printUtils.print(finalUri, renderFile, context, "","0x200");

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


    public void renderPageUsingDefaultPdfRendererForSelectedPages(File file, String printerString, Context context,int startIndex,int endIndex,int noOfCopies, ArrayList<URI> ippUri) {
        new Thread() {

            public void run()    //Anonymous class overriding run() method of Thread class
            {

                try {

                    Log.d("startIndex", String.valueOf(startIndex));
                    logger.info("Devnco_Android startIndex"+ String.valueOf(startIndex));
                    Log.d("endIndex", String.valueOf(endIndex));
                    logger.info("Devnco_Android endIndex:"+ String.valueOf(endIndex));

                    ParcelFileDescriptor fileDescriptor = ParcelFileDescriptor.open(file, MODE_READ_ONLY);
                    PdfRenderer renderer = new PdfRenderer(fileDescriptor);
                    final int pageCount = renderer.getPageCount();
                    URI finalUri = URI.create(printerString);
                    PrintUtils printUtils = new PrintUtils();
                    Bitmap pageImage = null;
                    Map<String, String> resultMap = printUtils.getAttributesCall(ippUri,context);
                      String attributeStatus ="attrribute status:"+resultMap.get("status");
                    logger.info("Devnco_Android attrribute status:"+ attributeStatus);

                    new Handler(Looper.getMainLooper()).post(
                            new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(context, attributeStatus, Toast.LENGTH_LONG).show();
                                }
                            });



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
                        logger.info("Devnco_Android status successful-ok");
                        finalUri = URI.create(resultMap.get("finalUri"));
                        String versionNumber =resultMap.get("versionNumber");
                       // int versionNumber =Integer.parseInt(versionNo);
                    for (int i = 0; i < noOfCopies; i++) {
                        int pagePrintCounter = 0;
                        int threadSleepInMilliSecs = 3000;
                        int timeThreshold = threadSleepInMilliSecs * 40;
                        int totalTimeThreadSleep = 0;
                        int startIndexOfPage = startIndex - 1;
                        int endIndexOfPage = endIndex - 1;
                        int counter = 0;
                        while (pagePrintCounter < pageCount) {
                            if (startIndexOfPage <= pagePrintCounter && endIndexOfPage >= pagePrintCounter) {

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
                                logger.info("Devnco_Android Saved Image - "+ renderFile.getAbsolutePath());
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
                                    new Handler(Looper.getMainLooper()).post(
                                            new Runnable() {
                                                @Override
                                                public void run() {
                                                    Toast.makeText(context, ippUriFinal, Toast.LENGTH_LONG).show();
                                                }
                                            });


                                    Map map = printUtils.print(finalUri, renderFile, context, "",versionNumber);
                                    String print ="print status:"+map.get("status").toString();
                                    logger.info("Devnco_Android print status:"+map.get("status").toString());
                                    new Handler(Looper.getMainLooper()).post(
                                            new Runnable() {
                                                @Override
                                                public void run() {
                                                    Toast.makeText(context, print, Toast.LENGTH_LONG).show();
                                                }
                                            });


                                    String exception = (String) map.get("Exception");
                                    new Handler(Looper.getMainLooper()).post(
                                            new Runnable() {
                                                @Override
                                                public void run() {
                                                    Toast.makeText(context, exception, Toast.LENGTH_LONG).show();
                                                }
                                            });

                                  /*  if (map.get("status") == null || map.get("status").equals("getAttributefailed")) {
                                        String expMessage = "The get attributes call failed ";
                                        new Handler(Looper.getMainLooper()).post(
                                                new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        Toast.makeText(context, expMessage, Toast.LENGTH_LONG).show();
                                                    }
                                                });

                                        Log.i("printer", expMessage);
                                        logger.info("printer:"+ expMessage);

                                      /*  if (counter < 15) {
                                            finalUri = ippUri.get(counter);
                                            counter++;
                                            continue;
                                        } else {
                                            counter = 0;
                                            break;
                                        }

                                    }*/

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
                                            logger.info("Devnco_Android printer:"+ expMessage);
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
                        }
                        Log.v("Saved Image - ", "page print counter: " + pagePrintCounter);
                        logger.info("Devnco_Android Saved Image - "+ "page print counter: " + pagePrintCounter);
                    }
                        PrintReleaseFragment printReleaseFrament =new PrintReleaseFragment();
                        printReleaseFrament.sendMetaData(context);
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
                    new Handler(Looper.getMainLooper()).post(
                            new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(context, expMessage, Toast.LENGTH_LONG).show();
                                }});


                    Log.v("Saved Image - ", exp.toString());
                    logger.info("Devnco_Android Saved Image - "+ exp.toString());
                    exp.printStackTrace();
                }

            }

        }.start();


    }


    public void printNoOfCOpiesJpgOrPngFiles(File file, String printerString, Context context, int noOfCopies, ArrayList<URI> ippUri) {
        new Thread() {

            public void run()    //Anonymous class overriding run() method of Thread class
            {
                try {
                    URI finalUri = URI.create(printerString);
                    PrintUtils printUtils = new PrintUtils();
                    Map<String, String> resultMap = printUtils.getAttributesCall(ippUri,context);

                    String attributeStatus ="attrribute status:"+resultMap.get("status");
                    logger.info("Devnco_Android attrribute status:"+ attributeStatus);
                    new Handler(Looper.getMainLooper()).post(
                            new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(context, attributeStatus, Toast.LENGTH_LONG).show();
                                }
                            });


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
                        logger.info("Devnco_Android status successful-ok");
                        finalUri = URI.create(resultMap.get("finalUri"));
                        String versionNumber =resultMap.get("versionNumber");

                    for (int i = 0; i < noOfCopies; i++) {
                        int pagePrintCounter = 0;
                        int threadSleepInMilliSecs = 3000;
                        int timeThreshold = threadSleepInMilliSecs * 40;
                        int totalTimeThreadSleep = 0;
                        while (pagePrintCounter < 1) {
                            String ippUriFinal = finalUri.toString();
                            new Handler(Looper.getMainLooper()).post(
                                    new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(context, ippUriFinal, Toast.LENGTH_LONG).show();
                                        }
                                    });

                            Map map = printUtils.print(finalUri, file, context, "",versionNumber);
                            String print ="print status:"+map.get("status").toString();
                            logger.info("Devnco_Android print status:"+map.get("status").toString());
                            new Handler(Looper.getMainLooper()).post(
                                    new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(context, print, Toast.LENGTH_LONG).show();
                                        }
                                    });
                            String exception = (String) map.get("Exception");
                          /*  new Handler(Looper.getMainLooper()).post(
                                    new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(context, exception, Toast.LENGTH_LONG).show();
                                        }
                                    });*/


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


                        }
                        Log.v("Saved Image - ", "page print counter: " + pagePrintCounter);
                    }
                        PrintReleaseFragment printReleaseFrament =new PrintReleaseFragment();
                        printReleaseFrament.sendMetaData(context);
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
                    new Handler(Looper.getMainLooper()).post(
                            new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(context, expMessage, Toast.LENGTH_LONG).show();
                                }});


                    Log.v("Saved Image - ", exp.toString());
                    exp.printStackTrace();
                }

            }

        }.start();


    }
}
