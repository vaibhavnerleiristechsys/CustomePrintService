package com.printerlogic.printerlogic.utils;

import com.printerlogic.printerlogic.jipp.PrinterModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class PrinterListComparator {
    public static ArrayList<PrinterModel> getSortedPrinterList(ArrayList<PrinterModel> list){
        Collections.sort(list, new Comparator<PrinterModel>() {
            @Override
            public int compare(PrinterModel item, PrinterModel t1) {
                String s1 = item.getServiceName();
                String s2 = t1.getServiceName();
                return s1.compareToIgnoreCase(s2);
            }

        });
        return list;
    }
}
