package cloud.hilang.cs_member.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 重置密码请求DTO
 *
 * @author HiLang Cloud Team
 * @since 2025-01-18
 */
@Data
@Schema(description = "重置密码请求")
public class ResetPasswordRequest {

    /**
     * 重置密码令牌（由验证邮箱和手机号后生成）
     */
    @NotBlank(message = "重置令牌不能为空")
    @Schema(description = "重置密码令牌", example = "reset_token_abc123")
    private String resetToken;

    /**
     * 新密码
     */
    @NotBlank(message = "新密码不能为空")
    @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*\\d)[a-zA-Z\\d@$!%*?&]{8,20}$",
             message = "密码必须包含字母和数字，长度8-20位")
    @Schema(description = "新密码", example = "NewPass123")
    private String newPassword;

    /**
     * 确认新密码
     */
    @NotBlank(message = "确认密码不能为空")
    @Schema(description = "确认新密码", example = "NewPass123")
    private String confirmPassword;
}