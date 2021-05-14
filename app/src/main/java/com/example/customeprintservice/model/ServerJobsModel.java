package com.example.customeprintservice.model;

public class ServerJobsModel {
    private String jobId;
    private String dateTime;
    private String documentTitle;
    private String size;
    private String totalPageCount;
    private String printType;

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public String getDocumentTitle() {
        return documentTitle;
    }

    public void setDocumentTitle(String documentTitle) {
        this.documentTitle = documentTitle;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getTotalPageCount() {
        return totalPageCount;
    }

    public void setTotalPageCount(String totalPageCount) {
        this.totalPageCount = totalPageCount;
    }

    public String getPrintType() {
        return printType;
    }

    public void setPrintType(String printType) {
        this.printType = printType;
    }
}
