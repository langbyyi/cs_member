package cloud.hilang.cs_member.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 更新员工请求DTO
 *
 * @author HiLang Cloud Team
 * @since 2025-12-03
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateEmployeeRequest {
    
    /**
     * 姓名
     */
    private String name;
    
    /**
     * 邮箱
     */
    private String email;
    
    /**
     * 头像URL
     */
    private String avatar;
    
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
     * 角色代码: STORE_ADMIN-店长, CLERK-店员
     */
    private String roleCode;
}
