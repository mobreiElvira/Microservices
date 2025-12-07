package com.zjsu.ljy.exception;

import com.zjsu.ljy.dto.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.stream.Collectors;

@ControllerAdvice  // 全局异常处理
public class GlobalExceptionHandler {

    // 1. 处理资源不存在异常（返回404）
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleResourceNotFound(ResourceNotFoundException e) {
        return new ResponseEntity<>(ApiResponse.error(404, e.getMessage()), HttpStatus.NOT_FOUND);
    }

    // 2. 处理业务异常/参数无效异常（返回400）
    @ExceptionHandler({BusinessException.class, InvalidParamException.class})
    public ResponseEntity<ApiResponse<Void>> handleBusinessError(RuntimeException e) {
        return new ResponseEntity<>(ApiResponse.error(400, e.getMessage()), HttpStatus.BAD_REQUEST);
    }

    // 3. 新增：处理参数校验异常（如邮箱格式错误，返回400）
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleParamValidationError(MethodArgumentNotValidException e) {
        // 提取所有校验失败的信息（如"邮箱格式无效"）
        String errorMsg = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)  // 获取每个字段的错误提示
                .collect(Collectors.joining("；"));   // 多个错误用分号分隔

        // 返回400状态码和校验错误信息
        return new ResponseEntity<>(ApiResponse.error(400, errorMsg), HttpStatus.BAD_REQUEST);
    }

    // 4. 处理未知异常（返回500，兜底）
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnknownError(Exception e) {
        return new ResponseEntity<>(ApiResponse.error(500, "服务器内部错误：" + e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
