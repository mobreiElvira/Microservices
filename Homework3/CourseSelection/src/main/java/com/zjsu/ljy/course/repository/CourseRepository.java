package com.zjsu.ljy.course.repository;

import com.zjsu.ljy.course.model.Course;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class CourseRepository {
    private final Map<String, Course> courses = new ConcurrentHashMap<>();

    public Course save(Course course) {
        courses.put(course.getId(), course);
        return course;
    }

    public Optional<Course> findById(String id) {
        return Optional.ofNullable(courses.get(id));
    }

    public List<Course> findAll() {
        return new ArrayList<>(courses.values());
    }

    public void deleteById(String id) {
        courses.remove(id);
    }

    public boolean existsById(String id) {
        return courses.containsKey(id);
    }
}
