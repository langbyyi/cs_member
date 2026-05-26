package cloud.hilang.cs_member.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 门店实体类
 *
 * @author HiLang Cloud Team
 * @since 2025-01-18
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("store")
public class Store implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 门店ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 门店编号
     */
    @TableField("store_no")
    private String storeNo;

    /**
     * 门店名称
     */
    @TableField("store_name")
    private String storeName;

    /**
     * 门店类型
     */
    @TableField("store_type")
    private String storeType;

    /**
     * 省份
     */
    @TableField("province")
    private String province;

    /**
     * 城市
     */
    @TableField("city")
    private String city;

    /**
     * 区县
     */
    @TableField("district")
    private String district;

    /**
     * 详细地址
     */
    @TableField("address")
    private String address;

    /**
     * 联系电话
     */
    @TableField("contact_phone")
    private String contactPhone;

    /**
     * 联系邮箱
     */
    @TableField("contact_email")
    private String contactEmail;

    /**
     * 营业时间
     */
    @TableField("business_hours")
    private String businessHours;

    /**
     * 店长ID
     */
    @TableField("manager_id")
    private Long managerId;

    /**
     * 店长姓名（非数据库字段，仅用于显示）
     */
    @TableField(exist = false)
    private String managerName;

    /**
     * 门店面积（平方米）
     */
    @TableField("area")
    private BigDecimal area;

    /**
     * 员工数量
     */
    @TableField("employee_count")
    private Integer employeeCount;

    /**
     * 会员数量
     */
    @TableField("member_count")
    private Integer memberCount;

    /**
     * 状态
     */
    @TableField("status")
    private Integer status;

    /**
     * 开业时间
     */
    @TableField("open_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime openTime;
}