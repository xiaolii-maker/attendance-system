package com.example.attendancesystem.entity;

public class Student {
        private String studentId;      // 学号
        private String name;           // 姓名
        private String className;      // 班级
        private String gender;         // 性别
        private Integer age;           // 年龄

        // 无参构造函数
        public Student() {
        }

        // 有参构造函数
        public Student(String studentId, String name, String className, String gender, Integer age) {
            this.studentId = studentId;
            this.name = name;
            this.className = className;
            this.gender = gender;
            this.age = age;
        }

        // Getter 和 Setter 方法
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
