package cloud.hilang.cs_member.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Email;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 忘记密码请求DTO
 *
 * @author HiLang Cloud Team
 * @since 2025-01-18
 */
@Data
@Schema(description = "忘记密码请求")
public class ForgetPasswordRequest {

    /**
     * 手机号
     */
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    @Schema(description = "手机号", example = "13800000000")
    private String phone;

    /**
     * 邮箱地址
     */
    @NotBlank(message = "邮箱地址不能为空")
    @Email(message = "邮箱格式不正确")
    @Schema(description = "邮箱地址", example = "user@example.com")
    private String email;

    /**
     * 邮箱验证码（发送验证码时可为空，验证验证码时必填）
     */
    @Schema(description = "邮箱验证码", example = "123456")
    private String emailCode;

    /**
     * 用户类型：member-会员 admin-管理员
     * 注意：该字段现在是可选的，因为可以通过接口路径自动确定用户类型
     */
    @Pattern(regexp = "^(member|admin)$", message = "用户类型只能是member或admin")
    @Schema(description = "用户类型", example = "member", allowableValues = {"member", "admin"})
    private String userType;
}