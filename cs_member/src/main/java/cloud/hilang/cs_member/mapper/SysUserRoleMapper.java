package cloud.hilang.cs_member.mapper;

import cloud.hilang.cs_member.entity.SysUserRole;
import cloud.hilang.cs_member.entity.SysRole;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户角色关联Mapper接口
 *
 * @author HiLang Cloud Team
 * @since 2025-01-15
 */
@Mapper
public interface SysUserRoleMapper extends BaseMapper<SysUserRole> {

    /**
     * 根据用户ID查询角色ID列表
     */
    List<Long> selectRoleIdsByUserId(@Param("userId") Long userId);

    /**
     * 根据用户ID删除角色关联
     */
    int deleteByUserId(@Param("userId") Long userId);

    /**
     * 根据用户ID和角色ID删除角色关联
     */
    int deleteByUserIdAndRoleId(@Param("userId") Long userId, @Param("roleId") Long roleId);

    /**
     * 插入用户角色关联
     */
    int insertUserRole(@Param("userId") Long userId, @Param("roleId") Long roleId);

    /**
     * 根据用户ID查找角色列表
     *
     * @param userId 用户ID
     * @return 角色列表
     */
    List<SysRole> findRolesByUserId(@Param("userId") Long userId);
    
    /**
     * 根据用户ID查询用户角色关联
     *
     * @param userId 用户ID
     * @return 用户角色关联，如果不存在则返回null
     */
    SysUserRole selectByUserId(@Param("userId") Long userId);
}