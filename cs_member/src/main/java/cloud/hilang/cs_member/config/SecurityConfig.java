package cloud.hilang.cs_member.config;

import cloud.hilang.cs_member.security.JwtAuthenticationFilter;
import cloud.hilang.cs_member.service.MemberAuthService;
import cloud.hilang.cs_member.service.AdminAuthService;
import cloud.hilang.cs_member.util.MemberJwtUtil;
import cloud.hilang.cs_member.util.AdminJwtUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Spring Security配置类
 * 配置双端JWT认证体系，支持会员端和管理端的分离认证
 *
 * @author HiLang Cloud Team
 * @since 2025-01-14
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    /**
     * 密码编码器
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * BCrypt密码编码器
     */
    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * CORS配置源
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 允许的源
        configuration.setAllowedOriginPatterns(List.of(
            "http://localhost:3000",
            "http://127.0.0.1:3000",
            "http://localhost:8080",
            "http://127.0.0.1:8080"
        ));

        // 允许的HTTP方法
        configuration.setAllowedMethods(Arrays.asList(
            "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"
        ));

        // 允许的请求头
        configuration.setAllowedHeaders(List.of("*"));

        // 允许凭证
        configuration.setAllowCredentials(true);

        // 暴露的响应头
        configuration.setExposedHeaders(List.of(
            "Authorization", "Content-Type", "X-Total-Count"
        ));

        // 预检请求缓存时间
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /**
     * JWT认证过滤器Bean
     */
    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(
            MemberAuthService memberAuthService,
            AdminAuthService adminAuthService,
            MemberJwtUtil memberJwtUtil,
            AdminJwtUtil adminJwtUtil) {
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter();
        filter.setMemberAuthService(memberAuthService);
        filter.setAdminAuthService(adminAuthService);
        filter.setMemberJwtUtil(memberJwtUtil);
        filter.setAdminJwtUtil(adminJwtUtil);
        return filter;
    }

    /**
     * 安全过滤器链配置
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
            JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {
        http
            // 禁用CSRF（使用JWT时不需要）
            .csrf(AbstractHttpConfigurer::disable)

            // 启用CORS
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))

            // 配置会话管理为无状态
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            // 添加JWT过滤器
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

            // 配置授权规则 - 按照优先级从高到低排列
            .authorizeHttpRequests(authz -> authz
                // 1. 静态资源 - 完全公开（最高优先级）
                .requestMatchers(
                    "/uploads/**",
                    "/static/**",
                    "/public/**",
                    "/css/**",
                    "/js/**",
                    "/images/**",
                    "/favicon.ico"
                ).permitAll()

                // 2. API文档 - 开发时公开
                .requestMatchers(
                    "/swagger-ui/**",
                    "/swagger-ui.html",
                    "/v3/api-docs/**",
                    "/swagger-resources/**",
                    "/configuration/**",
                    "/webjars/**",
                    "/api/v1/swagger-ui/**",
                    "/api/v1/swagger-ui.html",
                    "/api/v1/v3/api-docs/**",
                    "/api/v1/swagger-resources/**",
                    "/api/v1/configuration/**",
                    "/api/v1/webjars/**"
                ).permitAll()

                // 3. 认证相关接口 - 无需登录即可访问
                .requestMatchers(
                    "/api/v1/member/login",
                    "/api/v1/admin/login",
                    "/api/v1/member/register",
                    "/api/v1/member/refresh",
                    "/api/v1/admin/refresh",
                    "/api/v1/auth/health"
                ).permitAll()

                // 4. 忘记密码流程 - 无需登录即可访问（重要：放在角色权限之前）
                .requestMatchers(
                    "/api/v1/member/forgot-password/**",
                    "/api/v1/admin/forgot-password/**"
                ).permitAll()

                // 5. 邮箱验证 - 无需登录即可访问
                .requestMatchers(
                    "/api/v1/member/email-verification/**",
                    "/api/v1/admin/email-verification/**"
                ).permitAll()

                // 6. 会员等级配置查询 - 无需登录即可访问（用于注册时展示）
                .requestMatchers(
                    "/api/v1/member/grade-config/**"
                ).permitAll()

                // 7. 公共接口 - 无需登录即可访问
                .requestMatchers(
                    "/api/v1/common/**"
                ).permitAll()

                // 8. 会员端API - 需要MEMBER角色（排除已在上面配置为公开的接口）
                .requestMatchers("/api/v1/member/**").hasRole("MEMBER")

                // 9. 管理端API - 需要ADMIN角色（排除已在上面配置为公开的接口）
                .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")

                // 10. 其他所有请求都需要认证（兜底规则）
                .anyRequest().authenticated()
            )

            // 配置异常处理
            .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setStatus(401);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("""
                        {
                            "code": 401,
                            "message": "未认证，请先登录",
                            "data": null,
                            "timestamp": %d
                        }
                        """.formatted(System.currentTimeMillis()));
                })
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    response.setStatus(403);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("""
                        {
                            "code": 403,
                            "message": "权限不足，无法访问此资源",
                            "data": null,
                            "timestamp": %d
                        }
                        """.formatted(System.currentTimeMillis()));
                })
            )

            // 配置安全头
            .headers(headers -> headers
                .frameOptions(frameOptions -> frameOptions.deny())
                .contentTypeOptions(contentTypeOptions -> {})
            );

        return http.build();
    }
}