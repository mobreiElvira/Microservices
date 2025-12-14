package com.zjgsu.ljy.coursecloud.enrollment.client;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

/**
 * Feign客户端：调用课程目录服务（catalog-service）
 * name：服务名（需与Nacos中注册的catalog-service名称一致）
 * fallback：降级处理类（服务故障时触发）
 */
@FeignClient(
        name = "catalog-service",
        fallbackFactory =  CatalogClientFallbackFactory.class
)
//@Qualifier("catalogClient")
public interface CatalogClient {

    /**
     * 匹配Catalog Service实际接口：
     * 1. 参数类型：String id
     * 2. 返回值：Map<String, Object>
     * 3. 接口路径：/courses/{id}（Controller @RequestMapping 已去掉 /api 前缀）
     */
    @GetMapping("/courses/{id}")
    Map<String, Object> getCourse(@PathVariable("id") String id);
}
