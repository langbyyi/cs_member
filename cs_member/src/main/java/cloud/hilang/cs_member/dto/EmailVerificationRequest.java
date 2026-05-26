package cloud.hilang.cs_member.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 邮箱验证码请求DTO
 *
 * @author HiLang Cloud Team
 * @since 2025-01-17
 */
@Data
@Schema(description = "邮箱验证码请求")
public class EmailVerificationRequest {

    /**
     * 用户类型：member-会员 admin-管理员
     */
    @NotBlank(message = "用户类型不能为空")
    @Schema(description = "用户类型", example = "member", allowableValues = {"member", "admin"})
    private String userType;

    
    /**
     * 邮箱地址
     */
    @NotBlank(message = "邮箱地址不能为空")
    @Schema(description = "邮箱地址", example = "user@example.com")
    private String email;

    /**
     * 验证类型：register-注册 reset-重置密码 bind-绑定邮箱
     */
    @NotBlank(message = "验证类型不能为空")
    @Schema(description = "验证类型", example = "register", allowableValues = {"register", "reset", "bind"})
    private String verificationType;
}