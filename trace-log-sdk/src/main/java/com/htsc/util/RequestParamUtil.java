package com.htsc.util;

import com.alibaba.fastjson.JSONObject;
import com.htsc.wrapper.ChangeHeaderRequestWrapper;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

@Component
public class RequestParamUtil {
    public static TreeMap getParams(ChangeHeaderRequestWrapper changeHeaderRequestWrapper) throws IOException {
        return "POST".equalsIgnoreCase(changeHeaderRequestWrapper.getMethod())
                ? JSONObject.parseObject(changeHeaderRequestWrapper.getBody(), TreeMap.class)
                : getReqParamsForNotPostMethod(changeHeaderRequestWrapper);
    }

    private static TreeMap getReqParamsForNotPostMethod(ChangeHeaderRequestWrapper changeHeaderRequestWrapper) {
        TreeMap paramsMaps = new TreeMap();

        Map<String, String[]> parameterMap = changeHeaderRequestWrapper.getParameterMap();
        Set<Map.Entry<String, String[]>> entries = parameterMap.entrySet();
        Iterator<Map.Entry<String, String[]>> iterator = entries.iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, String[]> next = iterator.next();
            paramsMaps.put(next.getKey(), next.getValue()[0]);
        }
        return paramsMaps;
    }


}
