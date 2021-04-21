package com.example.customeprintservice.model;

import java.net.URI;

public class JobsModel {
    private Integer jobId;

    private URI usedUri;

    private Integer pageNo;

    private String jobStatus;

    public Integer getJobId() {
        return jobId;
    }

    public void setJobId(Integer jobId) {
        this.jobId = jobId;
    }

    public URI getUsedUri() {
        return usedUri;
    }

    public void setUsedUri(URI usedUri) {
        this.usedUri = usedUri;
    }

    public Integer getPageNo() {
        return pageNo;
    }

    public void setPageNo(Integer pageNo) {
        this.pageNo = pageNo;
    }

    public String getJobStatus() {
        return jobStatus;
    }

    public void setJobStatus(String jobStatus) {
        this.jobStatus = jobStatus;
    }
}

