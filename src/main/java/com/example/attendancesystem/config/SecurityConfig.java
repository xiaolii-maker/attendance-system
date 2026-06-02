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
                        // 公开页面
                        .requestMatchers("/page/login", "/page/register", "/page/dashboard",
                                "/user/login", "/user/register",
                                "/css/**", "/js/**", "/webjars/**").permitAll()
                        // 学生管理页面
                        .requestMatchers("/page/student/list", "/page/student/add",
                                "/page/student/edit/**", "/page/student/save",
                                "/page/student/delete/**").permitAll()
                        // 考勤页面
                        .requestMatchers("/attendance/checkin", "/attendance/list",
                                "/attendance/checkin/**", "/attendance/list/**").permitAll()
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