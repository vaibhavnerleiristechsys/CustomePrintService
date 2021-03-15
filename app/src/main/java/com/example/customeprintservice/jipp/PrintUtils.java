package com.example.customeprintservice.jipp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;
import com.example.customeprintservice.MainActivity;
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
            Log.i("printer", "input File-->" + inputFile);
            String fileName = inputFile.getName();
            String format = inputFile.getName();
            resultMap.put("fileName",fileName.toString()) ;
            if (fileName.contains(".")) {
                format = extensionTypes.get(fileName.substring(fileName.lastIndexOf(".") + 1));
                Log.i("printer", "format--->" + format.toLowerCase().trim());
            }
            List<String> att=new ArrayList<String>();
            List<String> attributes=new ArrayList<String>();

            /*try {
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

                Log.i("printer", "In print utils method");
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

            IppPacketData request = new IppPacketData(attributeRequest);
            IppPacketData response;
            try{
                response = transport.sendData(specificUri, request);
                IppPacket responsePacket = response.getPacket();
                resultMap =  getResponseDetails(responsePacket);
                String responseStringified=response.toString();
                resultMap.put("versionNumber","0x200");
                if(resultMap.get("status").equalsIgnoreCase("server-error-version-not-supported")){
                    //resultMap.clear();
                     attributeRequest = IppPacket.getPrinterAttributes(specificUri).setMajorVersionNumber(0x100)
                                    .build();
                     request = new IppPacketData(attributeRequest);
                    response = transport.sendData(specificUri, request);
                     responsePacket = response.getPacket();
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
                    new Handler(Looper.getMainLooper()).post(
                            new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(context,result , Toast.LENGTH_LONG).show();
                                }
                            });

                }
            }
            catch (IOException e){
               // Log.i("printer", "print status===>" + status + "\nprint status String===>" + statusString);
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

    private Map<String, String> getResponseDetails(IppPacket responsePacket) {

        Map<String, String> resultMap = new HashMap<>();

        List<AttributeGroup> attributeGroupList = responsePacket.getAttributeGroups();
        for (AttributeGroup attributeGroup : attributeGroupList) {
            for (int i = 0; i < attributeGroup.size(); i++) {
                Attribute attribute = attributeGroup.get(i);
                resultMap.put(attribute.getName(), attribute.toString());
            }
        }

        int responseCode = responsePacket.getCode();

        Status status = responsePacket.getStatus();
        String statusString = status.getName();
        int statusStringCode = status.getCode();
        resultMap.put("status", statusString);
        Log.i("printer", "print status===>" + status + "\nprint status String===>" + statusString);

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
    }



    public List<String> getPrinterSupportedVersion(URI uri, Context context) throws
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
            if (attributeGroup.get("ipp-versions-supported") != null) {
                Log.i("printer", "attribute groups-->" + attributeGroup.get("ipp-versions-supported"));
                Attribute attribute = attributeGroup.get("ipp-versions-supported");
                for (int i = 0; i < attribute.size(); i++) {
                    Object att = attribute.get(i);
                  //  if (att instanceof OtherString) {
                      //  OtherString attOtherString = (OtherString) att;
                    //    ValueTag valueTag = attOtherString.getTag();
                    //    String tagName = valueTag.getName();
                    //    String tagValue = attOtherString.getValue().trim().toLowerCase();
                        //attributeList.add(tagValue);
                    attributeList.add(att.toString());
                  //  }
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
