package cloud.hilang.cs_member.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 系统权限实体类
 *
 * @author HiLang Cloud Team
 * @since 2025-01-15
 */
@Data
@TableName("sys_permission")
public class SysPermission {

    /**
     * 权限ID（主键）
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 权限编码（包含层级和类型信息，如system:user:add）
     */
    private String permissionCode;

    /**
     * 权限名称
     */
    private String permissionName;

    /**
     * 创建时间
     */
    private Date createdTime;

    /**
     * 更新时间
     */
    private Date updatedTime;
}