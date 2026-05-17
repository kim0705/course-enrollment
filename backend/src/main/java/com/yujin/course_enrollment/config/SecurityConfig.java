package com.yujin.course_enrollment.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security 설정
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtUtil jwtUtil;

    /* 비밀번호 암호화 빈 */
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                /* CORS 설정 */
                .cors(Customizer.withDefaults())
                /* CSRF 비활성화 — JWT 사용으로 불필요 */
                .csrf(AbstractHttpConfigurer::disable)
                /* 세션 미사용 — JWT로 stateless 인증 */
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        /* H2 콘솔, 인증 엔드포인트는 인증 없이 허용 */
                        .requestMatchers("/h2-console/**", "/error").permitAll()
                        .requestMatchers("/api/auth/login").permitAll()
                        .requestMatchers("/api/auth/signup").anonymous()
                        .requestMatchers("/api/auth/logout", "/api/auth/refresh", "/api/auth/check-username", "/api/auth/check-email").permitAll()
                        /* 강사 전용 */
                        .requestMatchers(HttpMethod.GET, "/api/courses/my", "/api/courses/{courseId}/enrollments").hasRole("CREATOR")
                        .requestMatchers(HttpMethod.POST, "/api/courses").hasRole("CREATOR")
                        .requestMatchers(HttpMethod.PUT, "/api/courses/{courseId}").hasRole("CREATOR")
                        .requestMatchers(HttpMethod.PATCH, "/api/courses/{courseId}/publish", "/api/courses/{courseId}/close").hasRole("CREATOR")
                        /* 강의 목록/상세 조회는 인증 없이 허용 */
                        .requestMatchers(HttpMethod.GET, "/api/courses", "/api/courses/{courseId}").permitAll()
                        /* 수강생/강사 공통 */
                        .requestMatchers("/api/enrollments/**", "/api/payments/**").hasAnyRole("STUDENT", "CREATOR")
                        /* 프로필 수정 (STUDENT / CREATOR 전용) */
                        .requestMatchers(HttpMethod.PATCH, "/api/users/me", "/api/users/me/password").hasAnyRole("STUDENT", "CREATOR")
                        /* 수강생 전용 */
                        .requestMatchers(HttpMethod.POST, "/api/creator-requests").hasRole("STUDENT")
                        /* 관리자 전용 */
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .exceptionHandling(e -> e
                        /* 미인증 요청 — 401 */
                        .authenticationEntryPoint((request, response, ex) ->
                                response.sendError(HttpServletResponse.SC_UNAUTHORIZED))
                        /* 인증됐으나 권한 없음 — 403 */
                        .accessDeniedHandler((request, response, ex) ->
                                response.sendError(HttpServletResponse.SC_FORBIDDEN)))
                /* H2 콘솔 iframe 허용 */
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
                .addFilterBefore(new JwtFilter(jwtUtil), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
