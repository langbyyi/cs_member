package cloud.hilang.cs_member.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 用户注册请求DTO
 *
 * @author HiLang Cloud Team
 * @since 2025-01-14
 */
@Data
@Schema(description = "用户注册请求")
public class RegisterRequest {

    /**
     * 手机号
     */
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    @Schema(description = "手机号", example = "13800000000")
    private String phone;

    /**
     * 姓名
     */
    @NotBlank(message = "姓名不能为空")
    @Schema(description = "姓名", example = "张三")
    private String name;

    /**
     * 密码
     */
    @NotBlank(message = "密码不能为空")
    @Schema(description = "密码", example = "admin123")
    private String password;

    /**
     * 邮箱（必填）
     */
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    @Schema(description = "邮箱", example = "zhangsan@example.com")
    private String email;

    /**
     * 邮箱验证码
     */
    @NotBlank(message = "邮箱验证码不能为空")
    @Size(min = 6, max = 6, message = "邮箱验证码为6位数字")
    @Pattern(regexp = "^\\d{6}$", message = "邮箱验证码为6位数字")
    @Schema(description = "邮箱验证码", example = "123456")
    private String emailCode;

    /**
     * 性别（可选，1-男，2-女）
     */
    @Schema(description = "性别", example = "1")
    private Integer gender;
}