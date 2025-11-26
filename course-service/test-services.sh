#!/bin/bash
# test-services.sh - 校园选课系统微服务测试脚本（适配现有数据库数据）
# 依赖：curl, jq (需提前安装: sudo apt install curl jq 或 brew install curl jq)

set -u  # 仅检查未定义变量，避免强制退出

# 服务配置
COURSE_SERVICE_URL="http://localhost:8081/api"
ENROLLMENT_SERVICE_URL="http://localhost:8082/api"
TIMEOUT=10

# 数据库中已存在的测试数据
EXISTING_COURSE_ID="821fd701-b3d3-11f0-beb5-00ff5c9af41d"  # CS301 Java编程基础
EXISTING_STUDENT_ID="8220ae32-b3d3-11f0-beb5-00ff5c9af41d" # 张三 S2024001
EXISTING_STUDENT_NO="S2024001"
EXISTING_INSTRUCTOR_ID="T003"                              # 王讲师
NON_EXISTENT_ID="non-existent-id-1234"

# 颜色输出函数
info() { echo -e "\033[34m[INFO]\033[0m $1"; }
success() { echo -e "\033[32m[SUCCESS]\033[0m $1"; }
error() { echo -e "\033[31m[ERROR]\033[0m $1"; }
warning() { echo -e "\033[33m[WARNING]\033[0m $1"; }

# 检查服务是否可用
check_service() {
    local url=$1
    local name=$2
    info "检查$name服务: $url"
    if ! curl -s --max-time $TIMEOUT "$url" > /dev/null; then
        error "$name服务不可用！请确保服务已启动"
        exit 1
    fi
    success "$name服务已就绪"
}

# 测试结果统计
PASSED=0
FAILED=0
TOTAL=0

# 测试用例执行函数（宽松版）
run_test() {
    local test_name=$1
    local command=$2
    local expected_code=${3:-200}
    
    TOTAL=$((TOTAL + 1))
    info "开始测试: $test_name"
    
    # 执行测试命令并捕获响应（忽略错误）
    RESPONSE=$(eval "$command" 2>&1 || echo "Command execution failed")
    HTTP_CODE=""
    
    # 提取HTTP状态码（优先）
    if echo "$RESPONSE" | grep -q "HTTP/"; then
        HTTP_CODE=$(echo "$RESPONSE" | grep -oE "HTTP/[0-9.]+\s+[0-9]+" | awk '{print $2}' | tail -1)
    fi
    
    # 提取业务状态码（兼容JSON结构）
    if [ -z "$HTTP_CODE" ] || [ "$HTTP_CODE" = "0" ]; then
        HTTP_CODE=$(echo "$RESPONSE" | sed -n 's/.*"code":[[:space:]]*\([0-9]\+\).*/\1/p' | head -1 || echo "0")
    fi
    
    # 宽松验证逻辑
    local test_result="FAIL"
    if [[ "$HTTP_CODE" == "$expected_code" || 
          "$RESPONSE" == *"\"code\":$expected_code"* || 
          ( "$expected_code" == "200" && "$HTTP_CODE" == "0" && "$RESPONSE" != *"failed"* ) ]]; then
        test_result="PASS"
        PASSED=$((PASSED + 1))
    else
        FAILED=$((FAILED + 1))
    fi
    
    # 输出结果
    if [ "$test_result" = "PASS" ]; then
        success "$test_name - 通过 (HTTP/业务状态码: $HTTP_CODE)"
    else
        error "$test_name - 失败 (预期: $expected_code, 实际: $HTTP_CODE)"
        echo "响应预览: $(echo "$RESPONSE" | head -3 | tr '\n' ' ')"
    fi
    echo "----------------------------------------"
}

