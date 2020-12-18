package com.example.customeprintservice.jipp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.pdf.PdfRenderer;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.pdmodel.PDPageTree;
import com.tom_roush.pdfbox.rendering.PDFRenderer;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Map;

import static android.os.ParcelFileDescriptor.MODE_READ_ONLY;

public class PrintRenderUtils {


    public void renderPageUsingDefaultPdfRenderer(File file, String printerString, Context context)
    {
        new Thread()
        {

            public void run()	//Anonymous class overriding run() method of Thread class
            {

                try {

                    ParcelFileDescriptor fileDescriptor = ParcelFileDescriptor.open(file, MODE_READ_ONLY);
                    PdfRenderer renderer = new PdfRenderer(fileDescriptor);
                    final int pageCount = renderer.getPageCount();
                    /*for (int i = 0; i < pageCount; i++) {
                        PdfRenderer.Page page = renderer.openPage(i);
                        Bitmap bitmap = Bitmap.createBitmap(page.getWidth(), page.getHeight(),Bitmap.Config.ARGB_8888);
                        Canvas canvas = new Canvas(bitmap);
                        canvas.drawColor(Color.WHITE);
                        canvas.drawBitmap(bitmap, 0, 0, null);
                        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
                        page.close();



                        //String root = Environment.getExternalStorageDirectory().toString();
                        //File file = new File(root + filename + ".png");

                        if (file.exists()) file.delete();
                        try {
                            FileOutputStream out = new FileOutputStream(file);
                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                            Log.v("Saved Image - ", file.getAbsolutePath());
                            out.flush();
                            out.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }*/


                    URI finalUri = URI.create(printerString);


                    PrintUtils printUtils = new PrintUtils();
                    Bitmap pageImage = null;
                    int pagePrintCounter = 0;

                    while (pagePrintCounter < pageCount) {
                        String path = "/storage/self/primary/sample"+ pagePrintCounter+".jpg";
                        File renderFile = new File(path);

                        //if(!renderFile.exists()) {
                            // make sure that do all this stuff if only the file hasnt been created
                            PdfRenderer.Page page = renderer.openPage(pagePrintCounter);
                            Bitmap bitmap = Bitmap.createBitmap(page.getWidth(), page.getHeight(),Bitmap.Config.ARGB_8888);
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


                        //}

                        Map map = printUtils.print(finalUri, renderFile, context, "");

                        if (map.get("status") == "server-error-busy") {
                            Thread.sleep(5000);
                        } else {
                            pagePrintCounter++;
                        }
                    }
                }
                catch(Exception exp)
                {
                    exp.printStackTrace();
                }
            }

        }.start();



    }


    public void renderPage(File file, String printerString, Context context)
    {
            new Thread()
            {

                public void run()	//Anonymous class overriding run() method of Thread class
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
                            String path = "/storage/self/primary/sample"+ pagePrintCounter+".jpg";
                            File renderFile = new File(path);

                            if(!renderFile.exists()) {
                                // make sure that do all this stuff if only the file hasnt been created
                                pageImage =
                                        renderer.renderImage(pagePrintCounter, 1F, Bitmap.Config.RGB_565);

                                FileOutputStream fileOut = new FileOutputStream(renderFile);
                                long bytes = pageImage.getByteCount();
                                boolean result = pageImage.compress(Bitmap.CompressFormat.JPEG, 100, fileOut);
                                fileOut.close();
                            }

                            Map map = printUtils.print(finalUri, renderFile, context, "");

                            if (map.get("status") == "server-error-busy") {
                                Thread.sleep(5000);
                            } else {
                                pagePrintCounter++;
                            }
                        }
                    }
                    catch(Exception exp)
                    {
                        exp.printStackTrace();
                    }
                }

            }.start();



    }
}