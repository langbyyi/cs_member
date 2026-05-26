package cloud.hilang.cs_member.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 登录请求DTO
 *
 * @author HiLang Cloud Team
 * @since 2025-01-14
 */
@Data
@Schema(description = "登录请求")
public class LoginRequest {

    /**
     * 手机号（统一登录标识）
     */
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    @Schema(description = "手机号", example = "13800000000")
    private String phone;

    /**
     * 登录密码
     */
    @NotBlank(message = "密码不能为空")
    @Schema(description = "密码", example = "123456")
    private String password;
}