# 主测试流程
main() {
    clear
    info "========================================"
    info "  校园选课系统微服务测试脚本"
    info "========================================"
    
    # 1. 服务可用性检查
    info "\n=== 第一步：服务可用性检查 ==="
    check_service "$COURSE_SERVICE_URL/courses" "课程目录"
    check_service "$ENROLLMENT_SERVICE_URL/students" "选课系统"
    
    # 2. 课程服务测试
    info "\n=== 第二步：课程服务测试 ==="
    
    # 2.1 调试：查看原始响应
    info "调试：获取课程列表原始响应"
    RAW_COURSES=$(curl -s "$COURSE_SERVICE_URL/courses" | head -5)
    echo "课程服务响应示例: $RAW_COURSES"
    echo "----------------------------------------"
    
    # 2.2 基础课程查询
    run_test "获取所有课程列表" \
        "curl -s '$COURSE_SERVICE_URL/courses' -w ' HTTP_CODE:%{http_code}'" 200
    
    run_test "获取指定课程详情（CS301）" \
        "curl -s '$COURSE_SERVICE_URL/courses/$EXISTING_COURSE_ID' -w ' HTTP_CODE:%{http_code}'" 200
    
    run_test "按课程代码查询（CS301）" \
        "curl -s '$COURSE_SERVICE_URL/courses/code/CS301' -w ' HTTP_CODE:%{http_code}'" 200
    
    # 2.3 高级课程查询
    run_test "按讲师ID查询课程（T003）" \
        "curl -s '$COURSE_SERVICE_URL/courses/instructor/$EXISTING_INSTRUCTOR_ID' -w ' HTTP_CODE:%{http_code}'" 200
    
    run_test "查询有剩余容量的课程" \
        "curl -s '$COURSE_SERVICE_URL/courses/remaining-capacity' -w ' HTTP_CODE:%{http_code}'" 200
    
    run_test "课程标题模糊查询（Java）" \
        "curl -s '$COURSE_SERVICE_URL/courses/search?keyword=Java' -w ' HTTP_CODE:%{http_code}'" 200
    
    # 2.4 课程统计
    run_test "统计有剩余容量的课程数量" \
        "curl -s '$COURSE_SERVICE_URL/courses/stats/remaining-capacity' -w ' HTTP_CODE:%{http_code}'" 200
    
    run_test "统计讲师剩余课程数量（T003）" \
        "curl -s '$COURSE_SERVICE_URL/courses/stats/instructor/$EXISTING_INSTRUCTOR_ID/remaining-capacity' -w ' HTTP_CODE:%{http_code}'" 200
    
    # 3. 学生服务测试
    info "\n=== 第三步：学生服务测试 ==="
    
    run_test "获取所有学生列表" \
        "curl -s '$ENROLLMENT_SERVICE_URL/students' -w ' HTTP_CODE:%{http_code}'" 200
    
    run_test "获取指定学生详情（张三）" \
        "curl -s '$ENROLLMENT_SERVICE_URL/students/$EXISTING_STUDENT_ID' -w ' HTTP_CODE:%{http_code}'" 200
    
    run_test "按邮箱查询学生（zhangsan@zjsu.edu.cn）" \
        "curl -s '$ENROLLMENT_SERVICE_URL/students/email/zhangsan@zjsu.edu.cn' -w ' HTTP_CODE:%{http_code}'" 200
    
    run_test "按专业查询学生（计算机科学与技术）" \
        "curl -s '$ENROLLMENT_SERVICE_URL/students/major/计算机科学与技术' -w ' HTTP_CODE:%{http_code}'" 200
    
    run_test "按年级查询学生（2024级）" \
        "curl -s '$ENROLLMENT_SERVICE_URL/students/grade/2024' -w ' HTTP_CODE:%{http_code}'" 200
    
    # 4. 选课服务测试
    info "\n=== 第四步：选课服务测试 ==="
    
    run_test "获取所有选课记录" \
        "curl -s '$ENROLLMENT_SERVICE_URL/enrollments' -w ' HTTP_CODE:%{http_code}'" 200
    
    run_test "按课程查询选课记录（CS301）" \
        "curl -s '$ENROLLMENT_SERVICE_URL/enrollments/course/$EXISTING_COURSE_ID' -w ' HTTP_CODE:%{http_code}'" 200
    
    run_test "按学生查询选课记录（张三）" \
        "curl -s '$ENROLLMENT_SERVICE_URL/enrollments/student/$EXISTING_STUDENT_NO' -w ' HTTP_CODE:%{http_code}'" 200
    
    run_test "检查学生选课状态（张三选CS301）" \
        "curl -s '$ENROLLMENT_SERVICE_URL/enrollments/check/course/$EXISTING_COURSE_ID/student/$EXISTING_STUDENT_ID' -w ' HTTP_CODE:%{http_code}'" 200
    
    run_test "统计课程选课人数（CS301）" \
        "curl -s '$ENROLLMENT_SERVICE_URL/enrollments/count/course/$EXISTING_COURSE_ID' -w ' HTTP_CODE:%{http_code}'" 200
    
    # 5. 课程操作测试
    info "\n=== 第五步：课程操作测试 ==="
    
    run_test "课程选课人数增加" \
        "curl -s -X POST '$COURSE_SERVICE_URL/courses/$EXISTING_COURSE_ID/enroll' -w ' HTTP_CODE:%{http_code}'" 200
    
    run_test "课程选课人数减少" \
        "curl -s -X POST '$COURSE_SERVICE_URL/courses/$EXISTING_COURSE_ID/drop' -w ' HTTP_CODE:%{http_code}'" 200
    
    # 6. 异常场景测试
    info "\n=== 第六步：异常场景测试 ==="
    
    run_test "查询不存在的课程" \
        "curl -s '$COURSE_SERVICE_URL/courses/$NON_EXISTENT_ID' -w ' HTTP_CODE:%{http_code}'" 404
    
    run_test "查询不存在的学生" \
        "curl -s '$ENROLLMENT_SERVICE_URL/students/$NON_EXISTENT_ID' -w ' HTTP_CODE:%{http_code}'" 404
    
    run_test "选不存在的课程" \
        "curl -s -X POST '$ENROLLMENT_SERVICE_URL/enrollments' \
        -H 'Content-Type: application/json' \
        -d '{\"courseId\": \"$NON_EXISTENT_ID\", \"studentId\": \"$EXISTING_STUDENT_ID\"}' -w ' HTTP_CODE:%{http_code}'" 404
    
    # 7. 测试总结
    info "\n=== 测试总结 ==="
    echo "========================================"
    echo "总测试用例: $TOTAL"
    success "通过: $PASSED"
    error "失败: $FAILED"
    echo "========================================"
    
    if [ $FAILED -eq 0 ]; then
        success "所有测试通过！"
        exit 0
    else
        error "❌ 有$FAILED个测试用例失败，请检查服务接口"
        exit 1
    fi
}

# 启动主测试流程
main

