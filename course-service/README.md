# 校园选课系统微服务项目 README.md

## 1. 项目简介

### 项目名称和版本

校园选课系统微服务 - v1.0.0

### 基于哪个版本拆分

基于校园选课系统单体应用版本拆分为微服务架构，按业务领域拆分为课程目录服务（catalog-service）和选课服务（enrollment-service）。

### 微服务架构说明

项目采用微服务架构模式，将原有单体系统拆分为两个核心微服务：

- **catalog-service（8081）**：负责课程全生命周期管理（课程 CRUD、讲师关联、排课信息维护、选课容量管理等），独立对接`catalog_db`数据库，对外提供课程相关 API。
- **enrollment-service（8082）**：负责学生信息管理、选课 / 退课操作、选课记录查询及统计，通过 RestTemplate 远程调用 catalog-service 验证课程合法性、更新选课人数，独立对接`enrollment_db`数据库。

## 2. 架构图

```plaintext
客户端
  ↓
  ├─→ catalog-service (8081) → catalog_db (3307)
  │    ├── 课程管理（CRUD、容量维护）
  │    ├── 讲师信息关联
  │    └── 排课信息管理
  │
  └─→ enrollment-service (8082) → enrollment_db (3308)
       ├── 学生管理（信息CRUD、专业/年级筛选）
       ├── 选课管理（选课/退课、记录查询）
       ├── 统计分析（选课人数、状态校验）
       └── HTTP调用 → catalog-service（验证课程、更新选课人数）
```

## 3. 技术栈

- Spring Boot 3.5.6
- Java 17
- MySQL 8.4
- Docker & Docker Compose
- RestTemplate（服务间通信）
- JUnit 5（单元测试）
- Lombok（简化代码）

## 4. 环境要求

- JDK 17+
- Maven 3.8+
- Docker 20.10+
- Docker Compose 2.0+
- MySQL 8.4（本地部署时需手动配置）

## 5. 构建和运行步骤

### Docker Compose 部署

```bash
# 构建镜像并启动所有服务
docker-compose up -d

# 查看服务运行状态
docker-compose ps

# 查看服务日志
docker-compose logs -f catalog-service
docker-compose logs -f enrollment-service
```

## 6. API 文档

### 6.1 catalog-service 接口

| 接口路径                                                 | 请求方法 | 功能描述                 | 请求参数                          |
| -------------------------------------------------------- | -------- | ------------------------ | --------------------------------- |
| `/api/courses`                                           | GET      | 获取所有课程列表         | 无                                |
| `/api/courses/{courseId}`                                | GET      | 获取指定课程详情         | 路径参数：courseId                |
| `/api/courses/code/{code}`                               | GET      | 按课程代码查询           | 路径参数：code（如 CS301）        |
| `/api/courses/instructor/{instructorId}`                 | GET      | 按讲师 ID 查询课程       | 路径参数：instructorId（如 T003） |
| `/api/courses/available`                                 | GET      | 查询有剩余容量的课程     | 无                                |
| `/api/courses/search?title={keyword}`                    | GET      | 课程标题模糊查询         | 请求参数：keyword（如 Java）      |
| `/api/courses/available/count`                           | GET      | 统计有剩余容量的课程数量 | 无                                |
| `/api/courses/instructor/{instructorId}/available/count` | GET      | 统计讲师剩余课程数量     | 路径参数：instructorId            |
| `/api/courses/{courseId}/enrolled/increment`             | PUT      | 课程选课人数增加         | 路径参数：courseId                |
| `/api/courses/{courseId}/enrolled/decrement`             | PUT      | 课程选课人数减少         | 路径参数：courseId                |

### 6.2 enrollment-service 接口

