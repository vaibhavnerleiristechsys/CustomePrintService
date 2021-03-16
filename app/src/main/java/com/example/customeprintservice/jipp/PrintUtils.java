package com.example.customeprintservice.jipp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;
import com.example.customeprintservice.MainActivity;
import com.example.customeprintservice.print.PrintPreview;
import com.example.customeprintservice.print.PrintReleaseFragment;
import com.example.customeprintservice.print.ServerPrintRelaseFragment;
import com.hp.jipp.encoding.Attribute;
import com.hp.jipp.encoding.AttributeGroup;
import com.hp.jipp.encoding.IppPacket;
import com.hp.jipp.encoding.OtherString;
import com.hp.jipp.encoding.ValueTag;
import com.hp.jipp.model.Operation;
import com.hp.jipp.model.Status;
import com.hp.jipp.trans.IppClientTransport;
import com.hp.jipp.trans.IppPacketData;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
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
        put("pcl", "application/PCLm");
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
    Logger logger = LoggerFactory.getLogger(PrintUtils.class);

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

    void debugString(String str, String key) {

        int strLength = str.length();
        int strLengthMod = strLength % 100;
        Bundle bundle = new Bundle();

        //55

        for (int i = 0; i < strLengthMod; i++) {
            //655
            String subStr = str.substring(i * 100, i * 100 + 99);
            bundle.putString(key + i, subStr);
        }

        int remainingChars = strLength - strLengthMod * 100;

        String remainingSubstring = str.substring(strLengthMod * 100, strLengthMod * 100 + remainingChars - 1);
        bundle.putString(key + "remaining", remainingSubstring);

        //bundle.putString("key","value");
    }


    public void getSpecificPrinterAttributes(URI uri, List<String> attributeList) {
        new Thread(() -> {
            try {
                Attribute<String> requestedAttributeList;
                requestedAttributeList = requestedAttributes.of(attributeList);
                IppPacket getSpecificPrinterAttributesRequestPacket =
                        IppPacket.getPrinterAttributes(uri)
                                .putOperationAttributes(requestingUserName.of("print"), requestedAttributeList)
                                .build();

                IppPacketData getSpecificPrinterAttributesRequestPacketData = new IppPacketData(getSpecificPrinterAttributesRequestPacket);

                IppPacketData getSpecificPrinterAttributesPacketDataResponse = transport.sendData(uri, getSpecificPrinterAttributesRequestPacketData);
                IppPacket getSpecificPrinterAttributesResponsePacket = getSpecificPrinterAttributesPacketDataResponse.getPacket();

                Intent getSpecificPrinterAttributesIntent =
                        new Intent("com.example.PRINT_RESPONSE")
                                .putExtra("getSpecificPrinterAttributesIntent", getSpecificPrinterAttributesResponsePacket.toString());
                context.sendBroadcast(getSpecificPrinterAttributesIntent);
            } catch (Exception e) {
                Intent getSpecificPrinterAttributesIntent =
                        new Intent("com.example.PRINT_RESPONSE")
                                .putExtra("getSpecificPrinterAttributesIntent", e.toString());
                context.sendBroadcast(getSpecificPrinterAttributesIntent);
            }

        }).start();
    }

    public void cancelJob(URI uri, int jobId, Context context) throws IOException {

        new Thread(() -> {

            try {
                IppPacket cancelJobRequestPacket = IppPacket.cancelJob(uri, jobId).build();
                IppPacketData cancelJobRequestPacketData = new IppPacketData(cancelJobRequestPacket);
                IppPacketData cancelJobResponsePacketData = transport.sendData(uri, cancelJobRequestPacketData);
                IppPacket cancelJobResponsePacket = cancelJobResponsePacketData.getPacket();

                getResponseDetails(cancelJobResponsePacket);

                Intent cancelJobIntent =
                        new Intent("com.example.PRINT_RESPONSE")
                                .putExtra("cancelJobIntent", cancelJobResponsePacket.toString());
                context.sendBroadcast(cancelJobIntent);
            } catch (Exception ex) {
                Intent intent =
                        new Intent("com.example.PRINT_RESPONSE")
                                .putExtra("cancelJobIntent", ex.toString());
                context.sendBroadcast(intent);
            }
        }).start();

    }

    public void getJobs(URI uri, Context context) throws IOException {

        new Thread(() -> {

            try {
                IppPacket getJobsRequestPacket = IppPacket.getJobs(uri).build();
                IppPacketData getJobsRequestPacketData = new IppPacketData(getJobsRequestPacket);
                IppPacketData getJobsResponsePacketData = transport.sendData(uri, getJobsRequestPacketData);
                IppPacket getJobsResponsePacket = getJobsResponsePacketData.getPacket();

                getResponseDetails(getJobsResponsePacket);

                Intent getJobsIntent =
                        new Intent("com.example.PRINT_RESPONSE")
                                .putExtra("getJobsIntent", getJobsResponsePacket.toString());
                context.sendBroadcast(getJobsIntent);
            } catch (Exception ex) {
                Intent intent =
                        new Intent("com.example.PRINT_RESPONSE")
                                .putExtra("getJobsIntent", ex.toString());
                context.sendBroadcast(intent);
            }
        }).start();
    }

    public void getJobAttributes(URI uri, int jobId, Context context) throws IOException {

        new Thread(() -> {

            try {
                IppPacket getJobAttributesRequestPacket = IppPacket.getJobAttributes(uri, jobId).build();
                IppPacketData getJobAttributesRequestPacketData = new IppPacketData(getJobAttributesRequestPacket);
                IppPacketData getJobAttributesResponsePacketData = transport.sendData(uri, getJobAttributesRequestPacketData);
                IppPacket getJobAttributesResponsePacket = getJobAttributesResponsePacketData.getPacket();

                getResponseDetails(getJobAttributesResponsePacket);

                Intent getJobAttributesIntent =
                        new Intent("com.example.PRINT_RESPONSE")
                                .putExtra("getJobAttributesIntent", getJobAttributesResponsePacket.toString());
                context.sendBroadcast(getJobAttributesIntent);
            } catch (Exception ex) {
                Intent intent =
                        new Intent("com.example.PRINT_RESPONSE")
                                .putExtra("getJobAttributesIntent", ex.toString());
                context.sendBroadcast(intent);
            }
        }).start();

    }


    public Map<String, String> print(URI uri, File file, Context context, String fileFormat,String versionNumber) {
        Map<String, String> resultMap = new HashMap<>();
        int versionNo=0x200;
         if(versionNumber.equalsIgnoreCase("0x200")){
             versionNo=0x200;
         }else if(versionNumber.equalsIgnoreCase("0x100")){
             versionNo=0x100;
         }
        try {
            resultMap.put("uri",uri.toString()) ;
          /*  Intent intent =
                    new Intent("com.example.PRINT_RESPONSE")
                            .putExtra("finalUri", uri.toString());
            context.sendBroadcast(intent);*/


            File inputFile = new File(file.getAbsolutePath());
            boolean exists = inputFile.exists();
            Log.i("printer", String.valueOf(exists));
            logger.info("printer:"+ String.valueOf(exists));
            Log.i("printer", "input File-->" + inputFile);
            logger.info("printer"+ "input File-->" + inputFile);
            String fileName = inputFile.getName();
            String format = inputFile.getName();
            resultMap.put("fileName",fileName.toString()) ;
            if (fileName.contains(".")) {
                format = extensionTypes.get(fileName.substring(fileName.lastIndexOf(".") + 1));
                Log.i("printer", "format--->" + format.toLowerCase().trim());
                logger.info("printer"+ "format--->" + format.toLowerCase().trim());
            }
           /*
            List<String> att=new ArrayList<String>();
            List<String> attributes=new ArrayList<String>();

            try {
                att = getPrinterSupportedFormats(uri, context);
                resultMap.put("getPrinterSupportedFormatsatt", att.toString());
            }catch(Exception e){
                resultMap.put("Exception",e.getMessage());
                return resultMap;
            }*/

            /*try {
                attributes = getPrinterSupportedVersion(uri, context);
                resultMap.put("getPrinterSupportedVersionsatt", attributes.toString());
            }catch(Exception e){
                resultMap.put("Exception",e.getMessage());
                return resultMap;
            }


            if(att.isEmpty())
            {
                resultMap.put("status","getAttributefailed") ;
                return resultMap;

            }*/

          //  if (format != null && att.contains(format.toLowerCase().trim())) {
            if (format != null) {
                IppPacket printRequest = IppPacket.printJob(uri)
                        .putOperationAttributes(
                                requestingUserName.of(CMD_NAME),
                                documentFormat.of(format)).setMajorVersionNumber(versionNo)
                        .build();
                Log.i("printer", "Requesting->" + printRequest.prettyPrint(100, "  "));
                logger.info("printer"+ "Requesting->" + printRequest.prettyPrint(100, "  "));
                Log.i("printer", "In print utils method");
                logger.info("printer"+ "In print utils method");
                IppPacketData request = new IppPacketData(printRequest, new FileInputStream(inputFile));
                IppPacketData printResponse = transport.sendData(uri, request);
                resultMap.put("printResponse :",printResponse.toString()) ;
                IppPacket ippPacket = printResponse.getPacket();
                resultMap.putAll(getResponseDetails(ippPacket));
                ServerPrintRelaseFragment serverPrintRelaseFragment=new ServerPrintRelaseFragment();
                serverPrintRelaseFragment.removeDocumentFromSharedPreferences(context);




                Intent printResponseIntent =
                        new Intent("com.example.PRINT_RESPONSE")
                                .putExtra("printResponse", printResponse.toString());
                context.sendBroadcast(printResponseIntent);

                Log.i("printer", "Received ------>>>" + printResponse.getPacket().prettyPrint(100, "  "));
                logger.info("printer"+ "Received ------>>>" + printResponse.getPacket().prettyPrint(100, "  "));
            } else {
                Intent fileNotSupported =
                        new Intent("com.example.PRINT_RESPONSE")
                                .putExtra("fileNotSupported", "File Format is not supported");
                context.sendBroadcast(fileNotSupported);
            }
            return resultMap;
        } catch (Exception e) {
            resultMap.put("Exception", e.getMessage());
            Intent intent =
                    new Intent("com.example.PRINT_RESPONSE")
                            .putExtra("getPrintResponse", e.toString());
            context.sendBroadcast(intent);
        }
        return resultMap;
    }

    public Map<String, String> getAttributesCall(ArrayList<URI> ippUri, Context context)
    {
        Map<String, String> resultMap = new HashMap<>();
        int count = 0;

        for(URI specificUri: ippUri) {
            count++;
           IppPacket attributeRequest =
                    IppPacket.getPrinterAttributes(specificUri).setMajorVersionNumber(0x200)
                            .build();
           logger.info("Devnco_Android attributeRequest for Uri:"+specificUri+" ==> "+attributeRequest.toString());
           String attr ="attributeRequest:"+attributeRequest.toString();
            new Handler(Looper.getMainLooper()).post(
                    new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(context, attr, Toast.LENGTH_LONG).show();
                        }
                    });

            IppPacketData request = new IppPacketData(attributeRequest);
            logger.info("Devnco_Android IppPacketData request for Uri:"+specificUri+" ==> "+request.toString());
            String request1 ="request:"+request.toString();
            new Handler(Looper.getMainLooper()).post(
                    new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(context, request1, Toast.LENGTH_LONG).show();
                        }
                    });
            IppPacketData response;
            try{
                response = transport.sendData(specificUri, request);
                logger.info("Devnco_Android IppPacketData response for Uri:"+specificUri+" ==> "+response.toString());
                String response1 ="response:"+response.toString();
                new Handler(Looper.getMainLooper()).post(
                        new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(context, response1, Toast.LENGTH_LONG).show();
                            }
                        });

                IppPacket responsePacket = response.getPacket();
                logger.info("Devnco_Android responsePacket for Uri:"+specificUri+" ==> "+responsePacket.toString());

                resultMap = getResponseDetails(responsePacket);

                resultMap.put("versionNumber","0x200");
                if(resultMap.get("status").equalsIgnoreCase("server-error-version-not-supported")){

                     attributeRequest = IppPacket.getPrinterAttributes(specificUri).setMajorVersionNumber(0x100)
                                    .build();
                    logger.info("Devnco_Android attributeRequest for version 0x100 ==> Uri:"+specificUri+" ==> "+attributeRequest.toString());
                    String attributeRequest1 ="attributeRequest for 0x100:"+attributeRequest.toString();
                    new Handler(Looper.getMainLooper()).post(
                            new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(context, attributeRequest1, Toast.LENGTH_LONG).show();
                                }
                            });

                     request = new IppPacketData(attributeRequest);
                    logger.info("Devnco_Android IppPacketData request for version 0x100 ==> Uri:"+specificUri+" ==> "+request.toString());

                    response = transport.sendData(specificUri, request);
                    logger.info("Devnco_Android IppPacketData response for version 0x100 ==> Uri:"+specificUri+" ==> "+response.toString());
                    String response2 ="response for 0x100:"+response.toString();
                    new Handler(Looper.getMainLooper()).post(
                            new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(context, response2, Toast.LENGTH_LONG).show();
                                }
                            });

                     responsePacket = response.getPacket();
                    logger.info("Devnco_Android responsePacket for version 0x100 ==> Uri:"+specificUri+" ==> "+responsePacket.toString());

                    resultMap =  getResponseDetails(responsePacket);
                    resultMap.put("versionNumber","0x100");
                }

                Intent intent =
                        new Intent("com.example.PRINT_RESPONSE")
                                .putExtra("getPrinterAttributes", response.toString());
                context.sendBroadcast(intent);

                if(resultMap.get("status").trim().equalsIgnoreCase("successful-ok"))
                {
                    resultMap.put("finalUri",specificUri.toString());
                    resultMap.put("result","success");
                    return resultMap;
                }
                else
                {
                    resultMap.put("uri-"+count,specificUri.toString());
                    resultMap.put("result-"+count,resultMap.get("status"));
                    String result ="result-"+count+" uri-"+specificUri.toString()+" status-"+resultMap.get("status");
                    logger.info("Devnco_Android status of getAttributeCall ==> Uri:"+specificUri+" ==> "+resultMap.get("status"));
                    new Handler(Looper.getMainLooper()).post(
                            new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(context,result , Toast.LENGTH_LONG).show();
                                }
                            });

                }
            }
            catch (Exception e){
               // Log.i("printer", "print status===>" + status + "\nprint status String===>" + statusString);
                logger.info("Devnco_Android Exception in getAttributeCall ==> Uri:"+specificUri+" ==> "+e.getMessage());
                resultMap.put("uri-"+count,specificUri.toString());
                resultMap.put("result-"+count,e.getMessage());
                String result ="exception result-"+count+" uri-"+specificUri.toString()+" exception-"+e.getMessage();
                new Handler(Looper.getMainLooper()).post(
                        new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(context, result, Toast.LENGTH_LONG).show();
                            }
                        });


                continue;
            }



        }

       return resultMap;
     //   return resultMap;
    }

    @SuppressLint("LongLogTag")
    private Map<String, String> getResponseDetails(IppPacket responsePacket) {

        Map<String, String> resultMap = new HashMap<>();

        List<AttributeGroup> attributeGroupList = responsePacket.getAttributeGroups();
        for (AttributeGroup attributeGroup : attributeGroupList) {
            for (int i = 0; i < attributeGroup.size(); i++) {
                try {
                    Attribute attribute = attributeGroup.get(i);
                    resultMap.put(attribute.getName(), attribute.toString());
                }catch (Exception e){
                    Log.d("exception in getResponseDetails ",e.getMessage());
                    logger.info("Devnco_Android exception in getResponseDetails "+e.getMessage());
                    String result ="exception in getResponseDetails:"+e.getMessage();
                    new Handler(Looper.getMainLooper()).post(
                            new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(context, result, Toast.LENGTH_LONG).show();
                                }
                            });
                    continue;
                }
            }
        }

        int responseCode = responsePacket.getCode();

        Status status = responsePacket.getStatus();
        String statusString = status.getName();
        int statusStringCode = status.getCode();
        resultMap.put("status", statusString);
        Log.i("printer", "print status===>" + status + "\nprint status String===>" + statusString);
        logger.info("printer"+ "print status===>" + status + "\nprint status String===>" + statusString);
        Operation operation = responsePacket.getOperation();
        String operationName = operation.getName();
        int operationCode = operation.getCode();
        resultMap.put("operationName", operationName);

        Integer requestId = Integer.valueOf(responsePacket.getRequestId());
        resultMap.put("requestId", requestId.toString());

        return resultMap;

        // for (AttributeGroup attributeGroup : attributeGroupList) {

    }

    public List<String> getPrinterSupportedFormats(URI uri, Context context) throws
            IOException {
        List<String> attributeList = null;
            attributeList = new ArrayList<>();
            Attribute<String> requested;
            requested = requestedAttributes.of("all");

            IppPacket attributeRequest =
                    IppPacket.getPrinterAttributes(uri)
                            .putOperationAttributes(requestingUserName.of("print"), requested)
                            .build();

            String attributeRequestStringified  = attributeRequest.toString();
            Log.d("attributeRequest",attributeRequestStringified);
            logger.info("attributeRequest"+attributeRequestStringified);

           IppPacketData request = new IppPacketData(attributeRequest);
            IppPacketData response = transport.sendData(uri, request);
            attributeList.add(response.toString());
            IppPacket responsePacket = response.getPacket();

            Intent intent =
                    new Intent("com.example.PRINT_RESPONSE")
                            .putExtra("getPrinterAttributes", response.toString());
            context.sendBroadcast(intent);


            List<AttributeGroup> attributeGroupList = responsePacket.getAttributeGroups();

            for (AttributeGroup attributeGroup : attributeGroupList) {
                if (attributeGroup.get("document-format-supported") != null) {
                    Log.i("printer", "attribute groups-->" + attributeGroup.get("document-format-supported"));
                    logger.info("printer"+ "attribute groups-->" + attributeGroup.get("document-format-supported"));

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
                        logger.info("printer"+ "Format: " + i + " " + att);
                    }
                }
            }

            Log.i("printer", "attribute list in print utils->>" + attributeList);
            logger.info("printer"+ "attribute list in print utils->>" + attributeList);
            Intent printerSupportedFormatsIntent =
                    new Intent("com.example.PRINT_RESPONSE")
                            .putExtra("printerSupportedFormats", attributeList.toString());
            context.sendBroadcast(printerSupportedFormatsIntent);
            return attributeList;
    }


    public  String mapToString(Map<String, String> map) {
        StringBuilder stringBuilder = new StringBuilder();

        for (String key : map.keySet()) {
            if (stringBuilder.length() > 0) {
                stringBuilder.append("&");
            }
            String value = map.get(key);
            try {
                stringBuilder.append((key != null ? URLEncoder.encode(key, "UTF-8") : ""));
                stringBuilder.append("=");
                stringBuilder.append(value != null ? URLEncoder.encode(value, "UTF-8") : "");
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException("This method requires UTF-8 encoding support", e);
            }
        }



        return stringBuilder.toString();
    }
}
