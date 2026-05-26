package cloud.hilang.cs_member.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 积分记录实体类
 *
 * @author HiLang Cloud Team
 * @since 2025-01-17
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("points_record")
public class PointsRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 记录ID（主键）
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 会员ID
     */
    @TableField("member_id")
    private Long memberId;

    /**
     * 积分变动类型：earn-获得 use-使用 expire-过期 refund-退款调整
     */
    @TableField("change_type")
    private String changeType;

    /**
     * 积分变动数量（正数为获得，负数为使用）
     */
    @TableField("points_change")
    private Integer pointsChange;

    /**
     * 变动前积分
     */
    @TableField("points_before")
    private Integer pointsBefore;

    /**
     * 变动后积分
     */
    @TableField("points_after")
    private Integer pointsAfter;

    /**
     * 变动原因
     */
    @TableField("change_reason")
    private String changeReason;

    /**
     * 关联类型：consumption-消费 coupon-优惠券 manual-手动调整
     */
    @TableField("reference_type")
    private String referenceType;

    /**
     * 关联ID
     */
    @TableField("reference_id")
    private Long referenceId;

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
     * 操作员姓名
     */
    @TableField("operator_name")
    private String operatorName;

    /**
     * 积分过期时间
     */
    @TableField("expire_time")
    private LocalDateTime expireTime;

    /**
     * 记录时间
     */
    @TableField(value = "record_time", fill = FieldFill.INSERT)
    private LocalDateTime recordTime;

    // 以下字段为查询时的临时字段，不映射到数据库
    /**
     * 会员编号（查询时关联member表获取）
     */
    @TableField(exist = false)
    private String memberNo;

    /**
     * 会员姓名（查询时关联member表获取）
     */
    @TableField(exist = false)
    private String memberName;

    /**
     * 积分类型（1-获得 2-使用）
     */
    @TableField(exist = false)
    private Integer pointsType;

    /**
     * 积分类型名称
     */
    @TableField(exist = false)
    private String pointsTypeName;

    /**
     * 积分数量（绝对值）
     */
    @TableField(exist = false)
    private Integer points;

    /**
     * 变动前积分
     */
    @TableField(exist = false)
    private Integer beforePoints;

    /**
     * 变动后积分
     */
    @TableField(exist = false)
    private Integer afterPoints;

    /**
     * 描述
     */
    @TableField(exist = false)
    private String description;

    /**
     * 创建时间（别名）
     */
    @TableField(exist = false)
    private LocalDateTime createTime;
}