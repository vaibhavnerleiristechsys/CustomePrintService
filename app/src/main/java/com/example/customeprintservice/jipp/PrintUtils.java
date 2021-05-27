package com.example.customeprintservice.jipp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.customeprintservice.MainActivity;
import com.example.customeprintservice.model.JobsModel;
import com.example.customeprintservice.print.PrintPreview;
import com.example.customeprintservice.print.PrintReleaseFragment;
import com.example.customeprintservice.print.ServerPrintRelaseFragment;
import com.example.customeprintservice.utils.DataDogLogger;
import com.hp.jipp.encoding.Attribute;
import com.hp.jipp.encoding.AttributeGroup;
import com.hp.jipp.encoding.AttributeType;
import com.hp.jipp.encoding.IppPacket;
import com.hp.jipp.encoding.OtherString;
import com.hp.jipp.encoding.OutOfBandTag;
import com.hp.jipp.encoding.ValueTag;
import com.hp.jipp.model.Media;
import com.hp.jipp.model.Operation;
import com.hp.jipp.model.Orientation;
import com.hp.jipp.model.PrintColorMode;
import com.hp.jipp.model.Sides;
import com.hp.jipp.model.Status;
import com.hp.jipp.trans.IppClientTransport;
import com.hp.jipp.trans.IppPacketData;
import com.hp.jipp.util.PrettyPrinter;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.hp.jipp.model.Types.colorSupported;
import static com.hp.jipp.model.Types.documentFormat;
import static com.hp.jipp.model.Types.feedOrientation;
import static com.hp.jipp.model.Types.ippAttributeFidelity;
import static com.hp.jipp.model.Types.media;
import static com.hp.jipp.model.Types.mediaTypeSupported;
import static com.hp.jipp.model.Types.orientationRequested;
import static com.hp.jipp.model.Types.printColorMode;
import static com.hp.jipp.model.Types.requestedAttributes;
import static com.hp.jipp.model.Types.requestingUserName;
import static com.hp.jipp.model.Types.sides;

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
    //Logger logger = LoggerFactory.getLogger(PrintUtils.class);
    public static ArrayList<JobsModel> jobstatusList=new ArrayList<>();

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
                Log.i("get jobs repsonse:",getJobsResponsePacketData.toString());
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

    public void getJobsStatus(URI uri, Context context,int jobId,int pageCountNo) throws IOException {
        new Thread(() -> {
        Log.d("jobId", String.valueOf(jobId));
            try {
                IppPacket getJobsRequestPacket = IppPacket. getJobAttributes(uri,jobId).build();
                IppPacketData getJobsRequestPacketData = new IppPacketData(getJobsRequestPacket);
                IppPacketData getJobsResponsePacketData = transport.sendData(uri, getJobsRequestPacketData);
                Log.i("jobs status repsonse:",getJobsResponsePacketData.toString());
                IppPacket getJobsResponsePacket = getJobsResponsePacketData.getPacket();
                Map<String, String> map= getResponseDetails(getJobsResponsePacket);
                String status =map.get("job-state");
                String statusReasons =map.get("job-state-reasons");
                String[] jobState = status.split("=");
                String[] jobStatusReasons =statusReasons.split("=");

                if(jobState.length>0){
                    JobsModel jobModel = new JobsModel();
                    jobModel.setJobStatus(jobState[1]);
                    jobModel.setPageNo(pageCountNo);
                    jobModel.setJobId(jobId);
                    jobstatusList.add(jobModel);

                 /*   new Handler(Looper.getMainLooper()).post(
                            new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(context, "Page No: "+pageCountNo+" Print Status: "+jobState[1], Toast.LENGTH_LONG).show();
                                }
                            });

                  */
                }

            } catch (Exception ex) {
                new Handler(Looper.getMainLooper()).post(
                        new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(context, "Exception in get job Status:"+ex.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });
            }

        }).start();
    }

    public Map<String, String> print(URI uri, File file, Context context, String fileFormat,String versionNumber,String orientationType,String paperSize,boolean isColor,String sideSupported ) {
        Map<String, String> resultMap = new HashMap<>();
        Orientation orientationTypes = Orientation.portrait;

        int versionNo=0x200;
         if(versionNumber.equalsIgnoreCase("0x200")){
             versionNo=0x200;
         }else if(versionNumber.equalsIgnoreCase("0x100")){
             versionNo=0x100;
         }

         String sided =Sides.oneSided;
         if(sideSupported ==""){
             sided =Sides.oneSided;
         }else{
             sided=sideSupported;
         }

         String mediaSize =Media.isoA5;

         if(paperSize == ""){
             mediaSize =Media.isoA5;
         }else{
             mediaSize =paperSize;
         }


         String color= PrintColorMode.color;
         if(isColor == true){
             color= PrintColorMode.color;
         }else{
             color=PrintColorMode.monochrome;
         }

         if(orientationType.contains("landscape")){
          orientationTypes = Orientation.landscape;
         }else{
             orientationTypes = Orientation.portrait;
         }
        try {
            resultMap.put("uri",uri.toString()) ;
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

            if (format != null) {
                IppPacket printRequest;
                if(versionNo==0x100) {
                     printRequest = IppPacket.printJob(uri)
                            .putOperationAttributes(
                                    ippAttributeFidelity.of(false),
                                    documentFormat.of("application/octet-stream")).setMajorVersionNumber(versionNo)
                             .putJobAttributes(
                                     orientationRequested.of(orientationTypes),
                                     media.of(mediaSize),
                                     printColorMode.of(color),
                                     sides.of(sided)

                             )
                            .build();
                }else{
                     printRequest = IppPacket.printJob(uri)
                            .putOperationAttributes(
                                    requestingUserName.of(CMD_NAME),
                                    documentFormat.of(format)).setMajorVersionNumber(versionNo)
                             .putJobAttributes(
                                     orientationRequested.of(orientationTypes),
                                     media.of(mediaSize),
                                     printColorMode.of(color),
                                     sides.of(sided)
                             )
                            .build();
                }
                Log.i("printer", "Requesting->" + printRequest.prettyPrint(100, "  "));
                DataDogLogger.getLogger().i("Devnco_Android print method : "+ "printRequest->" + printRequest.toString());

                IppPacketData request = new IppPacketData(printRequest, new FileInputStream(inputFile));
                new Handler(Looper.getMainLooper()).post(
                        new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(context, request.toString(), Toast.LENGTH_LONG).show();
                            }
                        });

                IppPacketData printResponse = transport.sendData(uri, request);
                new Handler(Looper.getMainLooper()).post(
                        new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(context, printResponse.toString(), Toast.LENGTH_LONG).show();
                            }
                        });
                
                DataDogLogger.getLogger().i("Devnco_Android printResponse in print method :"+printResponse.toString());
                resultMap.put("printResponse :",printResponse.toString()) ;
                IppPacket ippPacket = printResponse.getPacket();
                resultMap.putAll(getResponseDetails(ippPacket));

                try{
                    String jobIds = resultMap.get("job-id");
                    Log.d("jobId ::", jobIds);
                    if (jobIds != null) {
                        String[] jobs = jobIds.split("=");
                        resultMap.put("jobId", jobs[1]);
                    }
                }catch(Exception e){
                   Log.e("exception in getjobs",e.getMessage());
                }


                ServerPrintRelaseFragment serverPrintRelaseFragment=new ServerPrintRelaseFragment();
                try {
                    serverPrintRelaseFragment.removeDocumentFromSharedPreferences(context);
                    Log.i("printer", "Received ------>>>" + printResponse.getPacket().prettyPrint(100, "  "));
                }catch (Exception e){
                    Log.i("printer", "exception in remove document from shared preferences ------>>>" + e.getMessage());
                }
                } else {
                Intent fileNotSupported =
                        new Intent("com.example.PRINT_RESPONSE")
                                .putExtra("fileNotSupported", "File Format is not supported");
                context.sendBroadcast(fileNotSupported);
            }
            return resultMap;
        } catch (Exception e) {
            resultMap.put("Exception", e.getMessage());
            DataDogLogger.getLogger().i("Devnco_Android Exception in print : "+ e.getMessage());
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
            DataDogLogger.getLogger().i("Devnco_Android attributeRequest for 0x200==> Uri:"+specificUri+" ==> "+attributeRequest.toString());
          /* String attr ="attributeRequest:"+attributeRequest.toString();
            new Handler(Looper.getMainLooper()).post(
                    new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(context, attr, Toast.LENGTH_LONG).show();
                        }
                    });
*/
            IppPacketData request = new IppPacketData(attributeRequest);
           // logger.info("Devnco_Android IppPacketData request for 0x200  ==> Uri:"+specificUri+" ==> "+request.toString());
       /*     String request1 ="request:"+request.toString();
            new Handler(Looper.getMainLooper()).post(
                    new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(context, request1, Toast.LENGTH_LONG).show();
                        }
                    });

        */
            IppPacketData response;
            try{
                response = transport.sendData(specificUri, request);
                DataDogLogger.getLogger().i("Devnco_Android IppPacketData response for 0x200==> Uri:"+specificUri+" ==> "+response.toString());
             /*     String response1 ="response:"+response.toString();
              new Handler(Looper.getMainLooper()).post(
                        new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(context, response1, Toast.LENGTH_LONG).show();
                            }
                        });
*/
                IppPacket responsePacket = response.getPacket();
               // logger.info("Devnco_Android responsePacket for 0x200 Uri:"+specificUri+" ==> "+responsePacket.toString());

                resultMap = getResponseDetails(responsePacket);

                resultMap.put("versionNumber","0x200");
                if(resultMap.get("status").equalsIgnoreCase("server-error-version-not-supported")){

                     attributeRequest = IppPacket.getPrinterAttributes(specificUri).setMajorVersionNumber(0x100)
                                    .build();
                    DataDogLogger.getLogger().i("Devnco_Android attributeRequest for version 0x100 ==> Uri:"+specificUri+" ==> "+attributeRequest.toString());
              /*       String attributeRequest1 ="attributeRequest for 1:"+attributeRequest.toString();
                   new Handler(Looper.getMainLooper()).post(
                            new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(context, attributeRequest1, Toast.LENGTH_LONG).show();
                                }
                            });
*/
                     request = new IppPacketData(attributeRequest);
                //    logger.info("Devnco_Android IppPacketData request for version 0x100 ==> Uri:"+specificUri+" ==> "+request.toString());

                    response = transport.sendData(specificUri, request);
                    DataDogLogger.getLogger().i("Devnco_Android IppPacketData response for version  0x100 ==> Uri:"+specificUri+" ==> "+response.toString());
                 /*   String response2 ="response for 0x100:"+response.toString();
                    new Handler(Looper.getMainLooper()).post(
                            new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(context, response2, Toast.LENGTH_LONG).show();
                                }
                            });


                  */
                     responsePacket = response.getPacket();
              //      logger.info("Devnco_Android responsePacket for version  0x100 ==> Uri:"+specificUri+" ==> "+responsePacket.toString());

                    resultMap =  getResponseDetails(responsePacket);
                    resultMap.put("versionNumber","0x100");
                }

              /*  Intent intent =
                        new Intent("com.example.PRINT_RESPONSE")
                                .putExtra("getPrinterAttributes", response.toString());
                context.sendBroadcast(intent);
*/
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
                    DataDogLogger.getLogger().i("Devnco_Android status of getAttributeCall ==> Uri:"+specificUri+" ==> "+resultMap.get("status"));
               /*     new Handler(Looper.getMainLooper()).post(
                            new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(context,result , Toast.LENGTH_LONG).show();
                                }
                            });
*/
                }
            }
            catch (Exception e){
               // Log.i("printer", "print status===>" + status + "\nprint status String===>" + statusString);
                DataDogLogger.getLogger().e("Devnco_Android Exception in getAttributeCall ==> Uri:"+specificUri+" ==> "+e.toString()+" <==");
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
                    DataDogLogger.getLogger().e("Devnco_Android exception in getResponseDetails "+e.getMessage());
                    String result ="exception in getResponseDetails:"+e.getMessage();
                  /*  new Handler(Looper.getMainLooper()).post(
                            new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(context, result, Toast.LENGTH_LONG).show();
                                }
                            });

                   */
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
      //  logger.info("Devnco_Android printer"+ "print status===>" + status + "\nprint status String===>" + statusString);
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
            DataDogLogger.getLogger().i("Devnco_Android attributeRequest"+attributeRequestStringified);

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
                    DataDogLogger.getLogger().i("Devnco_Android printer"+ "attribute groups-->" + attributeGroup.get("document-format-supported"));

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
                        DataDogLogger.getLogger().i("Devnco_Android printer"+ "Format: " + i + " " + att);
                    }
                }
            }

            Log.i("printer", "attribute list in print utils->>" + attributeList);
            DataDogLogger.getLogger().i("Devnco_Android printer"+ "attribute list in print utils->>" + attributeList);
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
