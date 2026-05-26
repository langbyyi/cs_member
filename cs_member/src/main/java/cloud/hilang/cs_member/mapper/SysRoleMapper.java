package cloud.hilang.cs_member.mapper;

import cloud.hilang.cs_member.entity.SysRole;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 系统角色Mapper接口
 *
 * @author HiLang Cloud Team
 * @since 2025-01-15
 */
@Mapper
public interface SysRoleMapper extends BaseMapper<SysRole> {

    /**
     * 根据角色编码查询角色
     */
    SysRole selectByRoleCode(@Param("roleCode") String roleCode);

    /**
     * 根据用户ID查询角色列表
     */
    List<SysRole> selectRolesByUserId(@Param("userId") Long userId);

    /**
     * 根据角色ID列表查询角色列表
     */
    List<SysRole> selectByRoleIds(@Param("roleIds") List<Long> roleIds);

    /**
     * 根据角色编码查找角色
     *
     * @param roleCode 角色编码
     * @return 角色信息
     */
    SysRole findByRoleCode(@Param("roleCode") String roleCode);

    /**
     * 根据角色ID查找权限列表
     *
     * @param roleId 角色ID
     * @return 权限列表
     */
    List<String> findPermissionsByRoleId(@Param("roleId") Long roleId);
}