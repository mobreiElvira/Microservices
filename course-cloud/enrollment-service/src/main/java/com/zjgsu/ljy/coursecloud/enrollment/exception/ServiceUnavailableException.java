package com.zjgsu.ljy.coursecloud.enrollment.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * 自定义服务不可用异常（用于Fallback场景）
 */
@Getter
public class ServiceUnavailableException extends RuntimeException {
    private final HttpStatus status;

    public ServiceUnavailableException(String message) {
        super(message);
        this.status = HttpStatus.SERVICE_UNAVAILABLE; // 503状态码
    }

    public ServiceUnavailableException(String message, Throwable cause) {
        super(message, cause);
        this.status = HttpStatus.SERVICE_UNAVAILABLE;
    }
}
