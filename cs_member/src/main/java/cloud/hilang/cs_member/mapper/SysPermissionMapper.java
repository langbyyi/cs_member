package cloud.hilang.cs_member.mapper;

import cloud.hilang.cs_member.entity.SysPermission;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 系统权限Mapper接口
 *
 * @author HiLang Cloud Team
 * @since 2025-01-15
 */
@Mapper
public interface SysPermissionMapper extends BaseMapper<SysPermission> {

    /**
     * 根据权限编码列表查询权限列表
     */
    List<SysPermission> selectByPermissionCodes(List<String> permissionCodes);
}