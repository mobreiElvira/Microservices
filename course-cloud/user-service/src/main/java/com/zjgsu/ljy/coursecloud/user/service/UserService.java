package com.zjgsu.ljy.coursecloud.user.service;


import com.zjgsu.ljy.coursecloud.user.model.Student;
import com.zjgsu.ljy.coursecloud.user.model.Teacher;
import com.zjgsu.ljy.coursecloud.user.model.User;
import com.zjgsu.ljy.coursecloud.user.repository.StudentRepository;
import com.zjgsu.ljy.coursecloud.user.repository.TeacherRepository;
import com.zjgsu.ljy.coursecloud.user.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UserService {
    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(StudentRepository studentRepository, TeacherRepository teacherRepository, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.studentRepository = studentRepository;
        this.teacherRepository = teacherRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * 根据用户名查找用户
     */
    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElse(null);
    }
    public Student createStudent(Student student) {
        return studentRepository.save(student);
    }

    public Teacher createTeacher(Teacher teacher) {
        return teacherRepository.save(teacher);
    }

    @Transactional(readOnly = true)
    public Optional<Student> getStudentById(String id) {
        return studentRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<Teacher> getTeacherById(String id) {
        return teacherRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Teacher> getAllTeachers() {
        return teacherRepository.findAll();
    }

    public void deleteStudent(String id) {
        studentRepository.deleteById(id);
    }

    public void deleteTeacher(String id) {
        teacherRepository.deleteById(id);
    }

    public Optional<Student> getStudentByStudentId(String studentId) {
        return studentRepository.findByStudentId(studentId);
    }

    public Optional<Teacher> getTeacherByTeacherId(String teacherId) {
        return teacherRepository.findByTeacherId(teacherId);
    }
    public Student updateStudent(Student student) {
        return studentRepository.save(student);
    }

    /**
     * 用户认证
     * @param username 用户名
     * @param password 密码（明文）
     * @return 认证成功返回用户对象，失败返回null
     */
    public User authenticate(String username, String password) {
        Optional<User> userOpt = userRepository.findByUsername(username);

        if (userOpt.isEmpty()) {
            return null;
        }

        User user = userOpt.get();

        // 验证密码
        if (passwordEncoder.matches(password, user.getPassword())) {
            return user;
        }

        return null;
    }
}
