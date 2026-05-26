package cloud.hilang.cs_member.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 会员实体类
 *
 * @author HiLang Cloud Team
 * @since 2025-01-14
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("member")
public class Member implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 会员ID（主键）
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 手机号码（扩展支持国际号码）
     */
    @TableField("phone")
    private String phone;

    /**
     * 会员编号（按时间顺序自动生成）
     */
    @TableField("member_no")
    private String memberNo;

    /**
     * 会员姓名
     */
    @TableField("name")
    private String name;

    /**
     * 密码（BCrypt加密存储）
     */
    @TableField("password")
    @JsonIgnore
    private String password;

    /**
     * 会员性别（1-男，2-女）
     */
    @TableField("gender")
    private Integer gender;

    /**
     * 会员生日
     */
    @TableField("birthday")
    private LocalDate birthday;

    /**
     * 会员邮箱
     */
    @TableField("email")
    private String email;

    /**
     * 会员地址
     */
    @TableField("address")
    private String address;

    /**
     * 会员头像URL
     */
    @TableField("avatar")
    private String avatar;

    /**
     * 累计消费金额
     */
    @TableField("total_consumption")
    private java.math.BigDecimal totalConsumption;

    /**
     * 累计积分
     */
    @TableField("total_points")
    private Integer totalPoints;

    /**
     * 当前积分
     */
    @TableField("current_points")
    private Integer currentPoints;

    /**
     * 账户余额
     */
    @TableField("balance")
    private java.math.BigDecimal balance;

    /**
     * 注册门店ID
     */
    @TableField("register_store_id")
    private Long registerStoreId;

    /**
     * 状态（0-禁用 1-正常 2-冻结）
     */
    @TableField("status")
    private Integer status;

    /**
     * 最后消费时间
     */
    @TableField("last_consumption_time")
    private LocalDateTime lastConsumptionTime;

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
     * 创建时间
     */
    @TableField(value = "created_time", fill = FieldFill.INSERT)
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    @TableField(value = "updated_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedTime;
}