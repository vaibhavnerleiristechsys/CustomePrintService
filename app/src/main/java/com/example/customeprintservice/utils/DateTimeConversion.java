package com.example.customeprintservice.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateTimeConversion {
    public static String currentDateTime(){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String date = sdf.format(new Date());
        return date;
    }
}
