package com.zjsu.ljy.course.service;

import com.zjsu.ljy.course.dto.request.CourseCreateRequest;
import com.zjsu.ljy.course.dto.request.CourseUpdateRequest;
import com.zjsu.ljy.course.exception.BusinessException;
import com.zjsu.ljy.course.exception.InvalidParamException;
import com.zjsu.ljy.course.exception.ResourceNotFoundException;
import com.zjsu.ljy.course.model.Course;
import com.zjsu.ljy.course.model.Instructor;
import com.zjsu.ljy.course.model.ScheduleSlot;
import com.zjsu.ljy.course.repository.CourseRepository;
import com.zjsu.ljy.course.repository.InstructorRepository; // 注入教师Repository
import com.zjsu.ljy.course.repository.ScheduleRepository; // 注入课程安排Repository
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CourseService {
    private final CourseRepository courseRepository;
    private final InstructorRepository instructorRepository; // 新增：教师Repository
    private final ScheduleRepository scheduleRepository;     // 新增：课程安排Repository

    /**
     * 创建课程：同步保存教师和课程安排
     */
    @Transactional
    public Course createCourse(CourseCreateRequest request) {
        // 1. 基础参数校验（仅校验非空，不校验关联数据是否存在）
        CourseCreateRequest.InstructorRequest instructorReq = request.getInstructor();
        CourseCreateRequest.ScheduleRequest scheduleReq = request.getSchedule();

        // 校验教师核心字段
        if (instructorReq == null || instructorReq.getId() == null || instructorReq.getId().trim().isEmpty()) {
            throw new InvalidParamException("教师ID不能为空");
        }
        if (instructorReq.getName() == null || instructorReq.getName().trim().isEmpty()) {
            throw new InvalidParamException("教师姓名不能为空");
        }
        // 校验课程安排核心字段
        if (scheduleReq == null) {
            throw new InvalidParamException("课程安排不能为空");
        }
        if (scheduleReq.getDayOfWeek() == null || scheduleReq.getDayOfWeek().trim().isEmpty()) {
            throw new InvalidParamException("周几不能为空");
        }
        // 校验课程容量
        if (request.getCapacity() == null || request.getCapacity() <= 0) {
            throw new InvalidParamException("课程容量必须大于0");
        }

        // 2. 课程编码唯一性校验（不变）
        if (courseRepository.existsByCode(request.getCode())) {
            throw new BusinessException("课程编码已存在：" + request.getCode());
        }

        // 3. 处理教师：不存在则新增，存在则直接使用
        Instructor instructor = instructorRepository.findById(instructorReq.getId())
                .orElseGet(() -> {
                    // 教师不存在，创建新教师并保存到instructor表
                    Instructor newInstructor = new Instructor();
                    BeanUtils.copyProperties(instructorReq, newInstructor); // 拷贝ID、姓名、邮箱
                    return instructorRepository.save(newInstructor);
                });

        // 4. 处理课程安排：直接新增到schedule_slot表（自动生成ID）
        ScheduleSlot scheduleSlot = new ScheduleSlot();
        BeanUtils.copyProperties(scheduleReq, scheduleSlot); // 拷贝周几、时间、预期人数
        ScheduleSlot savedSchedule = scheduleRepository.save(scheduleSlot); // 保存后生成ID

        // 5. 构建课程实体并保存（关联教师和课程安排）
        Course course = new Course();
        course.setId(UUID.randomUUID().toString()); // 生成课程UUID
        course.setCode(request.getCode());
        course.setTitle(request.getTitle());
        course.setInstructor(instructor); // 关联新增/查询到的教师
        course.setSchedule(savedSchedule); // 关联新增的课程安排（含自动ID）
        course.setCapacity(request.getCapacity());
        course.setEnrolled(0); // 初始选课人数为0

        return courseRepository.save(course);
    }

    // ------------------------------ 以下方法逻辑不变 ------------------------------
    @Transactional
    public Course updateCourse(String courseId, CourseUpdateRequest request) {
        Course existingCourse = getCourseById(courseId);

        if (request.getCode() != null && !request.getCode().equals(existingCourse.getCode())) {
            if (courseRepository.existsByCode(request.getCode())) {
                throw new BusinessException("新课程编码已存在：" + request.getCode());
            }
            existingCourse.setCode(request.getCode());
        }

        if (request.getTitle() != null) {
            existingCourse.setTitle(request.getTitle());
        }

        if (request.getInstructor() != null) {
            Instructor instructor = new Instructor();
            BeanUtils.copyProperties(request.getInstructor(), instructor);
            existingCourse.setInstructor(instructor);
        }

        // 5. 课程安排更新（创建新排课或更新已有）
        if (request.getSchedule() != null) {
            CourseUpdateRequest.ScheduleUpdateRequest scheduleReq = request.getSchedule();
            ScheduleSlot scheduleSlot;

            if (scheduleReq.getId() == null) {
                // 无 id：创建新排课（保存为持久化对象）
                scheduleSlot = new ScheduleSlot();
                BeanUtils.copyProperties(scheduleReq, scheduleSlot);
                scheduleSlot = scheduleRepository.save(scheduleSlot); // 保存后成为持久化对象
            } else {
                // 有 id：更新已有排课
                scheduleSlot = scheduleRepository.findById(scheduleReq.getId())
                        .orElseThrow(() -> new ResourceNotFoundException("课程安排不存在，id：" + scheduleReq.getId()));
                BeanUtils.copyProperties(scheduleReq, scheduleSlot); // 覆盖字段
                scheduleSlot = scheduleRepository.save(scheduleSlot);
            }

            existingCourse.setSchedule(scheduleSlot); // 关联持久化对象
        }

        if (request.getCapacity() != null && request.getCapacity() > 0) {
            if (request.getCapacity() < existingCourse.getEnrolled()) {
                throw new BusinessException("新容量不能小于当前选课人数（当前：" + existingCourse.getEnrolled() + "）");
            }
            existingCourse.setCapacity(request.getCapacity());
        }

        return courseRepository.save(existingCourse);
    }

    public List<Course> getAllCourses() {
        return courseRepository.findAll();
    }

    public Course getCourseById(String id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("课程不存在，id：" + id));
    }

    @Transactional
    public void deleteCourse(String id) {
        Course course = getCourseById(id);
        if (course.getEnrolled() > 0) {
            throw new BusinessException("无法删除：该课程已有" + course.getEnrolled() + "名学生选课");
        }
        courseRepository.deleteById(id);
    }

    @Transactional
    public void incrementEnrolledCount(String courseId) {
        Course course = getCourseById(courseId);
        int newEnrolled = course.getEnrolled() + 1;
        if (newEnrolled > course.getCapacity()) {
            throw new BusinessException("课程容量不足，当前：" + course.getEnrolled() + "，容量：" + course.getCapacity());
        }
        course.setEnrolled(newEnrolled);
        courseRepository.save(course);
    }

    @Transactional
    public void decrementEnrolledCount(String courseId) {
        Course course = getCourseById(courseId);
        int newEnrolled = course.getEnrolled() - 1;
        if (newEnrolled < 0) {
            throw new InvalidParamException("课程选课人数不能为负数");
        }
        course.setEnrolled(newEnrolled);
        courseRepository.save(course);
    }
}
