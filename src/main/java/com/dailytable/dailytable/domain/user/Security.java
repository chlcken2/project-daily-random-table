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
                        // 인증 없이 열어둘 엔드포인트 (화면 + 공개 페이지/API)
                        .requestMatchers(
                                "/",                // 메인 홈
                                "/login", "/signup" // 로그인/회원가입 화면
                        ).permitAll()

                        // 인증 관련 API (로그인 자체는 토큰 없이 가능해야 함)
                        .requestMatchers(
                                "/auth/signup",
                                "/auth/login",
                                "/auth/logout",
                                "/auth/token/refresh"
                        ).permitAll()

                        // 정적 리소스
                        .requestMatchers("/css/**", "/js/**", "/images/**").permitAll()

                        // 공개 화면/페이지 (가챠, 레시피 상세, 랭킹 등)
                        .requestMatchers(
                                "/gacha/**",
                                "/recipes/**",
                                "/api/ranking/**"
                        ).permitAll()

                        // 로그인 필요 영역 (마이페이지/내 데이터)
                        .requestMatchers("/users/me", "/users/me/**").authenticated()
                        .requestMatchers("/api/ingredients/**").authenticated()

                        // 그 외 나머지
                        .anyRequest().permitAll()
                )
                // 폼 로그인 / 기본 인증은 사용 안 함 (우리가 직접 만든 로그인 사용)
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())
                .exceptionHandling(ex -> ex.authenticationEntryPoint((request, response, authException) -> {
                    response.sendRedirect("/login");
                }));

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}