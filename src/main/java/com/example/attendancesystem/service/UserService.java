package com.example.attendancesystem.service;

import com.example.attendancesystem.entity.User;
import java.util.List;

public interface UserService {

    /**
     * 新增用户
     */
    void addUser(User user);

    /**
     * 根据ID查询用户
     */
    User getUserById(Long id);

    /**
     * 根据用户名查询用户（登录验证）
     */
    User getUserByUsername(String username);

    /**
     * 查询所有教师
     */
    List<User> getAllTeachers();

    /**
     * 查询所有用户
     */
    List<User> getAllUsers();

    /**
     * 更新用户信息
     */
    void updateUser(User user);

    /**
     * 删除用户
     */
    void deleteUser(Long id);
}