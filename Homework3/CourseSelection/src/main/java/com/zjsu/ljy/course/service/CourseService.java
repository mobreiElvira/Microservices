package com.zjsu.ljy.course.service;

import com.zjsu.ljy.course.exception.InvalidParamException;
import com.zjsu.ljy.course.exception.ResourceNotFoundException;
import com.zjsu.ljy.course.model.Course;
import com.zjsu.ljy.course.model.Instructor;
import com.zjsu.ljy.course.model.ScheduleSlot;
import com.zjsu.ljy.course.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CourseService {
    private final CourseRepository courseRepository;

    /**
     * 创建课程
     */
    public Course createCourse(String code, String title, Instructor instructor, ScheduleSlot schedule, Integer capacity) {
        // 验证必填字段
        if (code == null || code.trim().isEmpty()) {
            throw new InvalidParamException("课程编码不能为空");
        }
        if (title == null || title.trim().isEmpty()) {
            throw new InvalidParamException("课程名称不能为空");
        }
        if (instructor == null) {
            throw new InvalidParamException("教师信息不能为空");
        }
        if (schedule == null) {
            throw new InvalidParamException("课程安排不能为空");
        }
        if (capacity == null || capacity <= 0) {
            throw new InvalidParamException("课程容量必须大于0");
        }

        Course course = new Course(code, title, instructor, schedule, capacity);
        return courseRepository.save(course);
    }

    /**
     * 查询所有课程
     */
    public List<Course> getAllCourses() {
        return courseRepository.findAll();
    }

    /**
     * 根据id查询课程
     */
    public Course getCourseById(String id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("课程不存在，id：" + id));
    }

    /**
     * 更新课程信息
     */
    public Course updateCourse(String id, String newCode, String newTitle, Instructor newInstructor, ScheduleSlot newSchedule, Integer newCapacity) {
        Course existingCourse = getCourseById(id);

        // 更新字段（非null则修改）
        if (newCode != null) existingCourse.setCode(newCode);
        if (newTitle != null) existingCourse.setTitle(newTitle);
        if (newInstructor != null) existingCourse.setInstructor(newInstructor);
        if (newSchedule != null) existingCourse.setSchedule(newSchedule);
        if (newCapacity != null && newCapacity > 0) existingCourse.setCapacity(newCapacity);

        return courseRepository.save(existingCourse);
    }

    /**
     * 删除课程
     */
    public void deleteCourse(String id) {
        if (!courseRepository.existsById(id)) {
            throw new ResourceNotFoundException("课程不存在，id：" + id);
        }
        courseRepository.deleteById(id);
    }

    /**
     * 选课成功后，更新课程的当前选课人数（enrolled字段）
     */
    public void incrementEnrolledCount(String courseId) {
        Course course = getCourseById(courseId);
        int newEnrolled = course.getEnrolled() + 1;
        // 再次验证容量（避免并发问题）
        if (newEnrolled > course.getCapacity()) {
            throw new InvalidParamException("课程容量不足，当前：" + course.getEnrolled() + "，容量：" + course.getCapacity());
        }
        course.setEnrolled(newEnrolled);
        courseRepository.save(course);
    }
}
