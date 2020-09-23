package com.example.customeprintservice.jipp;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.hp.jipp.encoding.IppPacket;
import com.hp.jipp.trans.IppClientTransport;
import com.hp.jipp.trans.IppPacketData;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.hp.jipp.model.Types.documentFormat;
import static com.hp.jipp.model.Types.requestingUserName;

public class PrintUtils {

    private Context context = null;

    private final static String FORMAT_PDF = "application/pdf";
    private final static IppClientTransport transport = new HttpIppClientTransport();
    private final static String CMD_NAME = "jprint";
    private final static Map<String, String> extensionTypes = new HashMap<String, String>() {{
        put("pdf", FORMAT_PDF);
        put("pclm", "application/PCLm");
        put("pwg", "image/pwg-raster");
        put("jpeg", "image/jpeg");
        put("jpg", "image/jpeg");
        put("png", "application/image");
        put("xpdf", "application/vnd.adobe.xfdf");
        put("csv", "text/csv");
        put("xls", "application/vnd.ms-excel");
        put("xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        put("pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation");
        put("docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        put("dotx", "application/vnd.openxmlformats-officedocument.wordprocessingml.template");
    }};


    public void setContextAndInitializeJMDNS(Context context)
    {
        this.context = context;
        ExecutorService executor = Executors.newFixedThreadPool(1);
        /*JMDnsUtils dnsUtil = new JMDnsUtils();
        dnsUtil.setContext(context);
        executor.execute(dnsUtil);*/
        NSDUtils nsdUtils = new NSDUtils();
        nsdUtils.setContext(context);
        executor.execute(nsdUtils);
    }


    public void print(URI uri, File file, Context context) {

        File inputFile = new File(file.getAbsolutePath());
        boolean exists = inputFile.exists();

        Log.i("printer", String.valueOf(exists));

        Log.i("printer", "input File-->" + inputFile);
        String fileName = inputFile.getName();
        String format = inputFile.getName();

        if (fileName.contains(".")) {
            format = extensionTypes.get(fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase());
            Log.i("printer", "format--->" + format);
        }

        try {
            AttributesUtils attributesUtils = new AttributesUtils();
            List<String> att = attributesUtils.getAttributesForPrintUtils(uri);

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            }

            Log.i("printer", "att array--> " + att.toString());

            if (format != null && att.contains(format)) {
                IppPacket printRequest = IppPacket.printJob(uri)
                        .putOperationAttributes(
                                requestingUserName.of(CMD_NAME),
                                documentFormat.of(format))
                        .build();                Log.i("printer", "Requesting->" + printRequest.prettyPrint(100, "  "));
                new Thread(() -> {
                    try {
                        Log.i("printer", "In print utils method");
                        IppPacketData request = new IppPacketData(printRequest, new FileInputStream(inputFile));
                        IppPacketData response = transport.sendData(uri, request);
                        Intent intent =
                                new Intent("com.example.CUSTOM_INTENT")
                                        .putExtra("getMessage", response.toString());
                        context.sendBroadcast(intent);
                        Log.i("printer", "Received ------>>>" + response.getPacket().prettyPrint(100, "  "));
                    } catch (Exception e) {
                        Intent intent =
                                new Intent("com.example.CUSTOM_INTENT")
                                        .putExtra("getMessage", e.toString());
                        context.sendBroadcast(intent);
                    }
                }).start();

            } else {
                Toast toast = Toast.makeText(context, "File format is not supported", Toast.LENGTH_LONG);
                toast.show();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
