package cloud.hilang.cs_member.service;

import cloud.hilang.cs_member.entity.SysUser;
import cloud.hilang.cs_member.mapper.SysUserMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final SysUserMapper sysUserMapper;
    private final PasswordEncoder passwordEncoder;

    public PageInfo<SysUser> getUserList(int pageNum, int pageSize, String name, String phone, Integer status) {
        PageHelper.startPage(pageNum, pageSize);
        QueryWrapper<SysUser> wrapper = new QueryWrapper<>();
        if (name != null && !name.isEmpty()) wrapper.like("name", name);
        if (phone != null && !phone.isEmpty()) wrapper.like("phone", phone);
        if (status != null) wrapper.eq("status", status);
        wrapper.orderByDesc("created_time");
        List<SysUser> users = sysUserMapper.selectList(wrapper);
        return new PageInfo<>(users);
    }

    public SysUser getUserById(Long id) {
        return sysUserMapper.selectById(id);
    }

    public SysUser createUser(SysUser user) {
        user.setPassword(passwordEncoder.encode(user.getPassword() != null ? user.getPassword() : "123456"));
        user.setCreatedTime(LocalDateTime.now());
        user.setUpdatedTime(LocalDateTime.now());
        user.setStatus(1);
        sysUserMapper.insert(user);
        return user;
    }

    public SysUser updateUser(Long id, SysUser user) {
        user.setId(id);
        user.setUpdatedTime(LocalDateTime.now());
        user.setPassword(null);
        sysUserMapper.updateById(user);
        return sysUserMapper.selectById(id);
    }

    public boolean deleteUser(Long id) {
        return sysUserMapper.deleteById(id) > 0;
    }

    public boolean toggleUserStatus(Long id, Integer status) {
        SysUser user = sysUserMapper.selectById(id);
        if (user == null) return false;
        user.setStatus(status);
        user.setUpdatedTime(LocalDateTime.now());
        return sysUserMapper.updateById(user) > 0;
    }

    public boolean resetPassword(Long id, String newPassword) {
        SysUser user = sysUserMapper.selectById(id);
        if (user == null) return false;
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordUpdateTime(LocalDateTime.now());
        user.setUpdatedTime(LocalDateTime.now());
        return sysUserMapper.updateById(user) > 0;
    }
}
