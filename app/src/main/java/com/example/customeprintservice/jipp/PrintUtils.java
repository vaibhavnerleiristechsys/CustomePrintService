package com.example.customeprintservice.jipp;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.hp.jipp.encoding.Attribute;
import com.hp.jipp.encoding.AttributeGroup;
import com.hp.jipp.encoding.IppPacket;
import com.hp.jipp.encoding.OtherString;
import com.hp.jipp.encoding.ValueTag;
import com.hp.jipp.trans.IppClientTransport;
import com.hp.jipp.trans.IppPacketData;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.hp.jipp.model.Types.documentFormat;
import static com.hp.jipp.model.Types.requestedAttributes;
import static com.hp.jipp.model.Types.requestingUserName;

public class PrintUtils {

    private final static String FORMAT_PDF = "application/pdf";
    protected final static Map<String, String> extensionTypes = new HashMap<String, String>() {{
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
    private final static IppClientTransport transport = new HttpIppClientTransport();
    private final static String CMD_NAME = "jprint";
    private Context context = null;

    public void setContextAndInitializeJMDNS(Context context) {
        this.context = context;
        ExecutorService executor = Executors.newFixedThreadPool(1);
        /*JMDnsUtils dnsUtil = new JMDnsUtils();
        dnsUtil.setContext(context);
        executor.execute(dnsUtil);*/
        NSDUtils nsdUtils = new NSDUtils();
        nsdUtils.setContext(context);
        executor.execute(nsdUtils);
    }

    public void print(URI uri, File file, Context context,String fileFormat) {

        new Thread(() -> {
            try {
                File inputFile = new File(file.getAbsolutePath());
                boolean exists = inputFile.exists();
                Log.i("printer", String.valueOf(exists));
                Log.i("printer", "input File-->" + inputFile);
                String fileName = inputFile.getName();
                String format = inputFile.getName();

                if (fileName.contains(".")) {
                    format = extensionTypes.get(fileName.substring(fileName.lastIndexOf(".") + 1));
                    Log.i("printer", "format--->" + format.toLowerCase().trim());
                }

                List<String> att = getPrinterSupportedFormats(uri, context);
                Log.i("printer", "att array--> " + att.toString());

                if (format != null && att.contains(format.toLowerCase().trim())) {
                    IppPacket printRequest = IppPacket.printJob(uri)
                            .putOperationAttributes(
                                    requestingUserName.of(CMD_NAME),
                                    documentFormat.of(format))
                            .build();
                    Log.i("printer", "Requesting->" + printRequest.prettyPrint(100, "  "));

                    Log.i("printer", "In print utils method");
                    IppPacketData request = new IppPacketData(printRequest, new FileInputStream(inputFile));
                    IppPacketData printResponse = transport.sendData(uri, request);
                    Intent printResponseIntent =
                            new Intent("com.example.PRINT_RESPONSE")
                                    .putExtra("printResponse", printResponse.toString());
                    context.sendBroadcast(printResponseIntent);

                    Log.i("printer", "Received ------>>>" + printResponse.getPacket().prettyPrint(100, "  "));
                }else{
                    Intent fileNotSupported =
                            new Intent("com.example.PRINT_RESPONSE")
                                    .putExtra("fileNotSupported", "File Format is not supported");
                    context.sendBroadcast(fileNotSupported);
                }
            } catch (Exception e) {
                Intent intent =
                        new Intent("com.example.PRINT_RESPONSE")
                                .putExtra("getPrintResponse", e.toString());
                context.sendBroadcast(intent);
            }
        }).start();
    }

    private List<String> getPrinterSupportedFormats(URI uri, Context context) throws
            IOException {
        List<String> attributeList = null;
        try {
            attributeList = new ArrayList<>();
            Attribute<String> requested;
            requested = requestedAttributes.of("all");
            IppPacket attributeRequest =
                    IppPacket.getPrinterAttributes(uri)
                            .putOperationAttributes(requestingUserName.of("print"), requested)
                            .build();

            IppPacketData request = new IppPacketData(attributeRequest);

            IppPacketData response = transport.sendData(uri, request);
            IppPacket responsePacket = response.getPacket();

            Intent intent =
                    new Intent("com.example.PRINT_RESPONSE")
                            .putExtra("getPrinterAttributes", response.toString());
            context.sendBroadcast(intent);

            List<AttributeGroup> attributeGroupList = responsePacket.getAttributeGroups();

            for (AttributeGroup attributeGroup : attributeGroupList) {
                if (attributeGroup.get("document-format-supported") != null) {
                    Log.i("printer", "attribute groups-->" + attributeGroup.get("document-format-supported"));
                    Attribute attribute = attributeGroup.get("document-format-supported");
                    for (int i = 0; i < attribute.size(); i++) {
                        Object att = attribute.get(i);
                        if (att instanceof OtherString) {
                            OtherString attOtherString = (OtherString) att;
                            ValueTag valueTag = attOtherString.getTag();
                            String tagName = valueTag.getName();
                            String tagValue = attOtherString.getValue().trim().toLowerCase();
                            attributeList.add(tagValue);
                        }
                        Log.i("printer", "Format: " + i + " " + att);
                    }
                }
            }

            Log.i("printer", "attribute list in print utils->>" + attributeList);
            Intent printerSupportedFormatsIntent =
                    new Intent("com.example.PRINT_RESPONSE")
                            .putExtra("printerSupportedFormats", attributeList.toString());
            context.sendBroadcast(printerSupportedFormatsIntent);
            return attributeList;

        } catch (Exception e) {

            Intent printerSupportedFormatsIntent =
                    new Intent("com.example.PRINT_RESPONSE")
                            .putExtra("exception", e.toString());
            context.sendBroadcast(printerSupportedFormatsIntent);
            Log.i("printer", "exception message-->" + e.toString());

        }
        return attributeList;
    }
}
