package cloud.hilang.cs_member.security;

import cloud.hilang.cs_member.service.AdminAuthService;
import cloud.hilang.cs_member.service.MemberAuthService;
import cloud.hilang.cs_member.util.AdminJwtUtil;
import cloud.hilang.cs_member.util.MemberJwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * JWT认证过滤器
 * 支持会员端和管理端的双重JWT认证体系
 *
 * @author HiLang Cloud Team
 * @since 2025-01-14
 */
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private MemberAuthService memberAuthService;
    private AdminAuthService adminAuthService;
    private MemberJwtUtil memberJwtUtil;
    private AdminJwtUtil adminJwtUtil;

    private static final String MEMBER_PREFIX = "/api/v1/member";
    private static final String ADMIN_PREFIX = "/api/v1/admin";
    private static final String AUTH_PREFIX = "/api/v1/auth";

    // Setter methods for dependency injection (to avoid circular dependency)
    public void setMemberAuthService(MemberAuthService memberAuthService) {
        this.memberAuthService = memberAuthService;
    }

    public void setAdminAuthService(AdminAuthService adminAuthService) {
        this.adminAuthService = adminAuthService;
    }

    public void setMemberJwtUtil(MemberJwtUtil memberJwtUtil) {
        this.memberJwtUtil = memberJwtUtil;
    }

    public void setAdminJwtUtil(AdminJwtUtil adminJwtUtil) {
        this.adminJwtUtil = adminJwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                  FilterChain filterChain) throws ServletException, IOException {
        try {
            String requestURI = request.getRequestURI();
            String token = extractTokenFromRequest(request);

            // 如果没有令牌，继续过滤器链
            if (!StringUtils.hasText(token)) {
                filterChain.doFilter(request, response);
                return;
            }

            // 根据请求路径选择验证方式
            if (requestURI.startsWith(MEMBER_PREFIX)) {
                authenticateMemberToken(token, request);
            } else if (requestURI.startsWith(ADMIN_PREFIX) || requestURI.startsWith(AUTH_PREFIX)) {
                authenticateAdminToken(token, request);
            } else {
                // 对于其他路径，尝试两种令牌验证
                boolean isMember = authenticateMemberToken(token, request);
                if (!isMember) {
                    authenticateAdminToken(token, request);
                }
            }

        } catch (Exception e) {
            log.error("JWT认证失败: {}", e.getMessage());
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    /**
     * 验证会员令牌
     *
     * @param token JWT令牌
     * @param request HTTP请求
     * @return 是否验证成功
     */
    private boolean authenticateMemberToken(String token, HttpServletRequest request) {
        try {
            // 验证会员令牌 - token已经是纯净的（无Bearer前缀）
            if (memberAuthService.validateToken(token)) {
                Long memberId = memberJwtUtil.getUserIdFromToken(token);
                String phone = memberJwtUtil.getPhoneFromToken(token);
                String memberNo = memberJwtUtil.getMemberNoFromToken(token);

                // 创建认证对象
                CustomUserDetails userDetails = new CustomUserDetails(
                        memberId,
                        phone,
                        memberNo,
                        "MEMBER",
                        List.of(new SimpleGrantedAuthority("ROLE_MEMBER"))
                );

                UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                    );
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.debug("会员JWT认证成功: memberId={}, phone={}", memberId, phone);
                return true;
            }
        } catch (Exception e) {
            log.debug("会员令牌验证失败: {}", e.getMessage());
        }
        return false;
    }

    /**
     * 验证管理员令牌
     *
     * @param token JWT令牌
     * @param request HTTP请求
     * @return 是否验证成功
     */
    private boolean authenticateAdminToken(String token, HttpServletRequest request) {
        try {
            // 验证管理员令牌 - token已经是纯净的（无Bearer前缀）
            if (adminAuthService.validateToken(token)) {
                Long userId = adminJwtUtil.getUserIdFromToken(token);
                String phone = adminJwtUtil.getPhoneFromToken(token);
                String roleCode = adminJwtUtil.getRoleCodeFromToken(token);

                // 获取用户权限
                List<String> permissions = adminJwtUtil.getPermissionsFromToken(token);
                List<SimpleGrantedAuthority> authorities = permissions.stream()
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());

                // 添加角色权限
                authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
                authorities.add(new SimpleGrantedAuthority("ROLE_" + roleCode));

                // 创建认证对象
                CustomUserDetails userDetails = new CustomUserDetails(
                        userId,
                        phone,
                        phone,
                        "ADMIN",
                        authorities
                );
                userDetails.setRole(roleCode);
                userDetails.setPermissions(permissions);

                UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                    );
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.debug("管理员JWT认证成功: userId={}, phone={}, role={}", userId, phone, roleCode);
                return true;
            }
        } catch (Exception e) {
            log.debug("管理员令牌验证失败: {}", e.getMessage());
        }
        return false;
    }

    /**
     * 从请求中提取JWT令牌
     *
     * @param request HTTP请求
     * @return JWT令牌
     */
    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7); // 移除 "Bearer " 前缀，返回纯净的token
        }

        return null;
    }

}