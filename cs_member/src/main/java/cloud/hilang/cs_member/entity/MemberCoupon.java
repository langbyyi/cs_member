package cloud.hilang.cs_member.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("member_coupon")
public class MemberCoupon implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("member_id")
    private Long memberId;

    @TableField("coupon_id")
    private Long couponId;

    @TableField("coupon_code")
    private String couponCode;

    @TableField("receive_time")
    private LocalDateTime receiveTime;

    @TableField("use_time")
    private LocalDateTime useTime;

    @TableField("expire_time")
    private LocalDateTime expireTime;

    @TableField("consumption_id")
    private Long consumptionId;

    @TableField("created_time")
    private LocalDateTime createdTime;

    // 非数据库字段 - 关联查询用
    @TableField(exist = false)
    private String couponName;

    @TableField(exist = false)
    private String couponType;

    @TableField(exist = false)
    private java.math.BigDecimal discountValue;

    @TableField(exist = false)
    private java.math.BigDecimal minConsumption;

    @TableField(exist = false)
    private String status;
}
