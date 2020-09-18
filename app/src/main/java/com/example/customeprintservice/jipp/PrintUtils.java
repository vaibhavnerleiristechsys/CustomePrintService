package com.example.customeprintservice.jipp;

import android.util.Log;

import com.hp.jipp.encoding.IppPacket;
import com.hp.jipp.trans.IppClientTransport;
import com.hp.jipp.trans.IppPacketData;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
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

    public void print(URI uri, File file) {

        File inputFile = new File(file.getAbsolutePath());
        boolean exists = inputFile.exists();

        Log.i("printer", String.valueOf(exists));

        Log.i("printer", "input File-->" + inputFile);
        String fileName = inputFile.getName();
        String format = inputFile.getName();
        //if (format == null) {
            if (fileName.contains(".")) {
                format = extensionTypes.get(fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase());
            }
        //}
        // Deliver the print request
        IppPacket printRequest = IppPacket.printJob(uri)
                .putOperationAttributes(
                        requestingUserName.of(CMD_NAME),
                        documentFormat.of(format))
                .build();

        Log.i("printer", "Requesting->" + printRequest.prettyPrint(100, "  "));
        new Thread(() -> {
            try {
                Log.i("printer","In print utils method");
                IppPacketData request = new IppPacketData(printRequest, new FileInputStream(inputFile));
                IppPacketData response = transport.sendData(uri, request);
                Log.i("printer", "Received ------>>>" + response.getPacket().prettyPrint(100, "  "));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            catch (Exception e) {
            e.printStackTrace();
            }
        }).start();

    }

}
