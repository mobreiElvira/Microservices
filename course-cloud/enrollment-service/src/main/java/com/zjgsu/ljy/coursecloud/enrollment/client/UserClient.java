package com.zjgsu.ljy.coursecloud.enrollment.client;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;


@FeignClient(
        name = "user-service",
        fallbackFactory = UserClientFallbackFactory.class
)
//@Qualifier("userClient")
public interface UserClient {
//    @GetMapping("/api/users/students/{id}")
//    StudentDto getStudent(@PathVariable Long id);

    /**
     * 匹配User Service实际接口（按学号查询，业务上更合理）：
     * 1. 参数类型：String studentId（原Long错误）
     * 2. 返回值：Map<String, Object>（原StudentDto错误）
     * 3. 接口路径：/api/users/students/studentId/{studentId}（匹配Controller的/students/studentId/{studentId}，前缀/api/users由Controller类注解补充）
     */
    @GetMapping("/api/users/students/studentId/{studentId}") // 业务上用学号查询更合理
    Map<String, Object> getStudentByStudentId(@PathVariable("studentId") String studentId);

    // 可选：保留按ID查询（如果业务需要）
    @GetMapping("/api/users/students/{id}") // 匹配Controller的/students/{id}
    Map<String, Object> getStudentById(@PathVariable("id") String id);
}
