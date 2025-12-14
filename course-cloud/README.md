
# 校园选课系统 - 微服务架构

**项目名称**: course-cloud

**版本**: v08

**演进基础**: 基于单体应用 course-v07 拆分的微服务架构实践项目，已集成OpenFeign进行服务间通信

## 项目简介

本项目将传统单体选课系统按业务域拆分为 **用户服务、课程目录服务、选课服务** 三大核心微服务，通过 Nacos 实现服务注册发现与负载均衡，采用 Docker 容器化部署保障环境一致性，各服务独立数据库存储实现数据隔离，基于 **OpenFeign** 实现服务间可靠通信，最终达成 **业务解耦、弹性扩展、故障容错** 的微服务核心目标。

### 整体架构图

![微服务架构图](architecture.png)

### 架构核心说明

1. **客户端层**：接收用户操作请求，通过统一入口发起服务调用；
2. **服务注册发现层**：Nacos 作为核心中间件，负责服务注册、健康检查、负载均衡和故障转移；
3. **微服务层**：按业务域拆分的独立服务，可单独部署、扩容、迭代，互不影响；
4. **数据存储层**：各服务对应独立数据库，实现数据隔离，避免单点故障影响全系统。

### 技术栈

| 技术 / 框架                 | 版本 / 说明                     |
| ----------------------- | --------------------------- |
| Spring Boot             | 3.3.4（微服务基础框架）              |
| Java                    | 17（开发语言，适配 Spring Boot 3.x） |
| MySQL                   | 8.4（关系型数据库，数据持久化）           |
| Docker & Docker Compose | 20.10+/2.0+（容器化部署，环境一致性）    |
| OpenFeign               | 内置（服务间 HTTP 通信，支持负载均衡和熔断）  |
| Nacos                   | 2.2.3（服务注册发现 / 负载均衡 / 健康检查） |
| Spring Cloud Alibaba    | 适配 Nacos 生态，微服务注册发现支持       |
| Resilience4j            | 熔断降级实现                     |
| Maven                   | 3.8+（项目构建与依赖管理）             |

### 环境要求

| 依赖           | 版本 / 要求                                |
| -------------- | ------------------------------------------ |
| JDK            | 17（必须，Spring Boot 3.x 强制要求）       |
| Maven          | 3.8+（项目构建与依赖下载）                 |
| Docker         | 20.10+（容器化部署基础）                   |
| Docker Compose | 2.0+（多容器编排工具）                     |
| 操作系统       | Linux/macOS/Windows（Windows 需开启 WSL2） |
| 内存           | 至少 4GB（推荐 8GB+，保障多容器运行流畅）  |

## 快速上手

### 1. 项目结构

```
course-cloud/
├── README.md               # 项目说明文档（本文档）
├── docker-compose.yml      # Docker 多容器编排配置
├── run.sh                  # 一键启动脚本（构建+启动所有服务）
├── test-services.sh        # 基础功能测试脚本（创建初始学生和课程）
├── test-load-balance.sh    # 负载均衡测试脚本
├── Fallback.sh             # 熔断降级测试脚本
├── user-service/           # 用户服务模块（独立 Maven 项目）
├── catalog-service/        # 课程目录服务模块（独立 Maven 项目）
└── enrollment-service/     # 选课服务模块（独立 Maven 项目，集成OpenFeign）
```

### 2. 一键启动所有服务

```sh
# 1. 首次运行赋予脚本执行权限
chmod +x run.sh

# 2. 构建 Docker 镜像并启动所有服务（含 Nacos、数据库、微服务）
./run.sh
```

脚本自动完成以下操作：
* 编译各微服务源码并构建 Docker 镜像；
* 启动 Nacos 服务中心、3 个独立 MySQL 数据库容器；
* 启动所有微服务容器并注册到 Nacos；
* 等待服务健康检查通过，输出最终服务状态和访问地址。

### 3. 创建初始学生和课程数据

```sh
# 1. 赋予测试脚本执行权限
chmod +x test-services.sh

# 2. 执行基础功能测试（创建初始学生和课程数据）
./test-services.sh
```

测试内容：
* ✅ 新增 / 查询学生（验证 user-service）；
* ✅ 新增 / 查询课程（验证 catalog-service）；
* ✅ 学生选课 / 查询选课记录（验证 enrollment-service 及服务间通信）；
* ✅ 异常场景测试（学生不存在、课程不存在时的错误处理）。

## OpenFeign 配置说明

OpenFeign 是 Spring Cloud 提供的声明式 HTTP 客户端，用于简化服务间通信。在本项目中，选课服务（enrollment-service）通过 OpenFeign 调用用户服务和课程目录服务。

### 1. 核心依赖配置

在 `enrollment-service/pom.xml` 中添加 OpenFeign 和 Resilience4j 依赖：

