package com.example.customeprintservice.jipp;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.hp.jipp.encoding.IppPacket;
import com.hp.jipp.trans.IppClientTransport;
import com.hp.jipp.trans.IppPacketData;

import java.io.File;
import java.io.FileInputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import static com.hp.jipp.model.Types.documentFormat;
import static com.hp.jipp.model.Types.requestingUserName;

public class PrintUtils {

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
    }};

    public void print(URI uri, File file, Context context) {

        File inputFile = new File(file.getAbsolutePath());
        boolean exists = inputFile.exists();

        Log.i("printer", String.valueOf(exists));

        Log.i("printer", "input File-->" + inputFile);
        String fileName = inputFile.getName();
        String format = inputFile.getName();

        if (fileName.contains(".")) {
            format = extensionTypes.get(fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase());
        }

        // Deliver the print request
        IppPacket printRequest = IppPacket.printJob(uri)
                .putOperationAttributes(
                        requestingUserName.of(CMD_NAME),
                        documentFormat.of(format))
                .build();

        Log.i("printer", "Requesting->" + printRequest.prettyPrint(100, "  "));
        new Thread(() -> {
            try {
                IppPacketData request = new IppPacketData(printRequest, new FileInputStream(inputFile));
                IppPacketData response = transport.sendData(uri, request);

                Intent intent =
                        new Intent("com.example.CUSTOM_INTENT")
                                .putExtra("getMessage", "Response Print Util-->" + response.toString());
                context.sendBroadcast(intent);

                Log.i("printer", "Received ------>>>" + response.getPacket().prettyPrint(100, "  "));
            } catch (Exception e) {
                Intent intent =
                        new Intent("com.example.CUSTOM_INTENT")
                                .putExtra("getMessage", "Response Print Util-->" + e.toString());
                context.sendBroadcast(intent);
            }
        }).start();

    }

}
