package com.zjgsu.ljy.coursecloud.enrollment.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

public class CatalogServiceClientFallback implements CatalogServiceClient {

    private static final Logger log = LoggerFactory.getLogger(CatalogServiceClientFallback.class);

    @Override
    public Map<String, Object> getCourseById(String courseId) {
        log.warn("CatalogServiceClient fallback: getCourseById called with courseId={}, but catalog-service is unavailable", courseId);
        // 这里可以根据业务需求返回默认值或抛出异常
        throw new RuntimeException("Catalog service is temporarily unavailable, please try again later");
    }
}
