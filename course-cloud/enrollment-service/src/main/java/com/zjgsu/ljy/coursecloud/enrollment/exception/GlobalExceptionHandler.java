package com.zjgsu.ljy.coursecloud.enrollment.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 全局异常处理器：统一返回Fallback/业务异常响应
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理服务不可用异常（Fallback触发）
     */
    @ExceptionHandler(ServiceUnavailableException.class)
    public ResponseEntity<Map<String, Object>> handleServiceUnavailableException(
            ServiceUnavailableException e, WebRequest request) {
        Map<String, Object> response = buildErrorResponse(
                e.getStatus(),
                e.getMessage(),
                request.getDescription(false)
        );
        log.warn("Fallback响应触发: {}", e.getMessage());
        return new ResponseEntity<>(response, e.getStatus());
    }

    /**
     * 处理通用业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Map<String, Object>> handleBusinessException(
            BusinessException e, WebRequest request) {
        Map<String, Object> response = buildErrorResponse(
                e.getStatus(),
                e.getMessage(),
                request.getDescription(false)
        );
        return new ResponseEntity<>(response, e.getStatus());
    }

    /**
     * 处理ResponseStatusException（兼容原有代码）
     */
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> handleResponseStatusException(
            ResponseStatusException e, WebRequest request) {
        Map<String, Object> response = buildErrorResponse(
                e.getStatusCode(),
                e.getReason(),
                request.getDescription(false)
        );
        return new ResponseEntity<>(response, e.getStatusCode());
    }

    /**
     * 兜底处理所有未捕获的异常
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(
            Exception e, WebRequest request) {
        Map<String, Object> response = buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "服务器内部错误，请稍后重试",
                request.getDescription(false)
        );
        log.error("系统异常", e);
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * 构建标准化错误响应
     */
    private Map<String, Object> buildErrorResponse(
            HttpStatusCode status, String message, String path) {  // 修改为HttpStatusCode
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now().toString());
        errorResponse.put("status", status.value());
        errorResponse.put("error", status instanceof HttpStatus ? ((HttpStatus) status).getReasonPhrase() : "Unknown error");
        errorResponse.put("message", message);
        errorResponse.put("path", path.replace("uri=", ""));
        return errorResponse;
    }

}
