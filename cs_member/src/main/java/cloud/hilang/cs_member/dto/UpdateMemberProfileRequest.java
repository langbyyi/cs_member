package cloud.hilang.cs_member.dto;

import lombok.Data;

/**
 * 更新会员个人信息请求DTO
 *
 * @author HiLang Cloud Team
 * @since 2025-01-19
 */
@Data
public class UpdateMemberProfileRequest {

    /**
     * 会员姓名
     */
    private String name;

    /**
     * 性别：1-男 2-女
     */
    private Integer gender;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 生日
     */
    private String birthday;

    /**
     * 地址
     */
    private String address;

    /**
     * 头像URL
     */
    private String avatar;
}