package com.example.customeprintservice.jipp;

import java.util.ArrayList;
import java.util.List;

public class PrinterList {

    private static List<PrinterModel> printerList = new ArrayList<>();

    public boolean addPrinterModel(PrinterModel printerModel) {

        return printerList.add(printerModel);
    }

    public void removePrinter() {

    }

    public List<PrinterModel> getPrinterList() {
        return printerList;
    }

}


