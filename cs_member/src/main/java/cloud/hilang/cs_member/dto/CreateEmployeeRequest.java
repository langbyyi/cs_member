package cloud.hilang.cs_member.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * 创建员工请求DTO
 *
 * @author HiLang Cloud Team
 * @since 2025-12-03
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateEmployeeRequest {
    
    /**
     * 手机号码
     */
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;
    
    /**
     * 姓名
     */
    @NotBlank(message = "姓名不能为空")
    private String name;
    
    /**
     * 邮箱
     */
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;
    
    /**
     * 性别：1-男，2-女
     */
    private Integer gender;
    
    /**
     * 生日
     */
    private java.time.LocalDate birthday;
    
    /**
     * 所属门店ID
     */
    private Long storeId;
    
    /**
     * 角色代码：STORE_ADMIN-店长，CLERK-店员
     */
    @NotBlank(message = "角色不能为空")
    @Pattern(regexp = "^(STORE_ADMIN|CLERK)$", message = "角色只能是STORE_ADMIN或CLERK")
    private String roleCode;
    
    /**
     * 初始密码(可选,默认为123456)
     */
    private String password;
}
