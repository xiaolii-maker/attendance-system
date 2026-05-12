package com.example.attendancesystem.entity;
import jakarta.persistence.*;
@Entity
@Table(name = "student")


public class Student {
    @Id
    @Column(name = "student_id", length = 20)
    private String studentId;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Column(name = "class_name", length = 50)
    private String className;

    @Column(name = "gender", length = 10)
    private String gender;

    @Column(name = "age")
    private Integer age;

    public Student() {
    }

    public Student(String studentId, String name, String className, String gender, Integer age) {
        this.studentId = studentId;
        this.name = name;
        this.className = className;
        this.gender = gender;
        this.age = age;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }
}

