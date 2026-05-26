package cloud.hilang.cs_member.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 安全配置DTO
 *
 * @author HiLang Cloud Team
 * @since 2025-11-26
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SecurityConfigDTO {
    /**
     * 密码最小长度
     */
    private Integer passwordMinLength;

    /**
     * 登录最大尝试次数
     */
    private Integer loginMaxAttempts;

    /**
     * 登录锁定时长（分钟）
     */
    private Integer loginLockoutDuration;

    /**
     * 密码复杂度要求
     */
    private Boolean passwordComplexity;

    /**
     * 会话超时时间（分钟）
     */
    private Integer sessionTimeout;

    /**
     * 令牌有效期（小时）
     */
    private Integer tokenExpireHours;

    /**
     * 刷新令牌有效期（天）
     */
    private Integer refreshTokenExpireDays;
}
