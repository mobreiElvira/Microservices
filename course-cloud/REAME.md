# course-cloud-v09

course-cloud是一个基于Spring Cloud的微服务架构课程管理系统，包含用户管理、课程目录和选课功能。

## 项目结构

course-cloud-main/
├── user-service/        # 用户服务 - 管理用户信息和认证
├── catalog-service/     # 课程目录服务 - 管理课程信息
├── enrollment-service/  # 选课服务 - 管理选课信息
├── gateway-service/     # 网关服务 - 统一API入口
├── docker-compose.yml   # Docker容器编排配置
├── run.sh               # 一键启动脚本



## 技术栈

- **框架**: Spring Boot 2.x + Spring Cloud
- **服务注册与发现**: Nacos
- **数据库**: MySQL 8.x
- **ORM框架**: JPA/Hibernate
- **服务调用**: OpenFeign
- **容错机制**: Resilience4j (熔断器、重试)
- **认证**: JWT
- **容器化**: Docker
- **API网关**: Spring Cloud Gateway

## 快速开始

### 前置条件

- Docker和Docker Compose已安装
- Maven已安装（用于本地编译）
- Java 8+

### 一键启动

```bash
./run.sh
```

脚本会自动执行以下步骤：
1. 编译所有微服务
2. 构建Docker镜像
3. 启动所有Docker容器
4. 等待服务初始化完成

### 手动启动

#### 1. 编译服务

```bash
for service in user-service catalog-service enrollment-service; do
    cd $service && mvn clean package -DskipTests && cd ..
done
```

#### 2. 启动Docker容器

```bash
docker-compose up -d --build
```

## 服务访问

服务启动后，可以通过以下地址访问：

| 服务名称     | 端口 | 访问地址                                 |
| ------------ | ---- | ---------------------------------------- |
| 用户服务     | 8081 | http://localhost:8081/api/users/students |
| 课程目录服务 | 8082 | http://localhost:8082/api/courses        |
| 选课服务     | 8083 | http://localhost:8083/api/enrollments    |
| 网关服务     | 8090 | http://localhost:8090/api/               |
| Nacos控制台  | 8848 | http://localhost:8848/nacos              |

## API文档

### 用户服务 (user-service)

- `GET /api/users/students` - 获取学生列表
- `GET /api/users/students/{id}` - 获取单个学生信息
- `POST /api/users/students` - 创建学生
- `PUT /api/users/students/{id}` - 更新学生信息
- `DELETE /api/users/students/{id}` - 删除学生

### 课程目录服务 (catalog-service)

- `GET /api/courses` - 获取课程列表
- `GET /api/courses/{id}` - 获取单个课程信息
- `POST /api/courses` - 创建课程
- `PUT /api/courses/{id}` - 更新课程信息
- `DELETE /api/courses/{id}` - 删除课程

### 选课服务 (enrollment-service)

- `GET /api/enrollments` - 获取选课列表
- `GET /api/enrollments/{id}` - 获取单个选课信息
- `POST /api/enrollments` - 创建选课
- `DELETE /api/enrollments/{id}` - 删除选课
- `GET /api/enrollments/students/{studentId}` - 获取学生选课列表
- `GET /api/enrollments/courses/{courseId}` - 获取课程选课列表

## 配置管理

所有服务的配置文件位于各服务目录下的`src/main/resources/application.yml`。主要配置项包括：

- 数据库连接信息
- Nacos服务注册配置
- Feign客户端配置
- Resilience4j容错配置
- JWT认证配置

## 开发指南

### 本地开发环境

1. 启动Nacos服务
2. 启动MySQL数据库
3. 修改各服务配置文件中的数据库连接和Nacos地址
4. 分别启动各微服务

### 日志查看

```bash
docker-compose logs -f
```

### 停止服务

```bash
docker-compose down
```

### 删除数据

```bash
docker-compose down -v
```

## 监控与管理

- **Nacos控制台**: 用于服务注册与发现管理
- **Spring Boot Actuator**: 提供各服务的健康检查和监控端点

## 系统架构

```
+----------------+     +----------------+     +------------------+
|  user-service  |     | catalog-service|     |enrollment-service| 
|    (用户管理)   |     |   (课程管理)    |      |     (选课管理)    |
+-------+--------+     +--------+-------+     +---------+--------+
        |                       |                       |
        |                       |                       |
        +-----------------------+-----------------------+
                                |
                                v
                       +--------+--------+
                       | gateway-service |
                       |    (API网关)     |
                       +--------+--------+
                                |
                                v
                         +------+------+
                         |    客户端    |
                         +-------------+
```

## 注意事项

1. 首次启动时，系统会自动创建数据库表结构
2. 默认数据库用户名和密码可在docker-compose.yml中修改
3. Nacos默认用户名密码为nacos/nacos
4. 服务启动后需要等待约15秒完成初始化

