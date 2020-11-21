package com.example.customeprintservice

/*import android.annotation.SuppressLint
import android.os.Build
import android.print.PrintAttributes
import android.print.PrintAttributes.Resolution
import android.print.PrinterCapabilitiesInfo
import android.print.PrinterId
import android.print.PrinterInfo
import android.printservice.PrintJob
import android.printservice.PrintService
import android.printservice.PrinterDiscoverySession
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.customeprintservice.jipp.PrintUtils
import com.example.customeprintservice.jipp.PrinterList
import com.example.customeprintservice.jipp.PrinterModel
import java.io.*
import java.net.URI
import java.util.*
import kotlin.collections.HashMap


class MyPrintService : PrintService() {

    private val TAG = this.javaClass.name
    val hashMap = HashMap<PrinterId, PrinterModel>()
    val printUtils = PrintUtils()

    override fun onCreatePrinterDiscoverySession(): PrinterDiscoverySession? {
        return abc
    }

    var abc: PrinterDiscoverySession = object : PrinterDiscoverySession() {
        override fun onValidatePrinters(printerIds: List<PrinterId>) {
            Log.d(TAG, "onValidatePrinters")
        }

        override fun onStopPrinterStateTracking(printerId: PrinterId) {
            Log.d(TAG, "onStopPrinterStateTracking")
        }

        override fun onStopPrinterDiscovery() {
            Log.d(TAG, "onStopPrinterDiscovery")
        }

        @RequiresApi(Build.VERSION_CODES.N)
        override fun onStartPrinterStateTracking(printerId: PrinterId) {
            Log.d(TAG, "onStartPrinterStateTracking")
            Log.i(TAG, "printer selected=>${printerId}")
            hashMap.forEach { t, u ->
                run {
                    if (printerId == t) {
                        Log.i(TAG, "onStartPrinterStateTracking==>${u.serviceName}")
                    }
                }
            }
        }

        override fun onStartPrinterDiscovery(priorityList: MutableList<PrinterId>) {
            Log.d(TAG, "onStartPrinterDiscovery")
            PrintUtils().setContextAndInitializeJMDNS(applicationContext)

            try {
                Thread.sleep(3000)
            } catch (e: Exception) {
                Log.i(TAG, "ex==>${e.message}")
            }
            val printers: MutableList<PrinterInfo> = ArrayList()
            var printerId: PrinterId

            PrinterList().printerList.forEach {
                Log.i(TAG, "printer list nsd utils=>${it.serviceName}")
                printerId = generatePrinterId(it.printerHost.toString())
                val builder =
                    PrinterInfo.Builder(printerId, it.serviceName, PrinterInfo.STATUS_IDLE)
                val capBuilder = PrinterCapabilitiesInfo.Builder(printerId)
                capBuilder.addMediaSize(PrintAttributes.MediaSize.ISO_A4, true)
                capBuilder.addMediaSize(PrintAttributes.MediaSize.ISO_A3, false)
                capBuilder.addResolution(
                    Resolution("resolutionId", "default resolution", 600, 600),
                    true
                )
                capBuilder.setColorModes(
                    PrintAttributes.COLOR_MODE_COLOR or PrintAttributes.COLOR_MODE_MONOCHROME,
                    PrintAttributes.COLOR_MODE_COLOR
                )

                builder.setCapabilities(capBuilder.build())
                printers.add(builder.build())
                addPrinters(printers)

                hashMap.put(printerId, it)
            }

            Log.i(TAG, "Is printer discovery started $isPrinterDiscoveryStarted")
        }

        override fun onDestroy() {
            Log.d(TAG, "onDestroy")
        }
    }

    override fun onRequestCancelPrintJob(printJob: PrintJob?) {
        Log.i(TAG, "on RequestCancelPrintJob")
    }

    @SuppressLint("SdCardPath")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onPrintJobQueued(printJob: PrintJob?) {
        Log.i(TAG, "on Print Job Queued")
        val jobInfo = printJob?.info

        Log.i(TAG, "jobInfo==>${jobInfo}")
        val printerId = jobInfo?.printerId
        val finalUrl =
            "http" + "://" + hashMap.get(printerId)?.printerHost + ":" + hashMap.get(printerId)?.printerPort + "/ipp/print"

        if (printJob!!.isQueued) {
            printJob.start()
        }
        val info = printJob.info
        val file = File(filesDir, info.label + ".pdf")


        var `in`: InputStream? = null
        var out: FileOutputStream? = null

        try {
            `in` = FileInputStream(printJob.document.data!!.fileDescriptor)
            out = FileOutputStream(file)
            val buffer = ByteArray(1024)
            var read: Int
            while (`in`.read(buffer).also { read = it } != -1) {
                out.write(buffer, 0, read)
            }
            `in`.close()
            out.flush()
            out.close()

            printUtils.print(URI.create(finalUrl), file, applicationContext, "")
            val sb = StringBuilder()
            val fisTest = FileInputStream(file)
            var readByte = 0
            while (readByte != -1) {
                readByte = fisTest.read()
                val readChar = readByte.toChar()
                sb.append(readChar)
            }
            fisTest.close()
            Log.i("print=====>", sb.toString())
        } catch (ioe: IOException) {
        }
    }
}*/


