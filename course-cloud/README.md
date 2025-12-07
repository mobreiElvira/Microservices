# 校园选课系统 - 3services

**项目名称**: course-cloud

**版本**: v07

**演进基础**: 基于单体应用 course-v07 拆分的微服务架构实践项目

## 项目简介

本项目将传统单体选课系统按业务域拆分为 **用户服务、课程目录服务、选课服务** 三大核心微服务，通过 Nacos 实现服务注册发现与负载均衡，采用 Docker 容器化部署保障环境一致性，各服务独立数据库存储实现数据隔离，基于 RestTemplate 实现服务间可靠通信，最终达成 **业务解耦、弹性扩展、故障容错** 的微服务核心目标。

### 整体架构图（Mermaid 可视化）

![image-20251207204345299](C:\Users\20168\AppData\Roaming\Typora\typora-user-images\image-20251207204345299.png)

### 架构核心说明

1. **客户端层**：接收用户操作请求，通过统一入口发起服务调用；

2. **服务注册发现层**：Nacos 作为核心中间件，负责服务注册、健康检查、负载均衡和故障转移，是微服务通信的 “大脑”；

3. **微服务层**：按业务域拆分的独立服务，可单独部署、扩容、迭代，互不影响；

4. **数据存储层**：各服务对应独立数据库，实现数据隔离，避免单点故障影响全系统。

### 技术栈

| 技术 / 框架                 | 版本 / 说明                     |
| ----------------------- | --------------------------- |
| Spring Boot             | 3.3.4（微服务基础框架）              |
| Java                    | 17（开发语言，适配 Spring Boot 3.x） |
| MySQL                   | 8.4（关系型数据库，数据持久化）           |
| Docker & Docker Compose | 20.10+/2.0+（容器化部署，环境一致性）    |
| RestTemplate            | 内置（服务间 HTTP 通信，支持负载均衡）      |
| Nacos                   | 2.2.3（服务注册发现 / 负载均衡 / 健康检查） |
| Spring Cloud Alibaba    | 适配 Nacos 生态，微服务注册发现支持       |
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

### 服务详情

1. 用户服务（user-service）

* **核心端口**：8081（容器内 / 宿主机映射一致）

* **数据库**：user\_db（宿主机端口 3306）

* **核心功能**：学生 / 用户信息的增删改查，支持多实例部署与负载均衡

* **关键 API**：

| 接口                              | 方法     | 描述               |
| ------------------------------- | ------ | ---------------- |
| `/api/students`                 | GET    | 获取所有学生列表         |
| `/api/students/{id}`            | GET    | 按 ID 查询学生        |
| `/api/students/studentId/{sid}` | GET    | 按学号查询学生          |
| `/api/students`                 | POST   | 新增学生（JSON 入参）    |
| `/api/students/{id}`            | PUT    | 更新学生信息           |
| `/api/students/{id}`            | DELETE | 删除学生             |
| `/api/students/test`            | GET    | 负载均衡测试接口（返回实例信息） |
| `/actuator/health`              | GET    | 健康检查接口（Nacos 探测） |

#### 2. 课程目录服务（catalog-service）

* **核心端口**：8082（容器内 / 宿主机映射一致）

* **数据库**：catalog\_db（宿主机端口 3307）

* **核心功能**：课程信息的增删改查，支持多实例部署与故障转移

* **关键 API**：

| 接口                         | 方法     | 描述               |
| -------------------------- | ------ | ---------------- |
| `/api/courses`             | GET    | 获取所有课程列表         |
| `/api/courses/{id}`        | GET    | 按 ID 查询课程        |
| `/api/courses/code/{code}` | GET    | 按课程代码查询          |
| `/api/courses`             | POST   | 新增课程（JSON 入参）    |
| `/api/courses/{id}`        | PUT    | 更新课程信息           |
| `/api/courses/{id}`        | DELETE | 删除课程             |
| `/api/courses/test`        | GET    | 负载均衡测试接口（返回实例信息） |
| `/actuator/health`         | GET    | 健康检查接口（Nacos 探测） |

#### 3. 选课服务（enrollment-service）

* **核心端口**：8083（容器内 / 宿主机映射一致）

* **数据库**：enrollment\_db（宿主机端口 3308）

* **核心功能**：选课 / 退课逻辑处理，依赖用户服务验证学生合法性、课程服务验证课程有效性

* **关键 API**：

| 接口                               | 方法     | 描述                    |
| -------------------------------- | ------ | --------------------- |
| `/api/enrollments`               | GET    | 获取所有选课记录              |
| `/api/enrollments/course/{cid}`  | GET    | 按课程 ID 查询选课记录         |
| `/api/enrollments/student/{sid}` | GET    | 按学生 ID 查询选课记录         |
| `/api/enrollments`               | POST   | 学生选课（需传入学生 ID、课程 ID）  |
| `/api/enrollments/{id}`          | DELETE | 学生退课                  |
| `/api/enrollments/test`          | GET    | 故障转移测试接口（调用用户 / 课程服务） |
| `/actuator/health`               | GET    | 健康检查接口（Nacos 探测）      |

