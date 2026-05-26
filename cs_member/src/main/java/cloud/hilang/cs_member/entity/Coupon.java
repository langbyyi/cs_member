package cloud.hilang.cs_member.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 优惠券实体类
 *
 * @author HiLang Cloud Team
 * @since 2025-01-19
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("coupon")
public class Coupon {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 优惠券名称
     */
    @TableField("name")
    private String name;

    /**
     * 优惠券描述
     */
    @TableField("description")
    private String description;

    /**
     * 优惠券类型：1-满减券 2-折扣券 3-积分券
     */
    @TableField("coupon_type")
    private Integer couponType;

    /**
     * 优惠金额（满减券、积分券用）
     */
    @TableField("discount_amount")
    private BigDecimal discountAmount;

    /**
     * 折扣率（折扣券用，如0.8表示8折）
     */
    @TableField("discount_percent")
    private BigDecimal discountPercent;

    /**
     * 最低消费金额
     */
    @TableField("min_amount")
    private BigDecimal minAmount;

    /**
     * 最高优惠金额
     */
    @TableField("max_discount_amount")
    private BigDecimal maxDiscountAmount;

    /**
     * 所需积分
     */
    @TableField("required_points")
    private Integer requiredPoints;

    /**
     * 总发行数量
     */
    @TableField("total_quantity")
    private Integer totalQuantity;

    /**
     * 已发放数量
     */
    @TableField("issued_quantity")
    private Integer issuedQuantity;

    /**
     * 已使用数量
     */
    @TableField("used_quantity")
    private Integer usedQuantity;

    /**
     * 有效开始时间
     */
    @TableField("valid_start_time")
    private LocalDateTime validStartTime;

    /**
     * 有效结束时间
     */
    @TableField("valid_end_time")
    private LocalDateTime validEndTime;

    /**
     * 优惠券状态：0-禁用 1-启用 2-已过期
     */
    @TableField("status")
    private Integer status;

    /**
     * 适用门店ID，NULL表示全门店通用
     */
    @TableField("store_id")
    private Long storeId;

    /**
     * 适用会员等级，NULL表示全部会员
     */
    @TableField("member_level")
    private Integer memberLevel;

    /**
     * 创建人ID
     */
    @TableField("creator_id")
    private Long creatorId;

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
     * 备注
     */
    @TableField("remark")
    private String remark;
}