package com.example.attendancesystem.config;

import com.example.attendancesystem.security.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(customUserDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return new ProviderManager(provider);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // ========== 公开页面（免登录）==========
                        .requestMatchers(
                                "/page/login", "/page/register",
                                "/task/scan", "/task/doCheckin",
                                "/attendance/checkin", "/attendance/doCheckin",
                                "/attendance/taskCheckin", "/attendance/checkin-submit",
                                "/css/**", "/js/**", "/webjars/**"
                        ).permitAll()

                        // ========== 需要登录的页面 ==========
                        // 任务管理（教师专用）
                        .requestMatchers("/task/publish", "/task/list").authenticated()
                        // 考勤记录
                        .requestMatchers("/attendance/list").authenticated()
                        // 批量导入
                        .requestMatchers("/attendance/import", "/student/import",
                                "/template/attendance.xlsx", "/template/student.xlsx").authenticated()
                        // 学生管理
                        .requestMatchers("/page/student/list", "/page/student/add",
                                "/page/student/edit/**", "/page/student/save",
                                "/page/student/delete/**").authenticated()
                        // 请假相关
                        .requestMatchers("/leave/add", "/leave/submit",
                                "/leave/approve", "/leave/approve/**").authenticated()
                        // 其他所有请求需要认证
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/page/login")
                        .loginProcessingUrl("/user/login")
                        .defaultSuccessUrl("/page/dashboard", true)
                        .failureUrl("/page/login?error")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/page/login?logout")
                        .permitAll()
                );

        return http.build();
    }
}