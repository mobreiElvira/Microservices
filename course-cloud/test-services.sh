#!/bin/bash
set -eo pipefail

# 颜色定义（增强可读性）
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # 重置颜色

# 服务地址配置（使用宿主机端口，适配Docker映射）
USER_SERVICE="http://localhost:8081"
CATALOG_SERVICE="http://localhost:8082"
ENROLLMENT_SERVICE="http://localhost:8083"

# 超时配置（避免curl无限等待）
CURL_OPTS="-s --connect-timeout 5 --max-time 10"

# 日志输出函数
info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

# 前置检查：确保jq已安装
if ! command -v jq &> /dev/null; then
    error "未找到jq工具，请先安装：sudo apt install jq"
    exit 1
fi

# 前置检查：测试服务连通性（适配当前接口）
check_service() {
    local service_name=$1
    local service_url=$2
    local health_path=$3
    info "检查${service_name}服务连通性..."
    local status=$(curl ${CURL_OPTS} -o /dev/null -w "%{http_code}" ${service_url}${health_path} 2>/dev/null || echo "000")
    if [ "$status" = "000" ]; then
        error "${service_name}服务无法连接：${service_url}"
        error "请确认容器端口映射和服务是否正常启动"
        exit 1
    elif [ "$status" -ge 400 ]; then
        warning "${service_name}服务已连接，但返回状态码：${status}"
    else
        success "${service_name}服务连通正常"
    fi
}

# 主测试流程
echo "=== 测试微服务通过 Nacos 的服务发现 ==="
echo ""

# 前置服务检查
info "开始服务连通性检查..."
check_service "用户服务" "${USER_SERVICE}" "/api/students"
check_service "课程目录服务" "${CATALOG_SERVICE}" "/api/courses/test"
check_service "选课服务" "${ENROLLMENT_SERVICE}" "/api/enrollments"
echo ""

# 1. 创建学生
echo -e "\n=== 1. 创建学生 ==="
info "调用用户服务创建学生..."
STUDENT_RESPONSE=$(curl ${CURL_OPTS} -X POST ${USER_SERVICE}/api/students \
  -H "Content-Type: application/json" \
  -d '{
    "username": "lisi",
    "email": "lisi@example.edu.cn",
    "studentId": "2024002",
    "name": "李四",
    "major": "软件工程",
    "grade": 2024
  }')

# 处理响应输出
if [ -z "$STUDENT_RESPONSE" ]; then
    error "创建学生请求无响应"
else
    echo $STUDENT_RESPONSE | jq '.'
    if echo $STUDENT_RESPONSE | jq -e '.success // .status == "SUCCESS"' &> /dev/null; then
        success "学生创建请求执行完成"
    fi
fi

# 2. 获取所有学生
echo -e "\n=== 2. 获取所有学生 ==="
info "查询所有学生信息..."
STUDENTS_LIST=$(curl ${CURL_OPTS} ${USER_SERVICE}/api/students)
if [ -z "$STUDENTS_LIST" ]; then
    error "查询学生列表无响应"
else
    echo $STUDENTS_LIST | jq '.'
fi

# 3. 创建课程（核心修复：完全匹配CourseRequest校验规则）
echo -e "\n=== 3. 创建课程 ==="
info "调用课程服务创建课程（严格匹配参数校验规则）..."
# 关键修复点：
# 1. code符合正则：[A-Z]{3}\\d{3} → 示例CS102改为CSC102（3个大写字母+3个数字）
# 2. dayOfWeek使用枚举值：TUESDAY（全大写，匹配DayOfWeekValue枚举）
# 3. start/end为ISO格式：HH:mm:ss（LocalTime.parse要求）
# 4. capacity/expectedAttendance在10-500之间（符合@Min/@Max校验）
COURSE_RESPONSE=$(curl ${CURL_OPTS} -X POST ${CATALOG_SERVICE}/api/courses \
  -H "Content-Type: application/json" \
  -d '{
    "code": "CSC102",
    "title": "数据结构",
    "instructorId": "T002",
    "instructorName": "李教授",
    "instructorEmail": "li@example.edu.cn",
    "dayOfWeek": "TUESDAY",
    "start": "10:00:00",
    "end": "12:00:00",
    "capacity": 50,
    "expectedAttendance": 45
  }')

if [ -z "$COURSE_RESPONSE" ]; then
    error "创建课程请求无响应"
else
    echo $COURSE_RESPONSE | jq '.'
    # 提取课程ID（匹配CourseResponse的返回格式）
    COURSE_ID=$(echo $COURSE_RESPONSE | jq -r '.data.id // "unknown"')
    echo "课程ID: $COURSE_ID"
    if [ "$COURSE_ID" != "unknown" ] && [ "$COURSE_ID" != "null" ]; then
        success "课程ID提取成功：$COURSE_ID"
    else
        warning "未能提取到有效课程ID（可能创建失败）"
    fi
fi

# 4. 获取所有课程
echo -e "\n=== 4. 获取所有课程 ==="
info "查询所有课程信息..."
COURSES_LIST=$(curl ${CURL_OPTS} ${CATALOG_SERVICE}/api/courses)
if [ -z "$COURSES_LIST" ]; then
    error "查询课程列表无响应"
else
    echo $COURSES_LIST | jq '.'
fi

