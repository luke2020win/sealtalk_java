package com.rcloud.server.sealtalk.filter;

import com.fasterxml.jackson.databind.JsonNode;
import com.rcloud.server.sealtalk.util.JacksonUtil;
import com.rcloud.server.sealtalk.util.MiscUtils;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

/**
 * @Author: Jianlu.Yu
 * @Date: 2020/8/19
 * @Description:
 * @Copyright (c) 2020, rongcloud.cn All Rights Reserved
 */
@Slf4j
//@WebFilter(urlPatterns = "/*", filterName = "modifyParamFilter")
public class ModifyParamFilter implements Filter {

    private static final String CONTENT_TYPE = "application/json;charset=UTF-8";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        ModifyParamRequestWrapper requestWrapper = new ModifyParamRequestWrapper((HttpServletRequest) request);
        String method = requestWrapper.getMethod();
        String contentType = requestWrapper.getContentType();
        Map<String, String[]> parameterMap = new HashMap<>();
        if (method.equals("POST") && CONTENT_TYPE.equals(contentType)) {
            BufferedReader streamReader = new BufferedReader(new InputStreamReader(requestWrapper.getInputStream(), "UTF-8"));
            StringBuilder requestParamStr = new StringBuilder();
            String inputStr;
            while ((inputStr = streamReader.readLine()) != null)
                requestParamStr.append(inputStr);

            try {
                Map<String, Object> map = JacksonUtil.fromJson(requestParamStr.toString(), parameterMap.getClass());
                Set<Map.Entry<String, Object>> entrySet = map.entrySet();
                for (Map.Entry<String, Object> entry : entrySet) {
                    Object val = entry.getValue();
                    if (val instanceof String) {
                        parameterMap.put(entry.getKey(), new String[]{String.valueOf(val)});
                    } else if (val instanceof List) {
                        List<Object> list = (List) val;
                        String[] valArray = new String[list.size()];
                        for (int i = 0; i < list.size(); i++) {
                            valArray[i] = String.valueOf(list.get(i));
                        }
                        parameterMap.put(entry.getKey(), valArray);
                    }
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                throw new RuntimeException(e);
            }
        } else {
            parameterMap = new HashMap<>(requestWrapper.getParameterMap());
        }

        Map<String, String[]> iteratorMap = new HashMap<>(parameterMap);

        Set<Map.Entry<String, String[]>> entrySet = iteratorMap.entrySet();
        for (Map.Entry<String, String[]> entry : entrySet) {
            String key = entry.getKey();
            String[] val = entry.getValue();
            if (key.endsWith("Id") || key.endsWith("Ids")) {
                String encodeKey = "encoded" + key.substring(0, 1).toUpperCase() + key.substring(1);
                parameterMap.put(encodeKey, val);
                try {
                    parameterMap.put(key, MiscUtils.decodeIds(val));
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
            }
        }
        requestWrapper.setParameterMap(parameterMap);
        chain.doFilter(requestWrapper, response);
    }


    public void jsonLeaf(JsonNode node) {
        if (node.isValueNode()) {
            System.out.println(node.toString());
            return;
        }

        if (node.isObject()) {
            Iterator<Map.Entry<String, JsonNode>> it = node.fields();
            while (it.hasNext()) {
                Map.Entry<String, JsonNode> entry = it.next();
                jsonLeaf(entry.getValue());
            }
        }

        if (node.isArray()) {
            Iterator<JsonNode> it = node.iterator();
            while (it.hasNext()) {
                jsonLeaf(it.next());
            }
        }
    }

}
