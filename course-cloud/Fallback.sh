#!/bin/bash
set -euo pipefail

# 颜色常量
GREEN="\033[32m"
YELLOW="\033[33m"
BLUE="\033[34m"
RED="\033[31m"
NC="\033[0m"

# 配置参数
ENROLLMENT_SERVICE_URL="http://localhost:8083/api/enrollments"
STUDENT_ID="2024002"
COURSE_ID="7f53de81-1666-4094-81e8-166b00ca7e86"
USER_SERVICE="http://localhost:8081"

# 工具检查
check_dependency() {
    command -v $1 &> /dev/null || { echo -e "${RED}❌ 未找到 $1${NC}"; exit 1; }
}
check_dependency "curl"
check_dependency "docker"

echo -e "${BLUE}🚀 User Service 熔断降级测试开始${NC}"

# 1. 停止所有 User Service
echo -e "\n${YELLOW}📌 步骤1：停止 User Service${NC}"
for service in user-service-1 user-service-2 user-service-3; do
    if docker inspect -f '{{.State.Running}}' $service &> /dev/null; then
        docker stop $service > /dev/null
        echo -e "✅ $service 已停止"
    else
        echo -e "ℹ️ $service 已停止"
    fi
done

# 2. 触发 fallback
echo -e "\n${YELLOW}📌 步骤2：触发 fallback${NC}"
echo -e "检测日志关键词：'验证学生时服务降级'"

for i in {1..2}; do
    echo -e "\n🔄 第 $i 次请求"
    response=$(curl -s -w "\n%{http_code}" -X POST $ENROLLMENT_SERVICE_URL \
        -H "Content-Type: application/json" \
        -d "{\"studentId\": \"$STUDENT_ID\", \"courseId\": \"$COURSE_ID\"}")
    
    sleep 1
    log_check=$(docker logs enrollment-service --since 2s 2>&1 | grep "验证学生时服务降级" | tail -1)
    
    if [ -n "$log_check" ]; then
        echo -e "${GREEN}✅ fallback 触发成功${NC}"
        echo -e "📝 日志：$log_check"
        break
    else
        echo -e "${YELLOW}⚠️  未检测到降级日志${NC}"
        sleep 2
    fi
done

# 3. 查看降级日志
echo -e "\n${YELLOW}📌 步骤3：查看降级日志${NC}"
fallback_logs=$(docker logs enrollment-service --since 2m 2>&1 | grep "验证学生时服务降级" | tail -5)
if [ -n "$fallback_logs" ]; then
    echo -e "${GREEN}✅ 降级日志：${NC}"
    echo "$fallback_logs"
else
    echo -e "${YELLOW}⚠️  无降级日志${NC}"
fi

# 4. 重启并验证恢复
echo -e "\n${YELLOW}📌 步骤4：重启并验证恢复${NC}"
for service in user-service-1 user-service-2 user-service-3; do
    docker start $service > /dev/null && echo -e "✅ $service 启动"
done

echo -e "\n⏳ 等待30秒服务恢复..."
for i in {1..30}; do echo -n "."; sleep 1; done; echo ""

# 创建学生
echo -e "\n📝 创建学生..."
STUDENT_RESPONSE=$(curl -s -X POST ${USER_SERVICE}/api/students \
    -H "Content-Type: application/json" \
    -d '{"username":"lisi","email":"lisi@example.edu.cn","studentId":"2024002","name":"李四","major":"软件工程","grade":2024}')
echo "学生创建响应：$STUDENT_RESPONSE"

# 测试正常请求
echo -e "\n🔄 发送正常选课请求..."
normal_response=$(curl -s -w "\n%{http_code}" -X POST $ENROLLMENT_SERVICE_URL \
    -H "Content-Type: application/json" \
    -d "{\"studentId\": \"$STUDENT_ID\", \"courseId\": \"$COURSE_ID\"}")
normal_http_code=$(echo "$normal_response" | tail -n 1)

if [ "$normal_http_code" -eq 200 ] || [ "$normal_http_code" -eq 201 ]; then
    echo -e "${GREEN}✅ 服务恢复成功 (HTTP $normal_http_code)${NC}"
else
    echo -e "${YELLOW}⚠️  服务未完全恢复 (HTTP $normal_http_code)${NC}"
fi

# 总结
echo -e "\n${BLUE}📋 测试总结：${NC}"
echo -e "1. User Service 停止：✅ 完成"
echo -e "2. Fallback 触发：$(if [ -n "$fallback_logs" ]; then echo "✅ 成功"; else echo "❌ 失败"; fi)"
echo -e "3. 服务恢复：$(if [ "$normal_http_code" -eq 200 ] || [ "$normal_http_code" -eq 201 ]; then echo "✅ 成功"; else echo "⚠️  未完全恢复"; fi)"

echo -e "\n${GREEN}✅ 测试完成${NC}"
