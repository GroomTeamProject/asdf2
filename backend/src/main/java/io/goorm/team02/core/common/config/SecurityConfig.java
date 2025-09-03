package io.goorm.team02.core.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

@EnableWebSecurity
@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf().disable() // 테스트용
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**","/error").permitAll() // /signup, /login 공개
                .anyRequest().authenticated() // 나머지 요청은 인증 필요
            )
            .formLogin().disable() // REST API에서는 기본 로그인 페이지 비활성화
            .httpBasic().disable(); // 필요시 활성화
        return http.build();
    }
}