# 5. 测试选课（验证服务间通信）
echo -e "\n=== 5. 测试学生选课（验证服务间调用）==="
info "调用选课服务，测试服务间通信..."
if [ "$COURSE_ID" != "unknown" ] && [ "$COURSE_ID" != "null" ]; then
    ENROLLMENT_RESPONSE=$(curl ${CURL_OPTS} -X POST ${ENROLLMENT_SERVICE}/api/enrollments \
      -H "Content-Type: application/json" \
      -d "{
        \"courseId\": \"$COURSE_ID\",
        \"studentId\": \"2024002\"
      }")
    
    if [ -z "$ENROLLMENT_RESPONSE" ]; then
        error "选课请求无响应"
    else
        echo $ENROLLMENT_RESPONSE | jq '.'
    fi
else
    warning "课程ID无效，跳过选课测试（请先解决创建课程400错误）"
fi

# 6. 测试用已存在的学生选课
echo -e "\n=== 6. 使用已存在的学生(2024001)选课 ==="
info "使用学生2024001测试选课..."
if [ "$COURSE_ID" != "unknown" ] && [ "$COURSE_ID" != "null" ]; then
    ENROLLMENT_RESPONSE2=$(curl ${CURL_OPTS} -X POST ${ENROLLMENT_SERVICE}/api/enrollments \
      -H "Content-Type: application/json" \
      -d "{
        \"courseId\": \"$COURSE_ID\",
        \"studentId\": \"2024001\"
      }")
    
    if [ -z "$ENROLLMENT_RESPONSE2" ]; then
        error "选课请求无响应"
    else
        echo $ENROLLMENT_RESPONSE2 | jq '.'
    fi
else
    warning "课程ID无效，跳过该选课测试（请先解决创建课程400错误）"
fi

# 7. 查询选课记录
echo -e "\n=== 7. 查询所有选课记录 ==="
info "查询所有选课记录..."
ENROLLMENTS_LIST=$(curl ${CURL_OPTS} ${ENROLLMENT_SERVICE}/api/enrollments)
if [ -z "$ENROLLMENTS_LIST" ]; then
    error "查询选课记录无响应"
else
    echo $ENROLLMENTS_LIST | jq '.'
fi

# 8. 测试选课失败（学生不存在）
echo -e "\n=== 8. 测试选课失败（学生不存在）==="
info "测试不存在的学生选课（预期失败）..."
if [ "$COURSE_ID" != "unknown" ] && [ "$COURSE_ID" != "null" ]; then
    ERROR_RESPONSE=$(curl ${CURL_OPTS} -X POST ${ENROLLMENT_SERVICE}/api/enrollments \
      -H "Content-Type: application/json" \
      -d "{
        \"courseId\": \"$COURSE_ID\",
        \"studentId\": \"9999999\"
      }")
    
    if [ -z "$ERROR_RESPONSE" ]; then
        error "异常测试请求无响应"
    else
        echo $ERROR_RESPONSE | jq '.'
    fi
else
    warning "课程ID无效，跳过失联学生选课测试（请先解决创建课程400错误）"
fi

# 9. 测试选课失败（课程不存在）
echo -e "\n=== 9. 测试选课失败（课程不存在）==="
info "测试不存在的课程选课（预期失败）..."
ERROR_RESPONSE2=$(curl ${CURL_OPTS} -X POST ${ENROLLMENT_SERVICE}/api/enrollments \
  -H "Content-Type: application/json" \
  -d '{
    "courseId": "non-existent-course-id",
    "studentId": "2024001"
  }')

if [ -z "$ERROR_RESPONSE2" ]; then
    error "异常测试请求无响应"
else
    echo $ERROR_RESPONSE2 | jq '.'
fi

# 10. 测试重复选课
echo -e "\n=== 10. 测试重复选课（应该失败）==="
info "测试重复选课（预期失败）..."
if [ "$COURSE_ID" != "unknown" ] && [ "$COURSE_ID" != "null" ]; then
    DUPLICATE_RESPONSE=$(curl ${CURL_OPTS} -X POST ${ENROLLMENT_SERVICE}/api/enrollments \
      -H "Content-Type: application/json" \
      -d "{
        \"courseId\": \"$COURSE_ID\",
        \"studentId\": \"2024001\"
      }")
    
    if [ -z "$DUPLICATE_RESPONSE" ]; then
        error "重复选课测试无响应"
    else
        echo $DUPLICATE_RESPONSE | jq '.'
    fi
else
    warning "课程ID无效，跳过重复选课测试（请先解决创建课程400错误）"
fi

# 11. 查询特定学生的选课
echo -e "\n=== 11. 查询学生 2024001 的选课记录 ==="
info "查询学生2024001的选课记录..."
STUDENT_ENROLLMENTS=$(curl ${CURL_OPTS} ${ENROLLMENT_SERVICE}/api/enrollments/student/2024001)
if [ -z "$STUDENT_ENROLLMENTS" ]; then
    error "查询特定学生选课记录无响应"
else
    echo $STUDENT_ENROLLMENTS | jq '.'
fi

# 12. 查询特定课程的选课
echo -e "\n=== 12. 查询课程 $COURSE_ID 的选课记录 ==="
info "查询课程${COURSE_ID}的选课记录..."
if [ "$COURSE_ID" != "unknown" ] && [ "$COURSE_ID" != "null" ]; then
    COURSE_ENROLLMENTS=$(curl ${CURL_OPTS} "${ENROLLMENT_SERVICE}/api/enrollments/course/$COURSE_ID")
    if [ -z "$COURSE_ENROLLMENTS" ]; then
        error "查询特定课程选课记录无响应"
    else
        echo $COURSE_ENROLLMENTS | jq '.'
    fi
else
    warning "课程ID无效，跳过该查询（请先解决创建课程400错误）"
fi

echo -e "\n=== 测试完成 ==="
success "所有测试步骤执行完毕，请检查以上输出结果"