```xml
<!-- OpenFeign 依赖 -->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-openfeign</artifactId>
</dependency>

<!-- Resilience4j 依赖（用于熔断降级） -->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-circuitbreaker-resilience4j</artifactId>
</dependency>
```

### 2. 启用 OpenFeign

在 `enrollment-service` 的主类中添加 `@EnableFeignClients` 注解：

```java
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients  // 启用 OpenFeign
public class EnrollmentApplication {
    public static void main(String[] args) {
        SpringApplication.run(EnrollmentApplication.class, args);
    }
}
```

### 3. Feign 客户端接口定义

创建 Feign 客户端接口，定义服务间调用的 API：

```java
// 用户服务客户端
@FeignClient(name = "user-service", fallback = UserServiceClientFallback.class)
public interface UserServiceClient {
    @GetMapping("/api/students/studentId/{studentId}")
    Map<String, Object> getStudentById(@PathVariable("studentId") String studentId);
}

// 课程目录服务客户端
@FeignClient(name = "catalog-service", fallback = CatalogServiceClientFallback.class)
public interface CatalogServiceClient {
    @GetMapping("/api/courses/{courseId}")
    Map<String, Object> getCourseById(@PathVariable("courseId") String courseId);
}
```

### 4. 熔断降级实现

为每个 Feign 客户端创建 fallback 实现类，处理服务不可用时的降级逻辑：

```java
// 用户服务客户端降级实现
@Component
public class UserServiceClientFallback implements UserServiceClient {
    private static final Logger log = LoggerFactory.getLogger(UserServiceClientFallback.class);

    @Override
    public Map<String, Object> getStudentById(String studentId) {
        log.warn("UserServiceClient fallback: getStudentById called with studentId={}, but user-service is unavailable", studentId);
        throw new RuntimeException("User service is temporarily unavailable, please try again later");
    }
}

// 课程目录服务客户端降级实现
@Component
public class CatalogServiceClientFallback implements CatalogServiceClient {
    private static final Logger log = LoggerFactory.getLogger(CatalogServiceClientFallback.class);

    @Override
    public Map<String, Object> getCourseById(String courseId) {
        log.warn("CatalogServiceClient fallback: getCourseById called with courseId={}, but catalog-service is unavailable", courseId);
        throw new RuntimeException("Catalog service is temporarily unavailable, please try again later");
    }
}
```

### 5. 使用 Feign 客户端

在服务中注入并使用 Feign 客户端：

```java
@Service
@Transactional
public class EnrollmentService {
    private final UserServiceClient userServiceClient;
    private final CatalogServiceClient catalogServiceClient;
    private final EnrollmentRepository repository;

    public EnrollmentService(UserServiceClient userServiceClient, CatalogServiceClient catalogServiceClient, EnrollmentRepository repository) {
        this.userServiceClient = userServiceClient;
        this.catalogServiceClient = catalogServiceClient;
        this.repository = repository;
    }

    public EnrollmentRecord enroll(String courseId, String studentId) {
        // 通过 OpenFeign 调用用户服务
        Map<String, Object> studentResponse = userServiceClient.getStudentById(studentId);
        
        // 通过 OpenFeign 调用课程目录服务
        Map<String, Object> courseResponse = catalogServiceClient.getCourseById(courseId);
        
        // 处理选课逻辑...
    }
}
```

## 负载均衡测试

### 测试脚本

使用 `test-load-balance.sh` 脚本进行负载均衡测试：

```sh
# 1. 赋予脚本执行权限
chmod +x test-load-balance.sh

# 2. 执行负载均衡测试
./test-load-balance.sh
```

### 测试原理

1. 脚本自动验证 user-service 的 3 个实例是否正常运行
2. 通过 enrollment-service 调用 user-service 执行选课操作
3. 统计不同 user-service 实例处理的请求数量
4. 验证请求是否均匀分布到不同实例

### 测试结果

![c369ae9baf28766a8ae5311de0704bb2](D:\腾讯电脑管家文件搬家\微信聊天文件搬家\Users\20168\xwechat_files\wxid_6hj4g9hwluud22_95f7\temp\RWTemp\2025-12\11203f1f0463db1a7325bf8568c7275f\c369ae9baf28766a8ae5311de0704bb2.png)

![6eb4e867d1fe3306ac9e65c5cd424759](D:\腾讯电脑管家文件搬家\微信聊天文件搬家\Users\20168\xwechat_files\wxid_6hj4g9hwluud22_95f7\temp\RWTemp\2025-12\11203f1f0463db1a7325bf8568c7275f\6eb4e867d1fe3306ac9e65c5cd424759.png)

