package com.example.customeprintservice

import android.print.PrintAttributes
import android.print.PrintAttributes.Margins
import android.print.PrintAttributes.Resolution
import android.print.PrinterCapabilitiesInfo
import android.print.PrinterId
import android.print.PrinterInfo
import android.printservice.PrintJob
import android.printservice.PrintService
import android.printservice.PrinterDiscoverySession
import android.util.Log


class MyPrintService : PrintService() {

    private val TAG = this.javaClass.name

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

        override fun onStartPrinterStateTracking(printerId: PrinterId) {
            Log.d(TAG, "onStartPrinterStateTracking")
        }

        override fun onStartPrinterDiscovery(priorityList: MutableList<PrinterId>) {
            Log.d("TAG", "onStartPrinterDiscovery")


            val printerIDS: MutableList<PrinterId> = ArrayList<PrinterId>()

            for (i in priorityList.indices) {
                val printerID = priorityList[i]
                printerIDS.add(printerID)
            }

            val printerInfoList: MutableList<PrinterInfo> = getPrinterList(priorityList)
            addPrinters(printerInfoList)

//
//            List<PrinterId> printerIDS = new ArrayList<PrinterId>();
//            for (int i = 0;i < priorityList.size();i++){
//                PrinterId printerID = priorityList . get (i);
//                printerIDS.add(printerID);
//
//            }
//            removePrinters(printerIDS);
//
//            List<PrinterInfo> printerInfoList = getPrinterList (priorityList);
//            addPrinters(printerInfoList);
        }

        private fun getPrinterList(priorityList: MutableList<PrinterId>): MutableList<PrinterInfo> {
            val printersInfoList: MutableList<PrinterInfo> =
                ArrayList<PrinterInfo>()
            for (i in priorityList.indices) {
                val printerID = priorityList[i]
                val capabilities =
                    PrinterCapabilitiesInfo.Builder(printerID)
                        .addMediaSize(PrintAttributes.MediaSize.ISO_A1, true)
                        .setColorModes(
                            PrintAttributes.COLOR_MODE_COLOR
                                    or PrintAttributes.COLOR_MODE_MONOCHROME,
                            PrintAttributes.COLOR_MODE_COLOR
                        )
                        .addResolution(Resolution("R1", "sdfsdf", 600, 600), true)
                        .setMinMargins(Margins(50, 50, 50, 50))
                        .build()
//                val printer: PrinterInfo = PrintAttributes.Builder().build()
//                printersInfoList.add(printer)
            }
            return printersInfoList
        }

        override fun onDestroy() {
            Log.d(TAG, "onDestroy")
            //            List<PrinterId> dd = new ArrayList<PrinterId>();
//            dd.add(generatePrinterId("Printer 1"));
//            removePrinters(dd);
        }
    }

    override fun onRequestCancelPrintJob(printJob: PrintJob?) {

    }

    override fun onPrintJobQueued(printJob: PrintJob?) {

    }

}