package com.zjgsu.ljy.coursecloud.enrollment.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

@FeignClient(name = "catalog-service", fallback = CatalogServiceClientFallback.class)
public interface CatalogServiceClient {

    @GetMapping("/api/courses/{courseId}")
    Map<String, Object> getCourseById(@PathVariable("courseId") String courseId);
}