从日志可以看出，请求被均匀分配到了 user-service 的 3 个实例：
- user-service-1: 处理了 1 个请求
- user-service-2: 处理了 1 个请求
- user-service-3: 处理了 1 个请求

这表明 OpenFeign 结合 Nacos 实现了良好的负载均衡效果，请求按轮询策略均匀分发到不同实例。

## 熔断降级测试

### 测试脚本

使用 `Fallback.sh` 脚本进行熔断降级测试：

```sh
# 1. 赋予脚本执行权限
chmod +x Fallback.sh

# 2. 执行熔断降级测试
./Fallback.sh
```

### 测试原理

1. 脚本自动停止所有 user-service 实例
2. 通过 enrollment-service 调用 user-service 执行选课操作
3. 验证是否触发 fallback 机制
4. 重启 user-service 实例，验证服务恢复后是否正常工作

### 测试结果

![1abece519e400a2f6f98e689afa01f59](D:\腾讯电脑管家文件搬家\微信聊天文件搬家\Users\20168\xwechat_files\wxid_6hj4g9hwluud22_95f7\temp\RWTemp\2025-12\11203f1f0463db1a7325bf8568c7275f\1abece519e400a2f6f98e689afa01f59.png)

![37a3ae3c0b26713b0371b25341060a7c](D:\腾讯电脑管家文件搬家\微信聊天文件搬家\Users\20168\xwechat_files\wxid_6hj4g9hwluud22_95f7\temp\RWTemp\2025-12\11203f1f0463db1a7325bf8568c7275f\37a3ae3c0b26713b0371b25341060a7c.png)

从日志可以看出：
1. 当 user-service 停止后，请求触发了 fallback 机制
2. 当 user-service 重启后，请求恢复正常处理

这表明 OpenFeign 的熔断降级机制正常工作，能够在服务不可用时提供友好的错误提示，保障系统的可用性。

## OpenFeign vs RestTemplate 对比分析

| 特性 | OpenFeign | RestTemplate |
|------|-----------|--------------|
| **编程模型** | 声明式，基于接口注解 | 命令式，需要手动构建请求 |
| **服务发现集成** | 自动集成 Ribbon/Nacos 负载均衡 | 需要手动添加 `@LoadBalanced` 注解 |
| **代码可读性** | 高，接口定义清晰，易于维护 | 低，需要编写大量模板代码 |
| **熔断降级支持** | 内置支持，通过 `fallback` 属性配置 | 需要手动集成 Hystrix/Resilience4j |
| **错误处理** | 自动处理 HTTP 错误，可自定义 fallback | 需要手动处理异常和错误码 |
| **日志支持** | 内置详细日志，可配置日志级别 | 需要手动添加日志记录 |
| **扩展性** | 支持自定义拦截器、编码器、解码器 | 支持，但需要手动配置 |

### 本项目选择 OpenFeign 的原因

1. **简化代码**：通过声明式接口减少了大量模板代码，提高开发效率
2. **更好的可维护性**：接口定义清晰，便于团队协作和代码维护
3. **内置负载均衡**：自动集成 Nacos 负载均衡，无需额外配置
4. **完善的熔断降级**：通过 fallback 机制提供良好的故障容错能力
5. **与 Spring Cloud 生态完美集成**：与 Nacos、Resilience4j 等组件无缝协作

## 常见问题排查

### 1. OpenFeign 调用失败

* 检查 Feign 客户端接口定义是否正确
* 确认服务名是否与 Nacos 中注册的服务名一致
* 检查 fallback 类是否添加了 `@Component` 注解
* 查看日志确认具体错误信息：`docker logs enrollment-service`

### 2. 负载均衡不生效

* 确认 Nacos 控制台中服务多实例已注册
* 检查 Feign 客户端是否使用了服务名而非 IP:端口
* 查看日志确认请求是否分发到不同实例

### 3. 熔断降级未触发

* 确认 Resilience4j 依赖已正确添加
* 检查 fallback 类是否正确实现了 Feign 客户端接口
* 确认服务确实不可用（可通过停止服务实例测试）

## 运维指令

### 停止所有服务

```sh
docker-compose down
```

### 停止并删除数据卷（重置环境）

```sh
docker-compose down -v
```

### 查看服务日志

```sh
# 查看单个服务日志
docker-compose logs -f enrollment-service

# 查看所有服务日志
docker-compose logs -f
```

### 重启单个服务

```sh
docker-compose restart user-service
```

## 扩展说明

* **多环境部署**：可通过 Nacos 命名空间（namespace）区分开发 / 测试 / 生产环境
* **服务监控**：可集成 Spring Boot Admin 监控微服务健康状态
* **配置中心**：可基于 Nacos 配置中心实现配置动态刷新，无需重启服务
* **链路追踪**：可集成 Zipkin 或 SkyWalking 实现分布式链路追踪