## 快速上手

### 1. 项目结构

```
course-cloud/
├── README.md               # 项目说明文档（本文档）
├── docker-compose.yml      # Docker 多容器编排配置
├── run.sh                  # 一键启动脚本（构建+启动所有服务）
├── test-services.sh        # 基础功能测试脚本
├── nacos-test.sh           # 负载均衡/故障转移测试脚本
├── user-service/           # 用户服务模块（独立 Maven 项目）
│   ├── src/                # 源码目录（Controller/Service/Mapper/Model）
│   ├── Dockerfile          # 容器构建文件
│   └── pom.xml             # Maven 依赖配置
├── catalog-service/        # 课程服务模块（独立 Maven 项目）
│   ├── src/
│   ├── Dockerfile
│   └── pom.xml
└── enrollment-service/     # 选课服务模块（独立 Maven 项目）
   ├── src/
   ├── Dockerfile
   └── pom.xml
```

### 2. 一键启动所有服务

```sh
#1. 首次运行赋予脚本执行权限
chmod +x run.sh

#2. 构建 Docker 镜像并启动所有服务（含 Nacos、数据库、微服务）
./run.sh
```

脚本自动完成以下操作：

* 编译各微服务源码并构建 Docker 镜像；

* 启动 Nacos 服务中心、3 个独立 MySQL 数据库容器；

* 启动所有微服务容器并注册到 Nacos；

* 等待服务健康检查通过，输出最终服务状态和访问地址。

### 3. 验证服务启动成功

```sh
#查看所有运行中的容器
docker ps
#访问 Nacos 控制台验证服务注册（默认账号/密码：nacos/nacos）
#open http://localhost:8848/nacos  # Linux
或直接在浏览器输入：http://localhost:8848/nacos
```

在 Nacos 控制台「服务管理→服务列表」中，应能看到 `user-service`、`catalog-service`、`enrollment-service` 三个服务均已注册（每个服务默认 1 个实例）。

## 测试指南

### 1. 基础功能测试（验证服务可用性）

```sh
# 1. 赋予测试脚本执行权限
chmod +x test-services.sh

# 2. 执行基础功能测试
./test-services.sh
```

测试内容：

* ✅ 新增 / 查询学生（验证 user-service）；

* ✅ 新增 / 查询课程（验证 catalog-service）；

* ✅ 学生选课 / 查询选课记录（验证 enrollment-service 及服务间通信）；

* ✅ 异常场景测试（学生不存在、课程不存在时的错误处理）。

### 2. 负载均衡测试（验证多实例请求分发）

```sh
# 1. 赋予测试脚本执行权限

chmod +x nacos-test.sh

# 2. 执行负载均衡测试

./nacos-test.sh
```

核心验证逻辑：

* 自动启动额外的 `user-service-2` 和 `catalog-service-2` 实例；

* 通过 Nacos API 展示多实例注册状态（每个服务 2 个实例）；

* 10 次循环调用接口，验证请求均匀分发到不同实例；

* 输出实例调用统计结果，直观展示负载均衡效果。

### 3. 故障转移测试（核心验证高可用）

手动测试步骤（或通过 `nacos-test.sh` 自动执行）：

```sh
\# 步骤 1：启动额外实例（模拟多实例部署）

docker run -d --name catalog-service-2 --network course-cloud\_course-network -p 8084:8082 -e SPRING\_DATASOURCE\_URL=jdbc:mysql://catalog-db:3306/catalog\_db?useSSL=false\\\&allowPublicKeyRetrieval=true -e SPRING\_CLOUD\_NACOS\_DISCOVERY\_SERVER-ADDR=nacos:8848 course-cloud-catalog-service:1.1.0

\# 步骤 2：查看当前实例（确认 2 个 catalog-service 实例）

docker ps | grep catalog-service

\# 步骤 3：停止其中一个实例（模拟故障）

docker stop course-cloud-catalog-service-1

\# 步骤 4：等待 15 秒（Nacos 检测并剔除故障实例）

sleep 15

\# 步骤 5：验证故障转移（请求自动路由到健康实例）

for i in {1..5}; do

 echo "故障后第 \$i 次调用："

 curl -s http://localhost:8084/api/courses/test | jq '.port, .status, .hostname'

 sleep 1

 echo "------------------------"

done
```

**预期效果**：

* 故障实例停止后，Nacos 控制台标记该实例为「不健康」；

* 所有请求无报错、无超时，自动路由到剩余健康实例；

* 接口返回 `status=UP`，证明故障转移生效。

## 核心配置说明

### 1. Nacos 服务配置（docker-compose.yml）

