package com.zjsu.ljy.course.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    // 状态码（200=成功，404=资源不存在等）
    private int code;
    // 消息（成功/错误描述）
    private String message;
    // 响应数据（成功时返回，错误时为null）
    private T data;

    // 成功响应（带数据）
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(200, "Success", data);
    }

    // 成功响应（无数据，如删除）
    public static <T> ApiResponse<T> success() {
        return new ApiResponse<>(200, "Success", null);
    }

    // 错误响应
    public static <T> ApiResponse<T> error(int code, String message) {
        return new ApiResponse<>(code, message, null);
    }
}
