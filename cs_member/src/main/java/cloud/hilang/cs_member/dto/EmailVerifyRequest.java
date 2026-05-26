package cloud.hilang.cs_member.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 邮箱验证码验证请求DTO
 *
 * @author HiLang Cloud Team
 * @since 2025-01-17
 */
@Data
@Schema(description = "邮箱验证码验证请求")
public class EmailVerifyRequest {

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

    /**
     * 验证码
     */
    @NotBlank(message = "验证码不能为空")
    @Size(min = 4, max = 10, message = "验证码长度在4-10位之间")
    @Pattern(regexp = "^\\d+$", message = "验证码只能包含数字")
    @Schema(description = "验证码", example = "123456")
    private String verificationCode;
}