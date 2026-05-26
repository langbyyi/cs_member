package cloud.hilang.cs_member.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 消费记录实体类
 *
 * @author HiLang Cloud Team
 * @since 2025-01-18
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("consumption_record")
public class ConsumptionRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 记录ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 消费单号
     */
    @TableField("consumption_no")
    private String consumptionNo;

    /**
     * 会员ID
     */
    @TableField("member_id")
    private Long memberId;

    /**
     * 门店ID
     */
    @TableField("store_id")
    private Long storeId;

    /**
     * 操作员ID
     */
    @TableField("operator_id")
    private Long operatorId;

    /**
     * 消费类型
     */
    @TableField("consumption_type")
    private String consumptionType;

    /**
     * 总金额
     */
    @TableField("total_amount")
    private BigDecimal totalAmount;

    /**
     * 折扣金额
     */
    @TableField("discount_amount")
    private BigDecimal discountAmount;

    /**
     * 获得积分
     */
    @TableField("points_earned")
    private Integer pointsEarned;

    /**
     * 使用积分
     */
    @TableField("points_used")
    private Integer pointsUsed;

    /**
     * 使用的优惠券ID
     */
    @TableField("coupon_id")
    private Long couponId;

    /**
     * 支付方式
     */
    @TableField("payment_method")
    private String paymentMethod;

    /**
     * 消费时间
     */
    @TableField("consumption_time")
    private LocalDateTime consumptionTime;

    /**
     * 状态
     */
    @TableField("status")
    private String status;

    /**
     * 取消原因
     */
    @TableField("cancel_reason")
    private String cancelReason;

    /**
     * 退款原因
     */
    @TableField("refund_reason")
    private String refundReason;

    /**
     * 备注
     */
    @TableField("remark")
    private String remark;

    /**
     * 创建时间
     */
    @TableField(value = "created_time", fill = FieldFill.INSERT)
    private LocalDateTime createdTime;
}