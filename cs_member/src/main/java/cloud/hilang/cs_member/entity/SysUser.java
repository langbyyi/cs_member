package cloud.hilang.cs_member.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 系统用户实体类
 *
 * @author HiLang Cloud Team
 * @since 2025-01-14
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("sys_user")
public class SysUser implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID（主键）
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 手机号码（登录标识）
     */
    @TableField("phone")
    private String phone;

    /**
     * 密码（BCrypt加密）
     */
    @TableField("password")
    @JsonIgnore
    private String password;

    /**
     * 姓名
     */
    @TableField("name")
    private String name;

    /**
     * 邮箱
     */
    @TableField("email")
    private String email;

    /**
     * 头像URL
     */
    @TableField("avatar")
    private String avatar;

    /**
     * 性别：1-男，2-女
     */
    @TableField("gender")
    private Integer gender;

    /**
     * 生日
     */
    @TableField("birthday")
    private LocalDate birthday;

    /**
     * 所属门店ID
     */
    @TableField("store_id")
    private Long storeId;

    /**
     * 员工编号
     */
    @TableField("employee_no")
    private String employeeNo;

    /**
     * 状态：0-禁用 1-启用
     */
    @TableField("status")
    private Integer status;

    /**
     * 登录次数
     */
    @TableField("login_count")
    private Integer loginCount;

    /**
     * 最后登录时间
     */
    @TableField("last_login_time")
    private LocalDateTime lastLoginTime;

    /**
     * 最后登录IP
     */
    @TableField("last_login_ip")
    private String lastLoginIp;

    /**
     * 密码错误次数
     */
    @TableField("password_error_count")
    private Integer passwordErrorCount;

    /**
     * 密码错误时间
     */
    @TableField("password_error_time")
    private LocalDateTime passwordErrorTime;

    /**
     * 密码更新时间
     */
    @TableField("password_update_time")
    private LocalDateTime passwordUpdateTime;

    /**
     * 是否首次登录：0-否 1-是
     */
    @TableField("is_first_login")
    private Integer isFirstLogin;

    /**
     * 创建时间
     */
    @TableField(value = "created_time", fill = FieldFill.INSERT)
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    @TableField(value = "updated_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedTime;

    /**
     * 用户角色列表（用于关联查询）
     */
    @TableField(exist = false)
    private List<SysRole> roles;

    /**
     * 用户角色代码（用于权限判断）
     */
    @TableField(exist = false)
    private String roleCode;

    /**
     * 用户角色名称（用于显示）
     */
    @TableField(exist = false)
    private String roleName;
}