```yml
nacos:

 image: nacos/nacos-server:v2.2.3

 container\_name: nacos

 environment:

   - MODE=standalone                # 单机模式（开发/测试环境）

   - JVM\_XMS=256m                   # JVM 内存配置（适配开发机资源）

   - JVM\_XMX=256m

   - JVM\_XMN=128m

 ports:

   - "8848:8848"                    # Nacos 核心服务端口（服务注册发现）

   - "8080:8080"                    # 控制台访问端口

   - "9848:9848"                    # 客户端通信端口

 volumes:

   - nacos-data:/home/nacos/data    # 数据持久化（避免重启丢失注册信息）

   - nacos-logs:/home/nacos/logs    # 日志持久化

 networks:

   - course-network                 # 加入微服务统一网络

 healthcheck:

   test: \["CMD", "curl", "-f", "http://localhost:8848/nacos/"]

   interval: 20s                    # 健康检查间隔

   timeout: 15s                     # 超时时间

   retries: 20                      # 重试次数

   start\_period: 90s                # 启动初始化等待时间（避免误判）

 restart: unless-stopped            # 异常退出自动重启（提高可用性）
```

### 2. 微服务 Nacos 注册配置（application.yml）

以 `catalog-service` 为例：

```yml
spring:

 application:

   name: catalog-service            # 服务名（Nacos 注册唯一标识）

 cloud:

   nacos:

     discovery:

       server-addr: nacos:8848       # Nacos 地址（容器内域名，无需改 IP）

       namespace: dev               # 命名空间（需提前在 Nacos 控制台创建）

       group: DEFAULT\_GROUP         # 服务分组（默认即可）

       instance-id: \${HOSTNAME}     # 实例 ID（用容器名保证唯一）

       heart-beat-interval: 5000    # 心跳间隔 5 秒（Nacos 探测服务存活）

       heart-beat-timeout: 15000    # 心跳超时 15 秒（超时标记为不健康）

       ip-delete-timeout: 30000     # 实例剔除超时 30 秒（不健康实例剔除）

management:

 endpoints:

   web:

     exposure:

       include: health              # 暴露健康检查接口（Nacos 调用）

 endpoint:

   health:

     show-details: always           # 显示健康检查详情（便于排查问题）
```

### 3. 负载均衡配置（enrollment-service）

通过 `@LoadBalanced` 注解开启 RestTemplate 负载均衡：

```java
@Configuration

public class RestTemplateConfig {

   @Bean

   @LoadBalanced  // 关键注解：开启负载均衡，支持通过服务名调用

   public RestTemplate restTemplate() {

       return new RestTemplate();

   }

}
```

使用示例（调用 user-service 接口）：

```java
// 直接通过服务名调用（无需写 IP:端口，Nacos 自动解析并负载均衡）

String userUrl = "http://user-service/api/students/studentId/" + studentId;

Map, Object> studentInfo = restTemplate.getForObject(userUrl, Map.class);
```



| 服务               | 数据库名       | 宿主机端口 | 容器内端口 | 用户名           | 密码             |
| ------------------ | -------------- | ---------- | ---------- | ---------------- | ---------------- |
| user-service       | user\_db       | 3306       | 3306       | user\_user       | user\_pass       |
| catalog-service    | catalog\_db    | 3307       | 3306       | catalog\_user    | catalog\_pass    |
| enrollment-service | enrollment\_db | 3308       | 3306       | enrollment\_user | enrollment\_pass |

## 常见问题排查

### 1. 服务启动失败

* 检查容器日志：`docker logs <容器名>`（如 `docker logs course-cloud-catalog-service-1`）

* 确认数据库容器正常运行：`docker ps | grep mysql`

* 验证 Nacos 连接：访问 `http://localhost:8848/nacos` 确认服务可用

### 2. 负载均衡不生效

* 确认 RestTemplate 加了 `@LoadBalanced` 注解

* 检查 Nacos 控制台，确认服务多实例已注册

* 调用接口时使用服务名（如 `http://catalog-service/api/courses`）而非 IP: 端口

### 3. 故障转移未生效

* 等待足够时间（至少 15 秒），Nacos 需要时间剔除故障实例

* 检查 Nacos 心跳配置（`heart-beat-timeout`/`ip-delete-timeout`）

* 确认实例加入统一网络：`--network course-cloud_course-network`

## 运维指令

### 停止所有服务

```sh
docker-compose down
```

#### 停止并删除数据卷（重置环境）

```sh
docker-compose down -v
```

#### 查看服务日志

```sh
\# 查看单个服务日志

docker-compose logs -f catalog-service

\# 查看所有服务日志

docker-compose logs -f
```

#### 重启单个服务

```sh
docker-compose restart user-service
```

## 扩展说明

* **多环境部署**：可通过 Nacos 命名空间（namespace）区分开发 / 测试 / 生产环境

* **服务监控**：可集成 Spring Boot Admin 监控微服务健康状态

* **熔断降级**：可扩展集成 Sentinel 实现服务熔断，避免雪崩效应

* **配置中心**：可基于 Nacos 配置中心实现配置动态刷新，无需重启服务
