package cloud.hilang.cs_member.dto;

import lombok.Data;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

/**
 * 会员创建请求DTO
 *
 * @author HiLang Cloud Team
 * @since 2025-01-21
 */
@Data
public class MemberCreateRequest {

    /**
     * 会员姓名
     */
    @NotBlank(message = "会员姓名不能为空")
    @Size(min = 2, max = 50, message = "会员姓名长度必须在2-50个字符之间")
    private String name;

    /**
     * 手机号码
     */
    @NotBlank(message = "手机号码不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "请输入正确的11位手机号码")
    private String phone;

    /**
     * 邮箱地址
     */
    @NotBlank(message = "邮箱地址不能为空")
    @Email(message = "请输入正确的邮箱地址")
    @Size(max = 100, message = "邮箱地址长度不能超过100个字符")
    private String email;

    /**
     * 性别：1-男，2-女
     */
    private Integer gender = 1;

    /**
     * 生日
     */
    private LocalDate birthday;

    /**
     * 详细地址
     */
    @Size(max = 200, message = "地址长度不能超过200个字符")
    private String address;

    /**
     * 会员等级
     */
    private Integer memberLevel = 1;

    /**
     * 账户状态：1-启用，2-禁用，3-锁定
     */
    private Integer status = 1;

    /**
     * 注册门店ID
     */
    private Long registerStoreId;
}