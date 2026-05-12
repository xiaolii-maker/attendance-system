package com.example.attendancesystem.controller;

import com.example.attendancesystem.dto.LoginRequest;
import com.example.attendancesystem.dto.RegisterRequest;
import com.example.attendancesystem.entity.User;
import com.example.attendancesystem.repository.UserRepository;
import com.example.attendancesystem.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    // ========== 任务1：实现登录接口 ==========
    @PostMapping("/login")
    public Result<String> login(@RequestBody LoginRequest request) {
        try {
            // 1. 验证用户名和密码（Spring Security自动验证）
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );

            // 2. 保存认证信息到安全上下文
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // 3. 返回登录成功响应
            return Result.success("登录成功！欢迎回来，" + request.getUsername());
        } catch (Exception e) {
            return Result.error("用户名或密码错误");
        }
    }

    // ========== 任务2：实现注册接口 ==========
    @PostMapping("/register")
    public Result<String> register(@RequestBody RegisterRequest request) {
        // 1. 检查用户名是否已存在
        if (userRepository.existsByUsername(request.getUsername())) {
            return Result.error("用户名 " + request.getUsername() + " 已存在");
        }

        // 2. 密码加密存储（BCrypt）
        String encodedPassword = passwordEncoder.encode(request.getPassword());

        // 3. 创建新用户
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(encodedPassword);
        user.setRealName(request.getRealName());
        user.setRole(request.getRole() != null ? request.getRole() : "STUDENT");
        user.setCreateTime(java.time.LocalDateTime.now());

        userRepository.save(user);

        // 4. 返回注册成功响应
        return Result.success("注册成功！请登录");
    }

    // ========== 页面表单注册（第十周新增）==========

    @PostMapping("/register-page")
    public String registerPageForm(
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam String confirmPassword,
            @RequestParam String realName,
            @RequestParam(defaultValue = "STUDENT") String role,
            Model model) {

        // 1. 检查密码是否一致（后端校验）
        if (!password.equals(confirmPassword)) {
            model.addAttribute("errorMsg", "两次输入的密码不一致");
            return "register";
        }

        // 2. 检查密码长度
        if (password.length() < 6) {
            model.addAttribute("errorMsg", "密码长度不能少于6位");
            return "register";
        }

        // 3. 检查用户名是否已存在
        if (userRepository.existsByUsername(username)) {
            model.addAttribute("errorMsg", "用户名 " + username + " 已存在");
            return "register";
        }

        // 4. 密码加密存储
        String encodedPassword = passwordEncoder.encode(password);

        // 5. 创建新用户
        User user = new User();
        user.setUsername(username);
        user.setPassword(encodedPassword);
        user.setRealName(realName);
        user.setRole(role);
        user.setCreateTime(java.time.LocalDateTime.now());

        userRepository.save(user);

        // 6. 注册成功后跳转到登录页
        return "redirect:/login?success";
    }
}