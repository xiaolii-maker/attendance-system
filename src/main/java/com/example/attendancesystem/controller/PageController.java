package com.example.attendancesystem.controller;

import com.example.attendancesystem.entity.Student;
import com.example.attendancesystem.entity.User;
import com.example.attendancesystem.repository.StudentRepository;
import com.example.attendancesystem.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Controller
@RequestMapping("/page")
public class PageController {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * 获取当前登录用户
     */
    private User getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            String username = ((UserDetails) principal).getUsername();
            return userRepository.findByUsername(username).orElse(null);
        }
        return null;
    }

    // ========== 登录注册页面 ==========

    @GetMapping("/login")
    public String loginPage(@RequestParam(required = false) String error, Model model) {
        if (error != null) {
            model.addAttribute("errorMsg", "用户名或密码错误");
        }
        return "login";
    }

    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("title", "班级考勤管理系统");

        // 获取当前登录用户信息
        User currentUser = getCurrentUser();
        if (currentUser != null) {
            model.addAttribute("username", currentUser.getUsername());
            model.addAttribute("realName", currentUser.getRealName());
            model.addAttribute("role", currentUser.getRole());
        }

        return "dashboard";
    }

    // ========== 页面表单注册 ==========

    @PostMapping("/register")
    public String registerPageForm(
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam String confirmPassword,
            @RequestParam String realName,
            @RequestParam(defaultValue = "STUDENT") String role,
            Model model) {

        if (!password.equals(confirmPassword)) {
            model.addAttribute("errorMsg", "两次输入的密码不一致");
            return "register";
        }

        if (password.length() < 6) {
            model.addAttribute("errorMsg", "密码长度不能少于6位");
            return "register";
        }

        if (userRepository.existsByUsername(username)) {
            model.addAttribute("errorMsg", "用户名 " + username + " 已存在");
            return "register";
        }

        String encodedPassword = passwordEncoder.encode(password);

        User user = new User();
        user.setUsername(username);
        user.setPassword(encodedPassword);
        user.setRealName(realName);
        user.setRole(role);
        user.setCreateTime(java.time.LocalDateTime.now());

        userRepository.save(user);

        return "redirect:/page/login?success";
    }

    // ========== 学生管理页面 ==========

    @GetMapping("/student/list")
    public String studentList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {

        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Student> studentPage = studentRepository.findAll(pageable);

        model.addAttribute("students", studentPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", studentPage.getTotalPages());
        model.addAttribute("size", size);

        return "student-list";
    }

    @GetMapping("/student/add")
    public String addStudent(Model model) {
        model.addAttribute("student", new Student());
        return "student-form";
    }

    @GetMapping("/student/edit/{id}")
    public String editStudent(@PathVariable String id, Model model) {
        Optional<Student> student = studentRepository.findById(id);
        model.addAttribute("student", student.orElse(new Student()));
        return "student-form";
    }

    @PostMapping("/student/save")
    public String saveStudent(@ModelAttribute Student student) {
        studentRepository.save(student);
        return "redirect:/page/student/list";
    }

    @GetMapping("/student/delete/{id}")
    public String deleteStudent(@PathVariable String id) {
        studentRepository.deleteById(id);
        return "redirect:/page/student/list";
    }


}