package com.example.customeprintservice.utils;

import com.datadog.android.log.Logger;

public class DataDogLogger {
    public static Logger getLogger(){
        Logger logger = new Logger.Builder()
                .setBundleWithTraceEnabled(true)
                .setNetworkInfoEnabled(true)
                .setServiceName("MobilePrint")
                .setLogcatLogsEnabled(true)
                .setDatadogLogsEnabled(true)
                .setLoggerName("Android")
                .build();
        return logger;
    }
}