| 接口路径                                                 | 请求方法 | 功能描述           | 请求参数                              |
| -------------------------------------------------------- | -------- | ------------------ | ------------------------------------- |
| `/api/students`                                          | GET      | 获取所有学生列表   | 无                                    |
| `/api/students/{studentId}`                              | GET      | 获取指定学生详情   | 路径参数：studentId                   |
| `/api/students/email/{email}`                            | GET      | 按邮箱查询学生     | 路径参数：email                       |
| `/api/students/major/{major}`                            | GET      | 按专业查询学生     | 路径参数：major（如计算机科学与技术） |
| `/api/students/grade/{grade}`                            | GET      | 按年级查询学生     | 路径参数：grade（如 2024 级）         |
| `/api/enrollments`                                       | GET      | 获取所有选课记录   | 无                                    |
| `/api/enrollments/course/{courseId}`                     | GET      | 按课程查询选课记录 | 路径参数：courseId                    |
| `/api/enrollments/student/{studentNo}`                   | GET      | 按学生查询选课记录 | 路径参数：studentId                   |
| `/api/enrollments/status?courseId={cid}&studentId={sid}` | GET      | 检查学生选课状态   | 请求参数：courseId、studentId         |
| `/api/enrollments/course/{courseId}/count`               | GET      | 统计课程选课人数   | 路径参数：courseId                    |

## 7. 测试说明

### 7.1 自动化测试

项目包含完整的自动化测试脚本，覆盖服务可用性、功能接口、异常场景等：

```bash
# 运行测试脚本
bash test-services.sh
```

### 7.2 测试覆盖范围

1. **服务可用性检查**：验证 catalog-service 和 enrollment-service 是否正常启动。
2. **功能测试**：覆盖课程、学生、选课模块的所有核心接口（如课程查询、学生筛选、选课状态校验等）。
3. **异常场景测试**：验证不存在的课程 / 学生查询、非法选课操作等异常处理逻辑。

### 7.3 测试结果

```plaintext
总测试用例: 23
[SUCCESS] 通过: 23
[ERROR] 失败: 0
```

![image-20251126112645689](C:\Users\20168\AppData\Roaming\Typora\typora-user-images\image-20251126112645689.png)

## 8. 遇到的问题和解决方案

### 问题 1：服务间通信数据解析异常

**现象**：enrollment-service 调用 catalog-service 返回的课程数据字段不匹配，导致反序列化失败。

**解决方案**：统一定义 DTO 数据结构（如 CourseDTO、StudentDTO），确保服务间数据格式一致；添加字段兼容性处理（如 @JsonProperty 注解）。

### 问题 2：Docker 部署时数据库连接超时

**现象**：服务启动后无法连接 Docker 中的 MySQL 容器。

**解决方案**：在 Docker Compose 中配置容器间网络别名，服务通过容器名而非[localhost](https://localhost/)访问数据库；调整数据库连接池参数，增加超时等待时间。

### 问题 3：选课人数更新并发冲突

**现象**：多用户同时选课导致课程容量统计错误。

**解决方案**：在 catalog-service 中对选课人数更新操作添加数据库乐观锁（如 @Version 注解），确保并发更新的原子性。

### 问题 4：远程调用失败导致选课数据不一致

**现象**：enrollment-service 创建选课记录后，调用 catalog-service 更新人数失败，导致数据不一致。

**解决方案**：引入事务补偿机制，若远程调用失败则回滚选课记录；后续计划接入消息队列（RabbitMQ）实现最终一致性。

## 9. **截图**（至少 5 张）：

• Docker 容器运行截图（docker-compose ps）

![image-20251126112343018](C:\Users\20168\AppData\Roaming\Typora\typora-user-images\image-20251126112343018.png)

• catalog-service API 测试截图

![image-20251126112449955](C:\Users\20168\AppData\Roaming\Typora\typora-user-images\image-20251126112449955.png)

• enrollment-service API 测试截图

![image-20251126112505340](C:\Users\20168\AppData\Roaming\Typora\typora-user-images\image-20251126112505340.png)

• 选课成功的响应截图（证明服务间调用成功）

![image-20251126112519650](C:\Users\20168\AppData\Roaming\Typora\typora-user-images\image-20251126112519650.png)

• 课程不存在时的错误处理截图

![image-20251126112530367](C:\Users\20168\AppData\Roaming\Typora\typora-user-images\image-20251126112530367.png)