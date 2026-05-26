package cloud.hilang.cs_member.util;

import cloud.hilang.cs_member.service.SystemConfigService;
import cloud.hilang.cs_member.dto.SecurityConfigDTO;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 会员JWT工具类
 * 处理会员用户JWT令牌的生成、解析、验证等操作
 *
 * @author HiLang Cloud Team
 * @since 2025-01-14
 */
@Slf4j
@Component
public class MemberJwtUtil {

    private final SystemConfigService systemConfigService;

    public MemberJwtUtil(SystemConfigService systemConfigService) {
        this.systemConfigService = systemConfigService;
    }

    /**
     * JWT密钥
     */
    @Value("${jwt.member.secret}")
    private String secret;

    /**
     * 令牌前缀
     */
    @Value("${jwt.member.token-prefix}")
    private String tokenPrefix;

    /**
     * 用户ID声明
     */
    @Value("${jwt.member.user-id-claim}")
    private String userIdClaim;

    /**
     * 会员编号声明
     */
    @Value("${jwt.member.memberno-claim}")
    private String membernoClaim;

    /**
     * 手机号声明
     */
    @Value("${jwt.member.phone-claim}")
    private String phoneClaim;

    /**
     * 获取签名密钥
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    /**
     * 生成会员访问令牌
     */
    public String generateAccessToken(Long memberId, String memberNo, String phone) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(userIdClaim, memberId);
        claims.put(membernoClaim, memberNo);
        claims.put(phoneClaim, phone);
        claims.put("tokenType", "ACCESS");

        SecurityConfigDTO securityConfig = systemConfigService.getSecurityConfig();
        long expiration = securityConfig.getTokenExpireHours() * 60 * 60 * 1000L;

        return createToken(claims, memberId.toString(), expiration);
    }

    /**
     * 生成会员刷新令牌
     */
    public String generateRefreshToken(Long memberId, String memberNo, String phone) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(userIdClaim, memberId);
        claims.put(membernoClaim, memberNo);
        claims.put(phoneClaim, phone);
        claims.put("tokenType", "REFRESH");

        SecurityConfigDTO securityConfig = systemConfigService.getSecurityConfig();
        long refreshExpiration = securityConfig.getRefreshTokenExpireDays() * 24 * 60 * 60 * 1000L;

        return createToken(claims, memberId.toString(), refreshExpiration);
    }

    /**
     * 创建令牌
     */
    private String createToken(Map<String, Object> claims, String subject, Long expiration) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * 从令牌中获取用户ID
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.get(userIdClaim, Long.class);
    }

    /**
     * 从令牌中获取会员编号
     */
    public String getMemberNoFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.get(membernoClaim, String.class);
    }

    /**
     * 从令牌中获取手机号
     */
    public String getPhoneFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.get(phoneClaim, String.class);
    }

    /**
     * 从令牌中获取令牌类型
     */
    public String getTokenTypeFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.get("tokenType", String.class);
    }

    /**
     * 从令牌中获取过期时间
     */
    public Date getExpirationDateFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.getExpiration();
    }

    /**
     * 从令牌中获取声明
     */
    private Claims getClaimsFromToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 验证令牌是否有效
     */
    public boolean validateToken(String token) {
        try {
            getClaimsFromToken(token);
            return true;
        } catch (SecurityException e) {
            log.error("无效的JWT签名: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.error("令牌格式错误: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.error("令牌已过期: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("不支持的令牌: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("令牌参数非法: {}", e.getMessage());
        } catch (Exception e) {
            log.error("令牌验证失败: {}", e.getMessage());
        }
        return false;
    }

    /**
     * 检查令牌是否过期
     */
    public boolean isTokenExpired(String token) {
        try {
            Date expiration = getExpirationDateFromToken(token);
            return expiration.before(new Date());
        } catch (ExpiredJwtException e) {
            return true;
        } catch (Exception e) {
            log.error("检查令牌过期状态失败: {}", e.getMessage());
            return true;
        }
    }

    /**
     * 检查是否为刷新令牌
     */
    public boolean isRefreshToken(String token) {
        try {
            String tokenType = getTokenTypeFromToken(token);
            return "REFRESH".equals(tokenType);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 检查是否为访问令牌
     */
    public boolean isAccessToken(String token) {
        try {
            String tokenType = getTokenTypeFromToken(token);
            return "ACCESS".equals(tokenType);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 移除令牌前缀
     */
    public String removePrefix(String token) {
        if (token != null && token.startsWith(tokenPrefix + " ")) {
            return token.substring(tokenPrefix.length() + 1).trim();
        }
        return token != null ? token.trim() : null;
    }

    /**
     * 添加令牌前缀
     */
    public String addPrefix(String token) {
        if (token != null && !token.startsWith(tokenPrefix + " ")) {
            return tokenPrefix + " " + token;
        }
        return token;
    }

    /**
     * 获取令牌剩余有效时间（秒）
     */
    public Long getRemainingValidity(String token) {
        try {
            Date expiration = getExpirationDateFromToken(token);
            long remainingTime = expiration.getTime() - System.currentTimeMillis();
            return remainingTime > 0 ? remainingTime / 1000 : 0L;
        } catch (Exception e) {
            return 0L;
        }
    }
}