package com.example.attendancesystem.dao;

import com.example.attendancesystem.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository  // 1. 使用 @Repository 注解
public class UserDao {

    @Autowired  // 2. 注入 JdbcTemplate
    private JdbcTemplate jdbcTemplate;

    /**
     * 3. 实现 insert 方法（新增教师用户）
     */
    public void insert(User user) {
        String sql = "INSERT INTO user (username, password, real_name, role) VALUES (?, ?, ?, ?)";
        jdbcTemplate.update(sql,
                user.getUsername(),
                user.getPassword(),
                user.getRealName(),
                user.getRole());
    }

    /**
     * 4. 实现 findById 方法（根据 ID 查询）
     */
    public User findById(Long id) {
        String sql = "SELECT id, username, password, real_name as realName, role, create_time as createTime FROM user WHERE id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, new BeanPropertyRowMapper<>(User.class), id);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 5. 实现 findByUsername 方法（根据用户名查询，用于登录验证）
     */
    public User findByUsername(String username) {
        String sql = "SELECT id, username, password, real_name as realName, role, create_time as createTime FROM user WHERE username = ?";
        try {
            return jdbcTemplate.queryForObject(sql, new BeanPropertyRowMapper<>(User.class), username);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 6. 实现 findAllTeachers 方法（查询所有教师）
     */
    public List<User> findAllTeachers() {
        String sql = "SELECT id, username, password, real_name as realName, role, create_time as createTime FROM user WHERE role = 'TEACHER'";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(User.class));
    }

    /**
     * 7. 实现 update 方法（更新用户）
     */
    public void update(User user) {
        String sql = "UPDATE user SET password = ?, real_name = ?, role = ? WHERE id = ?";
        jdbcTemplate.update(sql,
                user.getPassword(),
                user.getRealName(),
                user.getRole(),
                user.getId());
    }

    /**
     * 8. 实现 deleteById 方法（删除用户）
     */
    public void deleteById(Long id) {
        String sql = "DELETE FROM user WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    /**
     * 额外方法：查询所有用户
     */
    public List<User> findAll() {
        String sql = "SELECT id, username, password, real_name as realName, role, create_time as createTime FROM user";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(User.class));
    }

    /**
     * 额外方法：根据角色统计用户数量
     */
    public Integer countByRole(String role) {
        String sql = "SELECT COUNT(*) FROM user WHERE role = ?";
        return jdbcTemplate.queryForObject(sql, Integer.class, role);
    }
}