package com.example.customeprintservice.jipp;

import java.util.ArrayList;
import java.util.List;

public class PrinterList {

   static private List<Printer> printerList = new ArrayList<>();

    public static List<Printer> getPrinterList() {
        return printerList;
    }

    public static void setPrinterList(List<Printer> printerList) {
        PrinterList.printerList = printerList;
    }
}
