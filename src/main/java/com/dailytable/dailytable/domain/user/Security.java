package com.dailytable.dailytable.domain.user;

import com.dailytable.dailytable.global.jwt.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class Security {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public Security(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // 인증 없이 열어둘 엔드포인트
                        .requestMatchers("/auth/signup", "/auth/login", "/auth/logout", "/auth/token/refresh").permitAll()
                        .requestMatchers("/", "/css/**", "/js/**", "/images/**").permitAll()
                        .requestMatchers("/gacha/**").permitAll()
                        .requestMatchers("/api/gacha/**").permitAll()
                        .requestMatchers("/recipe/**").permitAll()
                        .requestMatchers("/api/recipe/**").permitAll()
                        .requestMatchers("/login", "/signup").permitAll()

                        // 프로필 / 마이페이지는 인증 필요
                        .requestMatchers("/users/me", "/users/me/**").authenticated()

                        // 그 외 나머지
                        .anyRequest().permitAll()
                )
                // 폼 로그인 / 기본 인증은 사용 안 함 (우리가 직접 만든 로그인 사용)
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable());

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}