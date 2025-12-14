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
    "code": "CSC101",
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
    "code": "CSC101",
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
    "code": "CSC103",
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
    "code": "CSC104",
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
    "code": "CSC105",
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
    "code": "CSC106",
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
    "code": "CSC107",
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
    "code": "CSC108",
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
    "code": "CSC109",
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
    "code": "CSC110",
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
echo -e "\n=== 测试完成 ==="
success "所有测试步骤执行完毕，请检查以上输出结果"

