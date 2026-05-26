package cloud.hilang.cs_member.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 会员等级配置实体类
 *
 * @author HiLang Cloud Team
 * @since 2025-01-18
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("member_grade_config")
public class MemberGradeConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 等级ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 等级编码
     */
    @TableField("grade_code")
    private String gradeCode;

    /**
     * 等级名称
     */
    @TableField("grade_name")
    private String gradeName;

    /**
     * 最低积分要求
     */
    @TableField("min_points")
    private Integer minPoints;

    /**
     * 最高积分（NULL表示无上限）
     */
    @TableField("max_points")
    private Integer maxPoints;

    /**
     * 积分倍率
     */
    @TableField("points_multiplier")
    private BigDecimal pointsMultiplier;

    /**
     * 折扣率（百分比）
     */
    @TableField("discount_rate")
    private BigDecimal discountRate;

    /**
     * 等级颜色
     */
    @TableField("color")
    private String color;

    /**
     * 排序
     */
    @TableField("sort_order")
    private Integer sortOrder;

    /**
     * 状态
     */
    @TableField("status")
    private Integer status;

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