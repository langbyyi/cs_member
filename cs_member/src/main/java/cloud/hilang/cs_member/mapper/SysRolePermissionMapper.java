package cloud.hilang.cs_member.mapper;

import cloud.hilang.cs_member.entity.SysRolePermission;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 角色权限关联Mapper接口
 *
 * @author HiLang Cloud Team
 * @since 2025-01-15
 */
@Mapper
public interface SysRolePermissionMapper extends BaseMapper<SysRolePermission> {

    /**
     * 根据角色ID查询权限ID列表
     */
    List<Long> selectPermissionIdsByRoleId(@Param("roleId") Long roleId);

    /**
     * 根据角色ID删除权限关联
     */
    int deleteByRoleId(@Param("roleId") Long roleId);

    /**
     * 批量插入角色权限关联
     */
    int batchInsertRolePermissions(@Param("list") List<SysRolePermission> list);

    /**
     * 根据角色ID列表查询权限ID列表
     */
    List<Long> selectPermissionIdsByRoleIds(@Param("roleIds") List<Long> roleIds);
}