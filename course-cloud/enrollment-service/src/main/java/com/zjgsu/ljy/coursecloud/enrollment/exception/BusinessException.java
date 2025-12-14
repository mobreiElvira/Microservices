package com.zjgsu.ljy.coursecloud.enrollment.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * 通用业务异常
 */
@Getter
public class BusinessException extends RuntimeException {
    private final HttpStatus status;

    public BusinessException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }
}
