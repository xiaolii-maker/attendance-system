package com.example.attendancesystem.service.impl;

import com.example.attendancesystem.dao.UserDao;
import com.example.attendancesystem.entity.User;
import com.example.attendancesystem.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserDao userDao;

    @Override
    public void addUser(User user) {
        // 业务校验：用户名不能为空
        if (user.getUsername() == null || user.getUsername().isEmpty()) {
            throw new RuntimeException("用户名不能为空");
        }
        // 业务校验：密码不能为空
        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            throw new RuntimeException("密码不能为空");
        }
        // 业务校验：真实姓名不能为空
        if (user.getRealName() == null || user.getRealName().isEmpty()) {
            throw new RuntimeException("真实姓名不能为空");
        }
        // 检查用户名是否已存在
        User existingUser = userDao.findByUsername(user.getUsername());
        if (existingUser != null) {
            throw new RuntimeException("用户名 " + user.getUsername() + " 已存在");
        }
        userDao.insert(user);
    }

    @Override
    public User getUserById(Long id) {
        if (id == null) {
            throw new RuntimeException("用户ID不能为空");
        }
        User user = userDao.findById(id);
        if (user == null) {
            throw new RuntimeException("未找到ID为 " + id + " 的用户");
        }
        return user;
    }

    @Override
    public User getUserByUsername(String username) {
        if (username == null || username.isEmpty()) {
            throw new RuntimeException("用户名不能为空");
        }
        return userDao.findByUsername(username);
    }

    @Override
    public List<User> getAllTeachers() {
        return userDao.findAllTeachers();
    }

    @Override
    public List<User> getAllUsers() {
        return userDao.findAll();
    }

    @Override
    public void updateUser(User user) {
        if (user.getId() == null) {
            throw new RuntimeException("用户ID不能为空");
        }
        // 检查用户是否存在
        User existingUser = userDao.findById(user.getId());
        if (existingUser == null) {
            throw new RuntimeException("未找到ID为 " + user.getId() + " 的用户");
        }
        userDao.update(user);
    }

    @Override
    public void deleteUser(Long id) {
        if (id == null) {
            throw new RuntimeException("用户ID不能为空");
        }
        User existingUser = userDao.findById(id);
        if (existingUser == null) {
            throw new RuntimeException("未找到ID为 " + id + " 的用户");
        }
        userDao.deleteById(id);
    }
}