package com.printerlogic.printerlogic.jipp;

import java.util.ArrayList;

public class PrinterList {

    private static ArrayList<PrinterModel> printerList = new ArrayList<>();

    public boolean addPrinterModel(PrinterModel printerModel) {

        return printerList.add(printerModel);
    }

    public void removePrinters() {
        printerList.clear();
    }

    public ArrayList<PrinterModel> getPrinterList() {
        return printerList;
    }




}


