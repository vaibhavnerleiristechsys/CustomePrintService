package com.example.customeprintservice.jipp;

import java.net.InetAddress;

public class PrinterModel {

    private InetAddress printerHost;

    private Integer printerPort;

    private String serviceName;

    public InetAddress getPrinterHost() {
        return printerHost;
    }

    public void setPrinterHost(InetAddress printerHost) {
        this.printerHost = printerHost;
    }

    public Integer getPrinterPort() {
        return printerPort;
    }

    public void setPrinterPort(Integer printerPort) {
        this.printerPort = printerPort;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }
}

