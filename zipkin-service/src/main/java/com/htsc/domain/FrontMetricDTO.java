package com.htsc.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FrontMetricDTO {
    private String pageId;
    private String userId;
    private String frontReportMetric;
}
