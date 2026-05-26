package cloud.hilang.cs_member.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 会员卡实体类
 *
 * @author HiLang Cloud Team
 * @since 2025-01-17
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("member_card")
public class MemberCard implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 会员ID（主键）
     */
    @TableId(value = "member_id", type = IdType.INPUT)
    private Long memberId;

    /**
     * 会员编号
     */
    @TableField("member_no")
    private String memberNo;

    /**
     * 卡状态：1-正常 2-挂失 3-注销
     */
    @TableField("card_status")
    private Integer cardStatus;

    /**
     * 发卡日期
     */
    @TableField("issue_date")
    private LocalDateTime issueDate;

    /**
     * 到期日期
     */
    @TableField("expire_date")
    private LocalDateTime expireDate;

    /**
     * 创建时间
     */
    @TableField(value = "created_time", fill = FieldFill.INSERT)
    private LocalDateTime createdTime;
}