package cloud.hilang.cs_member.service;

import cloud.hilang.cs_member.entity.SysRole;
import cloud.hilang.cs_member.mapper.SysRoleMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoleService {

    private final SysRoleMapper sysRoleMapper;

    public PageInfo<SysRole> getRoleList(int pageNum, int pageSize, String roleName, Integer status) {
        PageHelper.startPage(pageNum, pageSize);
        QueryWrapper<SysRole> wrapper = new QueryWrapper<>();
        if (roleName != null && !roleName.isEmpty()) wrapper.like("role_name", roleName);
        wrapper.orderByAsc("id");
        List<SysRole> roles = sysRoleMapper.selectList(wrapper);
        return new PageInfo<>(roles);
    }

    public List<SysRole> getAllRoles(boolean onlyEnabled) {
        return sysRoleMapper.selectList(null);
    }

    public SysRole createRole(SysRole role) {
        role.setStatus(1);
        role.setCreatedTime(LocalDateTime.now());
        role.setUpdatedTime(LocalDateTime.now());
        sysRoleMapper.insert(role);
        return role;
    }

    public SysRole getRoleDetail(Long id) {
        return sysRoleMapper.selectById(id);
    }

    public SysRole updateRole(Long id, SysRole role) {
        role.setId(id);
        role.setUpdatedTime(LocalDateTime.now());
        sysRoleMapper.updateById(role);
        return role;
    }

    public boolean deleteRole(Long id) {
        return sysRoleMapper.deleteById(id) > 0;
    }

    public boolean toggleRoleStatus(Long id, Integer status) {
        SysRole role = new SysRole();
        role.setId(id);
        role.setStatus(status);
        role.setUpdatedTime(LocalDateTime.now());
        return sysRoleMapper.updateById(role) > 0;
    }
}
