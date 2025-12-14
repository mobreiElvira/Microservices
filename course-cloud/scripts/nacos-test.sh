#!/bin/bash
# 脚本功能：自动化测试Nacos服务注册（dev命名空间）与微服务调用

# 1. 启动所有服务
echo "=== 启动所有服务 ==="
docker compose up -d  # 新版Docker Compose语法（无短横线），旧版用docker-compose

# 2. 等待服务启动（Nacos+数据库初始化需足够时间，建议90秒）
echo "=== 等待服务初始化（90秒） ==="
sleep 90

# 3. 检查Nacos控制台可用性
echo "=== 验证Nacos控制台 ==="
curl -I http://localhost:8848/nacos/  # 仅获取响应头，快速验证是否可达

# 4. 检查catalog-service的注册状态（指定dev命名空间）
echo "=== 检查catalog-service（dev命名空间）注册情况 ==="
curl -X GET "http://localhost:8848/nacos/v1/ns/instance/list?serviceName=catalog-service&namespaceId=dev"

# 5. 检查user-service的注册状态（可选，验证多实例）
echo "=== 检查user-service（dev命名空间）注册情况 ==="
curl -X GET "http://localhost:8848/nacos/v1/ns/instance/list?serviceName=user-service&namespaceId=dev"

# 6. 测试enrollment-service接口（循环10次，验证负载均衡）
echo "=== 测试enrollment-service接口调用（10次） ==="
for i in {1..10}; do
  echo -e "\n第 $i 次请求结果："
  # 调用测试接口，提取user-service实例信息（验证负载均衡）
  curl -s http://localhost:8083/api/enrollments/test | jq '.["user-service"]'
done

# 7. 查看容器运行状态
echo -e "\n=== 容器运行状态 ==="
docker compose ps