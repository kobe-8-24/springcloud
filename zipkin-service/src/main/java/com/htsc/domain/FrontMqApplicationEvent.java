package com.htsc.domain;

import org.springframework.context.ApplicationEvent;

public class FrontMqApplicationEvent extends ApplicationEvent {
    private static final long serialVersionUID = 1L;

    private String pageId;
    private String userId;
    private String frontReportMetric;

    public FrontMqApplicationEvent(Object source, String pageId, String userId, String frontReportMetric) {
        super(source);
        this.pageId = pageId;
        this.userId = userId;
        this.frontReportMetric = frontReportMetric;
    }



    public String getPageId() {
        return pageId;
    }

    public void setPageId(String pageId) {
        this.pageId = pageId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getFrontReportMetric() {
        return frontReportMetric;
    }

    public void setFrontReportMetric(String frontReportMetric) {
        this.frontReportMetric = frontReportMetric;
    }
}
