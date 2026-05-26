package cloud.hilang.cs_member.service;

import cloud.hilang.cs_member.dto.LoginRequest;
import cloud.hilang.cs_member.dto.LoginResponse;
import cloud.hilang.cs_member.entity.Member;
import cloud.hilang.cs_member.mapper.MemberMapper;
import cloud.hilang.cs_member.util.MemberJwtUtil;
import cloud.hilang.cs_member.dto.SecurityConfigDTO;
import cloud.hilang.cs_member.exception.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * 会员认证服务
 * 处理会员用户的登录、认证、令牌管理等业务
 *
 * @author HiLang Cloud Team
 * @since 2025-01-14
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MemberAuthService {

    private final MemberMapper memberMapper;
    private final MemberJwtUtil memberJwtUtil;
    private final RedisTemplate<String, Object> redisTemplate;
    private final BCryptPasswordEncoder passwordEncoder;
    private final SystemConfigService systemConfigService;

    private static final Pattern PHONE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");

    /**
     * 会员登录
     * 仅支持手机号+密码登录方式
     * 防止用户名枚举攻击
     */
    public LoginResponse login(LoginRequest request) {
        String phone = request.getPhone();

        // 1. 验证手机号格式
        validatePhoneNumber(phone);

        // 2. 检查账户是否被锁定
        checkAccountLockout(phone);

        // 3. 根据手机号查找会员（仅支持手机号登录）
        Member member = memberMapper.selectByPhone(phone);
        
        // 4. 验证密码（防止时间差异攻击）
        boolean isPasswordValid = false;
        if (member != null) {
            isPasswordValid = passwordEncoder.matches(request.getPassword(), member.getPassword());
        } else {
            // 即使用户不存在，也执行一次BCrypt验证以保持一致的响应时间
            // 防止通过时间差异进行用户枚举攻击
            passwordEncoder.matches(request.getPassword(), 
                "$2a$10$dummyHashToPreventTimingAttack1234567890123456789012");
        }

        // 5. 统一的失败处理（不区分用户不存在还是密码错误）
        if (member == null || !isPasswordValid) {
            recordLoginFailure(phone);
            // 使用通用错误消息，不暴露具体原因
            throw new AuthenticationException("INVALID_CREDENTIALS", "手机号或密码错误");
        }

        // 6. 检查会员状态
        if (member.getStatus() != 1) {
            String statusMessage = getStatusMessage(member.getStatus());
            throw new AuthenticationException("ACCOUNT_DISABLED", statusMessage);
        }

        // 7. 登录成功，清除失败记录
        clearLoginFailures(phone);

        // 8. 生成令牌
        String accessToken = memberJwtUtil.generateAccessToken(
            member.getId(),
            member.getMemberNo(),
            member.getPhone()
        );

        String refreshToken = memberJwtUtil.generateRefreshToken(
            member.getId(),
            member.getMemberNo(),
            member.getPhone()
        );

        // 9. 存储令牌到Redis并初始化会话
        storeTokenToRedis(member.getId(), accessToken, refreshToken);

        log.info("会员登录成功: phone={}, memberId={}", maskPhone(phone), member.getId());

        return LoginResponse.builder()
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .tokenType("Bearer")
            .expiresIn(memberJwtUtil.getRemainingValidity(accessToken))
            .build();
    }

    /**
     * 验证手机号格式
     *
     * @param phone 手机号
     */
    private void validatePhoneNumber(String phone) {
        if (phone == null || !PHONE_PATTERN.matcher(phone).matches()) {
            throw new InvalidPhoneException("手机号格式不正确");
        }
    }

    /**
     * 检查账户是否被锁定
     *
     * @param phone 手机号
     */
    private void checkAccountLockout(String phone) {
        String lockKey = "login:lock:member:" + phone;
        if (Boolean.TRUE.equals(redisTemplate.hasKey(lockKey))) {
            Long remainingTime = redisTemplate.getExpire(lockKey, TimeUnit.MINUTES);
            if (remainingTime != null && remainingTime > 0) {
                throw new AccountLockedException("账户已被锁定，请" + remainingTime + "分钟后再试");
            } else {
                // 锁定已过期，删除锁定记录
                redisTemplate.delete(lockKey);
            }
        }
    }

    /**
     * 记录登录失败
     *
     * @param phone 手机号
     */
    private void recordLoginFailure(String phone) {
        SecurityConfigDTO config = systemConfigService.getSecurityConfig();
        String failKey = "login:fail:member:" + phone;

        Long failCount = redisTemplate.opsForValue().increment(failKey);
        if (failCount == 1) {
            // 第一次失败，设置15分钟过期
            redisTemplate.expire(failKey, 15, TimeUnit.MINUTES);
        }

        log.warn("会员登录失败: phone={}, 失败次数={}", maskPhone(phone), failCount);

        // 检查是否达到最大尝试次数
        if (failCount >= config.getLoginMaxAttempts()) {
            // 锁定账户
            String lockKey = "login:lock:member:" + phone;
            redisTemplate.opsForValue().set(lockKey, "locked",
                config.getLoginLockoutDuration(), TimeUnit.MINUTES);

            // 删除失败计数
            redisTemplate.delete(failKey);

            log.warn("会员账户已锁定: phone={}, 锁定时长={}分钟",
                maskPhone(phone), config.getLoginLockoutDuration());

            throw new AccountLockedException(config.getLoginLockoutDuration());
        }
    }

    /**
     * 清除登录失败记录
     *
     * @param phone 手机号
     */
    private void clearLoginFailures(String phone) {
        String failKey = "login:fail:member:" + phone;
        redisTemplate.delete(failKey);
    }

    /**
     * 获取用户状态对应的错误消息
     *
     * @param status 用户状态
     * @return 错误消息
     */
    private String getStatusMessage(Integer status) {
        switch (status) {
            case 0:
                return "账户已被禁用，请联系管理员";
            case 2:
                return "账户已被冻结，请联系管理员";
            default:
                return "账户状态异常，请联系管理员";
        }
    }

    /**
     * 脱敏手机号
     *
     * @param phone 手机号
     * @return 脱敏后的手机号
     */
    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) {
            return "***";
        }
        return phone.substring(0, 3) + "****" + phone.substring(7);
    }

    /**
     * 刷新令牌
     */
    public LoginResponse refreshToken(String refreshToken) {
        // 验证刷新令牌
        if (!memberJwtUtil.validateToken(refreshToken) || !memberJwtUtil.isRefreshToken(refreshToken)) {
            throw new AuthenticationException("INVALID_REFRESH_TOKEN", "无效的刷新令牌");
        }

        // 检查刷新令牌是否存在于Redis中（多设备登录支持）
        String refreshKey = "jwt:member:refresh:" + refreshToken;
        if (!Boolean.TRUE.equals(redisTemplate.hasKey(refreshKey))) {
             throw new AuthenticationException("REFRESH_TOKEN_REVOKED", "刷新令牌已失效或被撤销");
        }

        // 获取用户信息
        Long memberId = memberJwtUtil.getUserIdFromToken(refreshToken);
        String memberNo = memberJwtUtil.getMemberNoFromToken(refreshToken);
        String phone = memberJwtUtil.getPhoneFromToken(refreshToken);

        // 生成新的访问令牌
        String newAccessToken = memberJwtUtil.generateAccessToken(memberId, memberNo, phone);

        // 更新Redis中的访问令牌并初始化会话
        SecurityConfigDTO securityConfig = systemConfigService.getSecurityConfig();
        long tokenExpiration = securityConfig.getTokenExpireHours() * 60 * 60L;
        String tokenKey = "jwt:member:access:" + newAccessToken;
        redisTemplate.opsForValue().set(tokenKey, memberId, tokenExpiration, TimeUnit.SECONDS);
        
        // 初始化会话活跃时间
        String lastActiveKey = "session:active:member:" + newAccessToken;
        redisTemplate.opsForValue().set(lastActiveKey, 
            String.valueOf(System.currentTimeMillis()), 
            securityConfig.getSessionTimeout(), 
            TimeUnit.MINUTES);

        return LoginResponse.builder()
            .accessToken(newAccessToken)
            .refreshToken(refreshToken)
            .tokenType("Bearer")
            .expiresIn(memberJwtUtil.getRemainingValidity(newAccessToken))
            .build();
    }

    /**
     * 登出
     */
    public void logout(String token) {
        try {
            // 移除令牌前缀
            String cleanToken = memberJwtUtil.removePrefix(token);
            removeTokenFromRedis(cleanToken);
        } catch (Exception e) {
            log.warn("登出操作失败: {}", e.getMessage());
        }
    }

    /**
     * 验证令牌
     */
    public boolean validateToken(String token) {
        try {
            if (!memberJwtUtil.validateToken(token) || memberJwtUtil.isTokenExpired(token)) {
                return false;
            }

            // 检查令牌是否被撤销（包含会话超时检查）
            return !isTokenRevoked(token);
        } catch (Exception e) {
            log.error("令牌验证失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 存储令牌到Redis
     * 采用白名单模式：key为token，value为userId，支持多设备登录
     * 同时初始化会话活跃时间
     */
    private void storeTokenToRedis(Long memberId, String accessToken, String refreshToken) {
        try {
            SecurityConfigDTO securityConfig = systemConfigService.getSecurityConfig();
            long tokenExpiration = securityConfig.getTokenExpireHours() * 60 * 60L;
            long refreshExpiration = securityConfig.getRefreshTokenExpireDays() * 24 * 60 * 60L;

            // 存储 Access Token
            String tokenKey = "jwt:member:access:" + accessToken;
            redisTemplate.opsForValue().set(tokenKey, memberId, tokenExpiration, TimeUnit.SECONDS);

            // 存储 Refresh Token
            String refreshKey = "jwt:member:refresh:" + refreshToken;
            redisTemplate.opsForValue().set(refreshKey, memberId, refreshExpiration, TimeUnit.SECONDS);
            
            // 初始化会话活跃时间
            String lastActiveKey = "session:active:member:" + accessToken;
            redisTemplate.opsForValue().set(lastActiveKey, 
                String.valueOf(System.currentTimeMillis()), 
                securityConfig.getSessionTimeout(), 
                TimeUnit.MINUTES);
            
            log.debug("会话已初始化: memberId={}, sessionTimeout={}分钟", 
                memberId, securityConfig.getSessionTimeout());
        } catch (Exception e) {
            log.error("存储令牌到Redis失败: {}", e.getMessage());
        }
    }

    /**
     * 从Redis移除令牌
     */
    private void removeTokenFromRedis(String token) {
        try {
            String tokenKey = "jwt:member:access:" + token;
            String lastActiveKey = "session:active:member:" + token;
            redisTemplate.delete(tokenKey);
            redisTemplate.delete(lastActiveKey);
        } catch (Exception e) {
            log.warn("从Redis移除令牌失败: {}", e.getMessage());
        }
    }

    /**
     * 检查令牌是否被撤销
     * 如果Redis中不存在该Token，则视为已撤销（白名单模式）
     * 同时检查会话是否超时（基于最后活跃时间）
     */
    private boolean isTokenRevoked(String token) {
        try {
            String tokenKey = "jwt:member:access:" + token;
            // 如果键不存在，说明Token无效或已退出
            if (!Boolean.TRUE.equals(redisTemplate.hasKey(tokenKey))) {
                return true;
            }
            
            // 检查会话超时
            SecurityConfigDTO config = systemConfigService.getSecurityConfig();
            String lastActiveKey = "session:active:member:" + token;
            String lastActiveTimeStr = (String) redisTemplate.opsForValue().get(lastActiveKey);
            
            if (lastActiveTimeStr != null) {
                try {
                    long lastActiveTime = Long.parseLong(lastActiveTimeStr);
                    long currentTime = System.currentTimeMillis();
                    long inactiveMinutes = (currentTime - lastActiveTime) / (60 * 1000);
                    
                    if (inactiveMinutes > config.getSessionTimeout()) {
                        // 会话超时，删除令牌
                        log.info("会话超时: token={}, 不活跃时长={}分钟", 
                            token.substring(0, Math.min(10, token.length())) + "...", inactiveMinutes);
                        redisTemplate.delete(tokenKey);
                        redisTemplate.delete(lastActiveKey);
                        return true;
                    }
                } catch (NumberFormatException e) {
                    log.warn("解析最后活跃时间失败: {}", e.getMessage());
                }
            }
            
            // 更新最后活跃时间
            redisTemplate.opsForValue().set(lastActiveKey, 
                String.valueOf(System.currentTimeMillis()), 
                config.getSessionTimeout(), 
                TimeUnit.MINUTES);
            
            return false;
        } catch (Exception e) {
            log.warn("检查令牌撤销状态失败: {}", e.getMessage());
            return true;
        }
    }

    /**
     * 从令牌获取用户信息
     */
    public Object getUserInfoFromToken(String token) {
        try {
            if (!validateToken(token)) {
                return null;
            }

            final Long memberId = memberJwtUtil.getUserIdFromToken(token);
            final String memberNo = memberJwtUtil.getMemberNoFromToken(token);
            final String phone = memberJwtUtil.getPhoneFromToken(token);

            final Long memberIdFinal = memberId;
            final String memberNoFinal = memberNo;
            final String phoneFinal = phone;

            return new Object() {
                public final Long id = memberIdFinal;
                public final String memberNo = memberNoFinal;
                public final String phoneNum = phoneFinal;
                public final String role = "MEMBER";
            };
        } catch (Exception e) {
            log.error("从令牌获取用户信息失败: {}", e.getMessage());
            return null;
        }
    }
}