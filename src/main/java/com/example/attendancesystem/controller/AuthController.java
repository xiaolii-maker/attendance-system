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

    // API 登录接口
    @PostMapping("/login")
    public Result<String> login(@RequestBody LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
            return Result.success("登录成功！欢迎回来，" + request.getUsername());
        } catch (Exception e) {
            return Result.error("用户名或密码错误");
        }
    }

    // API 注册接口
    @PostMapping("/register")
    public Result<String> register(@RequestBody RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            return Result.error("用户名 " + request.getUsername() + " 已存在");
        }
        String encodedPassword = passwordEncoder.encode(request.getPassword());
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(encodedPassword);
        user.setRealName(request.getRealName());
        user.setRole(request.getRole() != null ? request.getRole() : "STUDENT");
        user.setCreateTime(java.time.LocalDateTime.now());
        userRepository.save(user);
        return Result.success("注册成功！请登录");
    }
}