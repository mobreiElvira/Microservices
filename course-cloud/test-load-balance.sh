#!/bin/bash
set -euo pipefail  # 开启严格模式，避免脚本静默失败

# 颜色常量（优化输出可读性）
GREEN="\033[32m"
YELLOW="\033[33m"
BLUE="\033[34m"
RED="\033[31m"
NC="\033[0m"  # 重置颜色

# 关键优化：限制日志时间范围为最近30秒（测试过程极短，完全覆盖）
LOG_SINCE="30s"

echo -e "${BLUE}开始 User Service 负载均衡测试...${NC}"
echo "========================================="

# 配置参数（集中管理，便于修改）
ENROLLMENT_SERVICE_URL="http://localhost:8083/api/enrollments"
STUDENT_ID="2024002"
RETRY_COUNT=3  # 请求失败重试次数
SLEEP_SECONDS=1  # 每次请求间隔
COURSE_IDS=(
    "03e743bd-16c4-48f6-a575-02ed610be3c1"
    "153a2e96-f2db-42c2-945d-bd31859da7e7"
    "3e6a8dbc-50dd-45e4-ba9a-355dd35127e5"
)
TOTAL_REQUESTS=${#COURSE_IDS[@]}  # 总请求数（自动获取）

# 前置检查：确保依赖工具存在
check_dependency() {
    if ! command -v $1 &> /dev/null; then
        echo -e "${RED}错误：未找到 $1 工具，请先安装！${NC}"
        exit 1
    fi
}
check_dependency "curl"
check_dependency "jq"
check_dependency "docker"
# 检查 timeout 命令（用于限制 docker logs 超时）
if ! command -v timeout &> /dev/null; then
    echo -e "${RED}错误：未找到 timeout 工具，请安装（sudo apt install coreutils）！${NC}"
    exit 1
fi

# 前置检查：确保核心服务正在运行（只检查 User Service 相关）
check_service_running() {
    local service_name=$1
    if ! docker inspect -f '{{.State.Running}}' $service_name &> /dev/null; then
        echo -e "${YELLOW}警告：$service_name 未运行，正在尝试启动...${NC}"
        docker start $service_name || {
            echo -e "${RED}错误：启动 $service_name 失败！${NC}"
            exit 1
        }
        sleep 5  # 等待服务启动
    fi
}
check_service_running "nacos"          # 服务发现依赖
check_service_running "user-db"        # User 服务数据库
check_service_running "enrollment-db"  # 选课服务数据库
check_service_running "user-service-1"
check_service_running "user-service-2"
check_service_running "user-service-3"
check_service_running "enrollment-service"

# 清空日志（只保留最近30秒，避免处理历史日志）
echo -e "${YELLOW}清空测试相关日志...${NC}"
for service in user-service-1 user-service-2 user-service-3 enrollment-service; do
    # 用 timeout 限制命令超时（5秒），避免阻塞
    timeout 5s docker logs $service --since $LOG_SINCE > /dev/null 2>&1 || true
done

# 执行选课请求（触发 User Service 调用，增加重试机制）
execute_enroll_request() {
    local course_id=$1
    local retry=0
    while [ $retry -lt $RETRY_COUNT ]; do
        response=$(curl -s -w "\n%{http_code}" -X POST $ENROLLMENT_SERVICE_URL \
            -H "Content-Type: application/json" \
            -d "{\"studentId\": \"$STUDENT_ID\", \"courseId\": \"$course_id\"}")
        http_code=$(echo "$response" | tail -n 1)
        body=$(echo "$response" | head -n -1)

        if [ "$http_code" -eq 200 ] || [ "$http_code" -eq 201 ]; then
            echo -e "${GREEN}  成功 - 选课ID: $(echo "$body" | jq -r '.id')${NC}"
            return 0
        else
            retry=$((retry + 1))
            echo -e "${YELLOW}  失败（HTTP $http_code），正在重试（$retry/$RETRY_COUNT）...${NC}"
            echo "  响应内容：$body"
            sleep $SLEEP_SECONDS
        fi
    done
    echo -e "${RED}  失败：重试 $RETRY_COUNT 次后仍未成功${NC}"
    return 1
}

# 批量执行请求（正常序号显示）
for ((i=0; i<TOTAL_REQUESTS; i++)); do
    course_id=${COURSE_IDS[$i]}
    current_index=$((i + 1))  # 序号从1开始
    echo -e "\n测试 ${current_index}/${TOTAL_REQUESTS} - 课程ID: $course_id"
    execute_enroll_request $course_id
    sleep $SLEEP_SECONDS
done

echo -e "\n${BLUE}=========================================${NC}"
echo -e "${BLUE}User Service 负载均衡测试完成！${NC}"
echo -e "${BLUE}=========================================${NC}"

# User Service 日志摘要（核心优化：超时控制+极简过滤）
echo -e "\n${BLUE}👤 User Service 日志摘要（最近1条请求）：${NC}"
for service in user-service-1 user-service-2 user-service-3; do
    echo -e "\n$service:"
    # 1. timeout 5秒：避免命令阻塞 2. 只过滤学生ID（极简关键词） 3. head -10：只取前10行，减少处理量
    log_line=$(timeout 5s docker logs $service --since $LOG_SINCE 2>&1 | grep -F "$STUDENT_ID" | tail -1)
    if [ -n "$log_line" ]; then
        echo "  $log_line"
    else
        echo -e "  ${YELLOW}无相关请求日志（可能未被负载均衡路由）${NC}"
    fi
done

# 请求统计（极简逻辑，只统计学生ID出现次数）
echo -e "\n${BLUE}📊 User Service 实例请求统计：${NC}"
# 逐个实例统计，超时5秒，过滤关键词简化为学生ID
user1_count=$(timeout 5s docker logs user-service-1 --since $LOG_SINCE 2>&1 | grep -cF "$STUDENT_ID")
user2_count=$(timeout 5s docker logs user-service-2 --since $LOG_SINCE 2>&1 | grep -cF "$STUDENT_ID")
user3_count=$(timeout 5s docker logs user-service-3 --since $LOG_SINCE 2>&1 | grep -cF "$STUDENT_ID")
user_total=$((user1_count + user2_count + user3_count))

echo -e "总请求数：${GREEN}$user_total${NC}"
echo -e "\n详细分布："
echo "user-service-1: $user1_count"
echo "user-service-2: $user2_count"
echo "user-service-3: $user3_count"

# 负载均衡验证结果（3次请求：1/1/1 为最优，差异≤1均正常）
echo -e "\n${BLUE}💡 负载均衡验证结果：${NC}"
user_counts=($user1_count $user2_count $user3_count)
user_max=${user_counts[0]}
user_min=${user_counts[0]}
for count in "${user_counts[@]}"; do
    [ $count -gt $user_max ] && user_max=$count
    [ $count -lt $user_min ] && user_min=$count
done

if [ $((user_max - user_min)) -le 1 ]; then
    echo -e "${GREEN}✅ 负载均衡效果良好！各实例请求分布均匀（差异：$((user_max - user_min)) 次）${NC}"
else
    echo -e "${YELLOW}⚠️  负载分布不均匀（差异：$((user_max - user_min)) 次）${NC}"
    echo -e "  建议检查：1. Nacos 控制台确认3个实例均已注册 2. 负载均衡策略（默认轮询）${NC}"
fi

echo -e "\n${BLUE}📌 测试说明：${NC}"
echo "  - 测试通过 enrollment-service 调用 user-service，模拟真实业务场景"
echo "  - $TOTAL_REQUESTS 次请求默认应均匀分布（各1次），差异≤1为正常"
echo "  - Nacos 控制台：http://localhost:8848/nacos（账号密码均为 nacos）"

