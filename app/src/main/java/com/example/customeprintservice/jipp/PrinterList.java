package com.example.customeprintservice.jipp;

import java.util.ArrayList;
import java.util.List;

public class PrinterList {

    static private List<PrinterModel> printerModelList = new ArrayList<>();

    public static List<PrinterModel> getPrinterModelList() {
        return printerModelList;
    }

    public static void setPrinterModelList(List<PrinterModel> printerModelList) {
        PrinterList.printerModelList = printerModelList;
    }
}
