package com.example.attendancesystem.controller;

import com.example.attendancesystem.entity.User;
import com.example.attendancesystem.service.UserService;
import com.example.attendancesystem.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 新增用户
     */
    @PostMapping("/add")
    public Result<String> addUser(@RequestBody User user) {
        try {
            userService.addUser(user);
            return Result.success("用户添加成功：" + user.getRealName());
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 根据ID查询用户
     */
    @GetMapping("/{id}")
    public Result<User> getUserById(@PathVariable Long id) {
        try {
            User user = userService.getUserById(id);
            return Result.success(user);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 根据用户名查询用户
     */
    @GetMapping("/username/{username}")
    public Result<User> getUserByUsername(@PathVariable String username) {
        User user = userService.getUserByUsername(username);
        if (user != null) {
            return Result.success(user);
        } else {
            return Result.error("未找到用户：" + username);
        }
    }

    /**
     * 查询所有教师
     */
    @GetMapping("/teachers")
    public Result<List<User>> getAllTeachers() {
        List<User> teachers = userService.getAllTeachers();
        return Result.success(teachers);
    }

    /**
     * 查询所有用户
     */
    @GetMapping("/all")
    public Result<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return Result.success(users);
    }

    /**
     * 更新用户
     */
    @PutMapping("/update")
    public Result<String> updateUser(@RequestBody User user) {
        try {
            userService.updateUser(user);
            return Result.success("用户更新成功");
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/{id}")
    public Result<String> deleteUser(@PathVariable Long id) {
        try {
            userService.deleteUser(id);
            return Result.success("用户删除成功");
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }
}