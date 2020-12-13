package com.example.customeprintservice.jipp;

import android.content.Context;
import android.graphics.Bitmap;

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

public class PrintRenderUtils {
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
                            pageImage =
                                    renderer.renderImage(pagePrintCounter, 1F, Bitmap.Config.RGB_565);
                            String path = "/storage/self/primary/sample"+ pagePrintCounter+".jpg";
                            File renderFile = new File(path);
                            FileOutputStream fileOut = new FileOutputStream(renderFile);
                            long bytes = pageImage.getByteCount();
                            boolean result = pageImage.compress(Bitmap.CompressFormat.JPEG, 100, fileOut);
                            fileOut.close();
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
