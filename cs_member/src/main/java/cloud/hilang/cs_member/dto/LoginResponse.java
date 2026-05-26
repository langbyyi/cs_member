package cloud.hilang.cs_member.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 登录响应DTO
 *
 * @author HiLang Cloud Team
 * @since 2025-01-14
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "登录响应")
public class LoginResponse {

    /**
     * JWT访问令牌
     */
    @Schema(description = "JWT访问令牌")
    private String accessToken;

    /**
     * JWT刷新令牌
     */
    @Schema(description = "JWT刷新令牌")
    private String refreshToken;

    /**
     * 令牌类型
     */
    @Schema(description = "令牌类型", example = "Bearer")
    private String tokenType;

    /**
     * 令牌有效期（秒）
     */
    @Schema(description = "令牌有效期（秒）", example = "7200")
    private Long expiresIn;

    /**
     * 用户ID
     */
    @Schema(description = "用户ID")
    private Long userId;

    /**
     * 用户名
     */
    @Schema(description = "用户名")
    private String username;

    /**
     * 手机号
     */
    @Schema(description = "手机号")
    private String phone;

    /**
     * 用户姓名
     */
    @Schema(description = "用户姓名")
    private String name;

    /**
     * 用户头像
     */
    @Schema(description = "用户头像")
    private String avatar;

    /**
     * 用户角色（管理端特有）
     */
    @Schema(description = "用户角色")
    private String role;

    /**
     * 用户权限列表（管理端特有）
     */
    @Schema(description = "用户权限列表")
    private List<String> permissions;

    /**
     * 会员等级（会员端特有）
     */
    @Schema(description = "会员等级")
    private Integer memberLevel;

    /**
     * 当前积分（会员端特有）
     */
    @Schema(description = "当前积分")
    private Integer currentPoints;

    /**
     * 登录时间
     */
    @Schema(description = "登录时间")
    private LocalDateTime loginTime;

    /**
     * 登录IP
     */
    @Schema(description = "登录IP")
    private String loginIp;

    /**
     * 是否首次登录
     */
    @Schema(description = "是否首次登录")
    private Boolean isFirstLogin;
}