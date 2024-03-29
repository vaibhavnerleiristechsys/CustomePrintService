package com.printerlogic.printerlogic.jipp;

import java.net.InetAddress;
import java.util.List;

public class PrinterModel {

    private InetAddress printerHost;

    private Integer printerPort;

    private String serviceName;

    private Boolean manual = false;

    private Boolean isFromServer = false;

    private String nodeId;

    private String id;

    private String isPullPrinter;

    private String pull_print;

    private Boolean isRecentUsed = false;

    private String location;

    private Integer isColor;

    private String secure_release;

    private List<String> orientationSupportList;

    private List<String> sidesSupportList;

    private List<String> colorSupportList;

    private List<String> mediaSupportList;

    private List<String> documentSupportList;

    private String printerAddedByUser;

    private String IdpName;



    public String getSecure_release() {
        return secure_release;
    }

    public void setSecure_release(String secure_release) {
        this.secure_release = secure_release;
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public Boolean getManual() {
        return manual;
    }

    public void setManual(Boolean manual) {
        this.manual = manual;
    }

    public Boolean getFromServer() {
        return isFromServer;
    }

    public void setFromServer(Boolean fromServer) {
        isFromServer = fromServer;
    }

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

    public String getIsPullPrinter() {
        return isPullPrinter;
    }

    public void setIsPullPrinter(String isPullPrinter) {
        this.isPullPrinter = isPullPrinter;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPull_print() {
        return pull_print;
    }

    public void setPull_print(String pull_print) {
        this.pull_print = pull_print;
    }

    public Boolean getRecentUsed() {
        return isRecentUsed;
    }

    public void setRecentUsed(Boolean recentUsed) {
        isRecentUsed = recentUsed;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Integer getIsColor() {
        return isColor;
    }

    public void setIsColor(Integer isColor) {
        this.isColor = isColor;
    }

    public List<String> getOrientationSupportList() {
        return orientationSupportList;
    }

    public void setOrientationSupportList(List<String> orientationSupportList) {
        this.orientationSupportList = orientationSupportList;
    }

    public List<String> getSidesSupportList() {
        return sidesSupportList;
    }

    public void setSidesSupportList(List<String> sidesSupportList) {
        this.sidesSupportList = sidesSupportList;
    }

    public List<String> getColorSupportList() {
        return colorSupportList;
    }

    public void setColorSupportList(List<String> colorSupportList) {
        this.colorSupportList = colorSupportList;
    }

    public List<String> getMediaSupportList() {
        return mediaSupportList;
    }

    public void setMediaSupportList(List<String> mediaSupportList) {
        this.mediaSupportList = mediaSupportList;
    }

    public List<String> getDocumentSupportList() {
        return documentSupportList;
    }

    public void setDocumentSupportList(List<String> documentSupportList) {
        this.documentSupportList = documentSupportList;
    }


    public String getPrinterAddedByUser() {
        return printerAddedByUser;
    }

    public void setPrinterAddedByUser(String printerAddedByUser) {
        this.printerAddedByUser = printerAddedByUser;
    }

    public String getIdpName() {
        return IdpName;
    }

    public void setIdpName(String idpName) {
        IdpName = idpName;
    }
}

