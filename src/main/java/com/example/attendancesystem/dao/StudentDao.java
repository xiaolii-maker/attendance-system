package com.example.attendancesystem.dao;

import com.example.attendancesystem.entity.Student;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public class StudentDao {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public void insert(Student student) {
        String sql = "insert into student (student_id, name, class_name, gender, age) values (?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql,
                student.getStudentId(),
                student.getName(),
                student.getClassName(),
                student.getGender(),
                student.getAge());
    }

    public Student findById(String studentId) {
        String sql = "select student_id as studentId, name, class_name as className, gender, age from student where student_id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, new BeanPropertyRowMapper<>(Student.class), studentId);
        } catch (Exception e) {
            return null;
        }
    }

    public List<Student> findAll() {
        String sql = "select student_id as studentId, name, class_name as className, gender, age from student";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Student.class));
    }

    public List<Student> findByClassName(String className) {
        String sql = "select student_id as studentId, name, class_name as className, gender, age from student where class_name = ?";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Student.class), className);
    }
}