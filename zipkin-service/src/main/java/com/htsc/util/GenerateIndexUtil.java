package com.htsc.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public final class GenerateIndexUtil {

    private static final String DATE_FORMAT = "yyMMdd";

    public static String generateIndexNameByDay(String indexPrefix) {
        // 获取当前年月日
        return indexPrefix + new SimpleDateFormat(DATE_FORMAT).format(new Date());
    }
}
