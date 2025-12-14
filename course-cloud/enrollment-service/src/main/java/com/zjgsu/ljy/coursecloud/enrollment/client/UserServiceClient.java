package com.zjgsu.ljy.coursecloud.enrollment.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

@FeignClient(name = "user-service", fallback = UserServiceClientFallback.class)
public interface UserServiceClient {

    @GetMapping("/api/students/studentId/{studentId}")
    Map<String, Object> getStudentById(@PathVariable("studentId") String studentId);
}
