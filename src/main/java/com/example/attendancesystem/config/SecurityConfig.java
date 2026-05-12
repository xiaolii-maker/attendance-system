package com.example.attendancesystem.config;

import com.example.attendancesystem.security.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
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
                // 1. 禁用 CSRF（允许表单提交）
                .csrf(csrf -> csrf.disable())

                // 2. 配置授权
                .authorizeHttpRequests(auth -> auth
                        // 公开页面和接口
                        .requestMatchers("/login", "/register", "/dashboard",
                                "/user/login", "/user/register", "/user/register-page",
                                "/css/**", "/js/**").permitAll()
                        // 其他请求需要认证
                        .anyRequest().authenticated()
                )

                // 3. 配置表单登录
                .formLogin(form -> form
                        .loginPage("/login")                    // 自定义登录页面
                        .loginProcessingUrl("/user/login")      // 登录表单提交地址
                        .defaultSuccessUrl("/dashboard", true)  // 登录成功跳转
                        .failureUrl("/login?error")             // 登录失败跳转
                        .permitAll()
                )

                // 4. 配置退出登录
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                );

        return http.build();
    }
}