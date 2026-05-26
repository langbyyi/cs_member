package cloud.hilang.cs_member.dto;

import lombok.Builder;
import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 重置密码令牌响应DTO
 *
 * @author HiLang Cloud Team
 * @since 2025-01-18
 */
@Data
@Builder
@Schema(description = "重置密码令牌响应")
public class ResetPasswordTokenResponse {

    /**
     * 重置密码令牌
     */
    @Schema(description = "重置密码令牌", example = "reset_token_abc123")
    private String resetToken;

    /**
     * 令牌过期时间（秒）
     */
    @Schema(description = "令牌有效期（秒）", example = "600")
    private Long expiresIn;

    /**
     * 用户信息
     */
    @Schema(description = "用户信息")
    private UserInfo userInfo;

    @Data
    @Builder
    @Schema(description = "用户信息")
    public static class UserInfo {
        @Schema(description = "用户ID", example = "1001")
        private Long userId;

        @Schema(description = "用户名", example = "张三")
        private String name;

        @Schema(description = "手机号", example = "13800000000")
        private String phone;

        @Schema(description = "邮箱", example = "user@example.com")
        private String email;

        @Schema(description = "用户类型", example = "member")
        private String userType;
    }
}