package com.zjgsu.ljy.coursecloud.enrollment.client;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

public class UserServiceClientFallback implements UserServiceClient {

    private static final Logger log = LoggerFactory.getLogger(UserServiceClientFallback.class);

    @Override
    public Map<String, Object> getStudentById(String studentId) {
        log.warn("UserServiceClient fallback: getStudentById called with studentId={}, but user-service is unavailable", studentId);
        // 这里可以根据业务需求返回默认值或抛出异常
        throw new RuntimeException("User service is temporarily unavailable, please try again later");
    }
}
