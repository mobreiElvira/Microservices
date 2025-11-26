package com.zjsu.ljy.service;

import com.zjsu.ljy.dto.request.CourseCreateRequest;
import com.zjsu.ljy.dto.request.CourseUpdateRequest;
import com.zjsu.ljy.exception.BusinessException;
import com.zjsu.ljy.exception.InvalidParamException;
import com.zjsu.ljy.exception.ResourceNotFoundException;
import com.zjsu.ljy.model.Course;
import com.zjsu.ljy.model.Instructor;
import com.zjsu.ljy.model.ScheduleSlot;
import com.zjsu.ljy.repository.CourseRepository;
import com.zjsu.ljy.repository.InstructorRepository;
import com.zjsu.ljy.repository.ScheduleRepository;
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
    private final InstructorRepository instructorRepository;
    private final ScheduleRepository scheduleRepository;

    /**
     * 创建课程：同步保存教师和课程安排
     */
    @Transactional
    public Course createCourse(CourseCreateRequest request) {
        // 1. 基础参数校验
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

        // 2. 课程编码唯一性校验
        if (courseRepository.existsByCode(request.getCode())) {
            throw new BusinessException("课程编码已存在：" + request.getCode());
        }

        // 3. 处理教师：不存在则新增，存在则直接使用
        Instructor instructor = instructorRepository.findById(instructorReq.getId())
                .orElseGet(() -> {
                    Instructor newInstructor = new Instructor();
                    BeanUtils.copyProperties(instructorReq, newInstructor);
                    return instructorRepository.save(newInstructor);
                });

        // 4. 处理课程安排：直接新增到schedule_slot表
        ScheduleSlot scheduleSlot = new ScheduleSlot();
        BeanUtils.copyProperties(scheduleReq, scheduleSlot);
        ScheduleSlot savedSchedule = scheduleRepository.save(scheduleSlot);

        // 5. 构建课程实体并保存
        Course course = new Course();
        course.setId(UUID.randomUUID().toString());
        course.setCode(request.getCode());
        course.setTitle(request.getTitle());
        course.setInstructor(instructor);
        course.setSchedule(savedSchedule);
        course.setCapacity(request.getCapacity());
        course.setEnrolled(0);

        return courseRepository.save(course);
    }

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

        // 课程安排更新
        if (request.getSchedule() != null) {
            CourseUpdateRequest.ScheduleUpdateRequest scheduleReq = request.getSchedule();
            ScheduleSlot scheduleSlot;

            if (scheduleReq.getId() == null) {
                scheduleSlot = new ScheduleSlot();
                BeanUtils.copyProperties(scheduleReq, scheduleSlot);
                scheduleSlot = scheduleRepository.save(scheduleSlot);
            } else {
                scheduleSlot = scheduleRepository.findById(scheduleReq.getId())
                        .orElseThrow(() -> new ResourceNotFoundException("课程安排不存在，id：" + scheduleReq.getId()));
                BeanUtils.copyProperties(scheduleReq, scheduleSlot);
                scheduleSlot = scheduleRepository.save(scheduleSlot);
            }

            existingCourse.setSchedule(scheduleSlot);
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

    // ------------------------------ 新增查询方法实现 ------------------------------

    /**
     * 按课程代码查询
     */
    public Course getCourseByCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            throw new InvalidParamException("课程代码不能为空");
        }

        return courseRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("课程不存在，代码：" + code));
    }

    /**
     * 按讲师ID查询课程
     */
    public List<Course> getCoursesByInstructor(String instructorId) {
        if (instructorId == null || instructorId.trim().isEmpty()) {
            throw new InvalidParamException("讲师ID不能为空");
        }

        List<Course> courses = courseRepository.findByInstructorId(instructorId);
        if (courses.isEmpty()) {
            throw new ResourceNotFoundException("该讲师暂无课程，讲师ID：" + instructorId);
        }
        return courses;
    }

    /**
     * 按讲师ID查询有剩余容量的课程
     */
    public List<Course> getCoursesByInstructorWithRemainingCapacity(String instructorId) {
        if (instructorId == null || instructorId.trim().isEmpty()) {
            throw new InvalidParamException("讲师ID不能为空");
        }

        List<Course> courses = courseRepository.findByInstructorIdAndCapacityGreaterThanEnrolled(instructorId);
        if (courses.isEmpty()) {
            throw new ResourceNotFoundException("该讲师暂无有剩余容量的课程，讲师ID：" + instructorId);
        }
        return courses;
    }

    /**
     * 查询所有有剩余容量的课程
     */
    public List<Course> getCoursesWithRemainingCapacity() {
        List<Course> courses = courseRepository.findByCapacityGreaterThanEnrolled();
        if (courses.isEmpty()) {
            throw new ResourceNotFoundException("暂无有剩余容量的课程");
        }
        return courses;
    }

    /**
     * 按标题关键字模糊查询课程
     */
    public List<Course> searchCoursesByTitle(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            throw new InvalidParamException("查询关键字不能为空");
        }

        List<Course> courses = courseRepository.findByTitleContainingIgnoreCase(keyword);
        if (courses.isEmpty()) {
            throw new ResourceNotFoundException("未找到匹配标题的课程，关键字：" + keyword);
        }
        return courses;
    }

    /**
     * 按标题关键字模糊查询有剩余容量的课程
     */
    public List<Course> searchCoursesByTitleWithRemainingCapacity(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            throw new InvalidParamException("查询关键字不能为空");
        }

        List<Course> courses = courseRepository.findByTitleContainingIgnoreCaseAndCapacityGreaterThanEnrolled(keyword);
        if (courses.isEmpty()) {
            throw new ResourceNotFoundException("未找到匹配标题且有剩余容量的课程，关键字：" + keyword);
        }
        return courses;
    }

    /**
     * 统计有剩余容量的课程数量
     */
    public long countCoursesWithRemainingCapacity() {
        return courseRepository.countByCapacityGreaterThanEnrolled();
    }

    /**
     * 统计指定讲师有剩余容量的课程数量
     */
    public long countCoursesByInstructorWithRemainingCapacity(String instructorId) {
        if (instructorId == null || instructorId.trim().isEmpty()) {
            throw new InvalidParamException("讲师ID不能为空");
        }
        return courseRepository.countByInstructorIdAndCapacityGreaterThanEnrolled(instructorId);
    }

}