/*
package com.example.customeprintservice;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.print.PrintAttributes;
import android.print.PrintJobInfo;
import android.print.PrinterCapabilitiesInfo;
import android.print.PrinterId;
import android.print.PrinterInfo;
import android.printservice.PrintJob;
import android.printservice.PrintService;
import android.printservice.PrinterDiscoverySession;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.example.customeprintservice.jipp.PrintUtils;
import com.example.customeprintservice.jipp.PrinterList;
import com.example.customeprintservice.jipp.PrinterModel;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class ThermalPrintService extends PrintService {

    private static final String TAG = "ThermalPrintService";

    private PrinterInfo builder;
    private Handler mHandler;

    @Override
    public void onCreate() {
//        builder = new PrinterInfo.Builder(generatePrinterId("Printer 1"),
//                "MiNiPrinter", PrinterInfo.STATUS_IDLE).build();
    }

    @Override
    protected void onConnected() {
        Log.i(TAG, "#onConnected()");
        mHandler = new PrintHandler(getMainLooper());
    }

    @Nullable
    @Override
    protected PrinterDiscoverySession onCreatePrinterDiscoverySession() {
        return new ThermalPrinterDiscoverySession(builder, getApplicationContext(), this);
    }

    @Override
    protected void onRequestCancelPrintJob(PrintJob printJob) {
        Log.i(TAG, "#onRequestCancelPrintJob() printJobId: " + printJob.getId());
        if (mHandler.hasMessages(PrintHandler.MSG_HANDLE_PRINT_JOB)) {
            mHandler.removeMessages(PrintHandler.MSG_HANDLE_PRINT_JOB);
            if (printJob.isQueued() || printJob.isStarted()) {
                printJob.cancel();
            }
        } else {
            if (printJob.isQueued() || printJob.isStarted()) {
                printJob.cancel();
            }
        }
    }

    @Override
    protected void onPrintJobQueued(PrintJob printJob) {
        Message message = mHandler.obtainMessage(PrintHandler.MSG_HANDLE_PRINT_JOB, printJob);
        mHandler.sendMessageDelayed(message, 0);
    }

    private void handleHandleQueuedPrintJob(final PrintJob printJob) {
        if (printJob.isQueued()) {
            printJob.start();
        }
        PrinterId printerId = printJob.getInfo().getPrinterId();
        PrinterHashmap printerHashmap = new PrinterHashmap();
        String finalUrl = "http" + "://" + printerHashmap.getHashMap().get(printerId).getPrinterHost() + ":" + printerHashmap.getHashMap().get(printerId).getPrinterPort() + "/ipp/print";
        final PrintJobInfo info = printJob.getInfo();
        final File file = new File(getFilesDir(), info.getLabel() + ".pdf");


        InputStream in = null;
        FileOutputStream out = null;

        try {
            in = new FileInputStream(printJob.getDocument().getData().getFileDescriptor());
            out = new FileOutputStream(file);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();

            out.flush();
            out.close();


            PrintUtils printUtils = new PrintUtils();
            printUtils.print(URI.create(finalUrl), file, getApplicationContext(), "");

            StringBuilder sb = new StringBuilder();
            FileInputStream fisTest = new FileInputStream(file);
            long fileSize = file.length();

            Log.i(TAG, "FileLength: " + fileSize);

            int readBytes = 0;
            while (readBytes != -1) {
                readBytes = fisTest.read();
                char readChar = (char) readBytes;
                sb.append(readChar);
            }
            fisTest.close();
            Log.i(TAG, sb.toString());


           /* Intent printPreview = new Intent(this, MainActivity.class);
            printPreview.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            printPreview.putExtra("FILE", file.getPath());
            startActivity(printPreview);*/

        } catch (IOException ioe) {

        }

    }

    private final class PrintHandler extends Handler {
        static final int MSG_HANDLE_PRINT_JOB = 3;

        public PrintHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message message) {
            switch (message.what) {
                case MSG_HANDLE_PRINT_JOB: {
                    PrintJob printJob = (PrintJob) message.obj;
                    handleHandleQueuedPrintJob(printJob);
                }
                break;
            }
        }
    }
}


