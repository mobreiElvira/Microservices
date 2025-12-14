
package com.zjgsu.ljy.coursecloud.enrollment.client;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;


//
//@Slf4j
//@Component
//public class UserClientFallbackFactory implements FallbackFactory<UserClient> {
//    @Override
//    public UserClient create(Throwable cause) {
//        return new UserClient() {
//            @Override
//            public Map<String, Object> getStudentByStudentId(String studentId) {
//                log.error("⚠️ UserClient fallback triggered for studentId: {}, 原因: {}",
//                        studentId, cause.getMessage(), cause);
//
//                Map<String, Object> fallback = new HashMap<>();
//                fallback.put("status", "ERROR");
//                fallback.put("message", "用户服务暂时不可用: " + cause.getMessage());
//                fallback.put("port", "unknown");
//                fallback.put("hostname", "unknown");
//
//                Map<String, Object> data = new HashMap<>();
//                data.put("studentId", studentId);
//                fallback.put("data", data);
//
//                return fallback;
//            }
//
//            @Override
//            public Map<String, Object> getStudentById(String id) {
//                log.error("⚠️ UserClient fallback triggered for id: {}, 原因: {}",
//                        id, cause.getMessage(), cause);
//
//                Map<String, Object> fallback = new HashMap<>();
//                fallback.put("status", "ERROR");
//                fallback.put("message", "用户服务暂时不可用: " + cause.getMessage());
//                fallback.put("port", "unknown");
//                fallback.put("hostname", "unknown");
//
//                Map<String, Object> data = new HashMap<>();
//                data.put("id", id);
//                fallback.put("data", data);
//
//                return fallback;
//            }
//        };
//    }
//}





import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class UserClientFallbackFactory implements FallbackFactory<UserClient> {

    @Override
    public UserClient create(Throwable cause) {
        // ⭐ 在创建 Fallback 时就记录日志
        log.error("=================================================");
        log.error("🔥 UserClientFallbackFactory 被调用！");
        log.error("🔥 异常类型: {}", cause.getClass().getName());
        log.error("🔥 异常信息: {}", cause.getMessage());
        log.error("=================================================", cause);

        return new UserClient() {
            @Override
            public Map<String, Object> getStudentByStudentId(String studentId) {
                // ⭐ 每次调用 Fallback 方法也记录日志
                log.error("✅✅✅ UserClient Fallback 触发！studentId: {}", studentId);
                log.error("✅ 原因: {}", cause.getMessage());

                Map<String, Object> fallback = new HashMap<>();
                fallback.put("status", "ERROR");
                fallback.put("fallback", true);
                fallback.put("message", "用户服务暂时不可用: " + cause.getMessage());
                fallback.put("port", "unknown");
                fallback.put("hostname", "fallback-instance");

                Map<String, Object> data = new HashMap<>();
                data.put("studentId", studentId);
                fallback.put("data", data);

                log.error("✅ 返回降级响应: {}", fallback);
                return fallback;
            }

            @Override
            public Map<String, Object> getStudentById(String id) {
                log.error("✅✅✅ UserClient Fallback 触发！id: {}", id);
                log.error("✅ 原因: {}", cause.getMessage());

                Map<String, Object> fallback = new HashMap<>();
                fallback.put("status", "ERROR");
                fallback.put("fallback", true);
                fallback.put("message", "用户服务暂时不可用: " + cause.getMessage());
                fallback.put("port", "unknown");
                fallback.put("hostname", "fallback-instance");

                Map<String, Object> data = new HashMap<>();
                data.put("id", id);
                fallback.put("data", data);

                return fallback;
            }
        };
    }
}
