package com.zjsu.ljy.course.exception;

import com.zjsu.ljy.course.dto.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice  // 全局异常处理
public class GlobalExceptionHandler {
    /**
     * 处理资源不存在异常（404）
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleResourceNotFound(ResourceNotFoundException e) {
        return new ResponseEntity<>(ApiResponse.error(404, e.getMessage()), HttpStatus.NOT_FOUND);
    }

    /**
     * 处理业务逻辑异常（400）
     */
    @ExceptionHandler({BusinessException.class, InvalidParamException.class})
    public ResponseEntity<ApiResponse<Void>> handleBusinessError(RuntimeException e) {
        return new ResponseEntity<>(ApiResponse.error(400, e.getMessage()), HttpStatus.BAD_REQUEST);
    }

    /**
     * 处理其他未知异常（500）
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnknownError(Exception e) {
        return new ResponseEntity<>(ApiResponse.error(500, "服务器内部错误：" + e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