class ThermalPrinterDiscoverySession extends PrinterDiscoverySession {

    ThermalPrintService thermalPrintService;
    private PrinterInfo printerInfo;
    private Context appContext = null;
    private PrinterInfo builder;


    ThermalPrinterDiscoverySession(PrinterInfo printerInfo, Context context, ThermalPrintService printService) {
        appContext = context;
        thermalPrintService = printService;
//        PrinterCapabilitiesInfo capabilities =
//                new PrinterCapabilitiesInfo.Builder(printerInfo.getId())
//                        .addMediaSize(PrintAttributes.MediaSize.ISO_A5, true)
//                        .addResolution(new PrintAttributes.Resolution("1234", "Default", 200, 200), true)
//                        .setColorModes(PrintAttributes.COLOR_MODE_MONOCHROME, PrintAttributes.COLOR_MODE_MONOCHROME)
//                        .build();
//        this.printerInfo = new PrinterInfo.Builder(printerInfo)
//                .setCapabilities(capabilities)
//                .build();
    }

    @SuppressLint("WrongConstant")
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onStartPrinterDiscovery(List<PrinterId> priorityList) {

        Log.d("customprintservices", "onStartPrinterDiscovery");
        PrintUtils printUtils = new PrintUtils();

        printUtils.setContextAndInitializeJMDNS(appContext);

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        List<PrinterInfo> printers = new ArrayList<>();
        final PrinterId[] printerId = new PrinterId[1];

        PrinterList printerList = new PrinterList();
        printerList.getPrinterList().forEach(p -> {
            printerId[0] = thermalPrintService.generatePrinterId(p.getPrinterHost().toString());

            PrinterInfo builder = new PrinterInfo.Builder(thermalPrintService.generatePrinterId(p.getPrinterHost().toString()),
                    p.getServiceName(), PrinterInfo.STATUS_IDLE).build();

            PrinterCapabilitiesInfo capabilities =
                    new PrinterCapabilitiesInfo.Builder(printerId[0])
                            .addMediaSize(PrintAttributes.MediaSize.ISO_A5, true)
                            .addResolution(new PrintAttributes.Resolution("1234", "Default", 200, 200), true)
                            .setColorModes(PrintAttributes.COLOR_MODE_MONOCHROME, PrintAttributes.COLOR_MODE_MONOCHROME)
                            .build();
            this.printerInfo = new PrinterInfo.Builder(builder)
                    .setCapabilities(capabilities)
                    .build();

            printers.add(this.printerInfo);
            this.addPrinters(printers);
            PrinterHashmap printerHashmap = new PrinterHashmap();
            HashMap<PrinterId, PrinterModel> hashMap = new HashMap();
            hashMap.put(printerId[0], p);
            printerHashmap.setHashMap(hashMap);
        });


        /*List<PrinterInfo> printers = new ArrayList<PrinterInfo>();
        printers.add(printerInfo);
        addPrinters(printers);*/
    }

    @Override
    public void onStopPrinterDiscovery() {

    }

    @Override
    public void onValidatePrinters(List<PrinterId> printerIds) {

    }

    @Override
    public void onStartPrinterStateTracking(PrinterId printerId) {

    }

    @Override
    public void onStopPrinterStateTracking(PrinterId printerId) {

    }

    @Override
    public void onDestroy() {

    }
}

class PrinterHashmap {
    private static HashMap<PrinterId, PrinterModel> hashMap = new HashMap();

    public HashMap<PrinterId, PrinterModel> getHashMap() {
        return hashMap;
    }

    public void setHashMap(HashMap<PrinterId, PrinterModel> hashMap) {
        PrinterHashmap.hashMap = hashMap;
    }
}


 */