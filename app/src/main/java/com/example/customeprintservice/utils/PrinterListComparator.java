package com.example.customeprintservice.utils;

import com.example.customeprintservice.jipp.PrinterModel;
import com.example.customeprintservice.print.PrintersFragment;

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
