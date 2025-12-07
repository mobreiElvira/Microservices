#!/bin/bash
# run.sh - 构建并启动 course-cloud 微服务

set -e

echo "========================================"
echo "Course Cloud 微服务启动脚本"
echo "========================================"

# 构建并启动 Docker 容器
echo ""
echo ">>> 步骤 1/2: 构建并启动 Docker 容器..."
docker compose up -d --build

# 等待服务启动
echo ""
echo ">>> 步骤 2/2: 等待服务启动..."
echo "  等待数据库健康检查（约15秒）..."
sleep 15

# 显示服务状态
echo ""
echo "========================================"
echo "✓ 所有服务已启动"
echo "========================================"
docker compose ps

# 显示访问信息
echo ""
echo "服务访问地址："
echo "  - User Service:       http://localhost:8081/api/students"
echo "  - Catalog Service:    http://localhost:8082/api/courses"
echo "  - Enrollment Service: http://localhost:8083/api/enrollments"
echo ""
echo "查看日志: docker-compose logs -f"
echo "停止服务: docker-compose down"
echo "删除数据: docker-compose down -v"
