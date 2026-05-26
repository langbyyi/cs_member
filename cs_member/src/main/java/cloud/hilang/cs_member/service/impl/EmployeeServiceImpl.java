package cloud.hilang.cs_member.service.impl;

import cloud.hilang.cs_member.dto.CreateEmployeeRequest;
import cloud.hilang.cs_member.dto.EmployeeDTO;
import cloud.hilang.cs_member.dto.UpdateEmployeeRequest;
import cloud.hilang.cs_member.entity.SysRole;
import cloud.hilang.cs_member.entity.SysUser;
import cloud.hilang.cs_member.entity.SysUserRole;
import cloud.hilang.cs_member.enums.UserRole;
import cloud.hilang.cs_member.exception.BusinessException;
import cloud.hilang.cs_member.mapper.SysRoleMapper;
import cloud.hilang.cs_member.mapper.SysUserMapper;
import cloud.hilang.cs_member.mapper.SysUserRoleMapper;
import cloud.hilang.cs_member.mapper.StoreMapper;
import cloud.hilang.cs_member.service.EmployeeService;
import cloud.hilang.cs_member.service.StoreService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 员工管理服务实现
 *
 * @author HiLang Cloud Team
 * @since 2025-12-03
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {

    private final SysUserMapper sysUserMapper;
    private final SysRoleMapper sysRoleMapper;
    private final SysUserRoleMapper sysUserRoleMapper;
    private final StoreMapper storeMapper;
    private final StoreService storeService;
    private final PasswordEncoder passwordEncoder;

    private static final String DEFAULT_PASSWORD = "admin123";

    @Override
    public PageInfo<EmployeeDTO> getEmployeeList(int pageNum, int pageSize,
                                                  String name, String phone,
                                                  Long storeId, String roleName, Integer status,
                                                  String sortProp, String sortOrder) {
        log.info("查询员工列表，pageNum={}, pageSize={}, name={}, phone={}, storeId={}, roleName={}, status={}, sortProp={}, sortOrder={}",
                pageNum, pageSize, name, phone, storeId, roleName, status, sortProp, sortOrder);

        // 处理排序
        String orderBy = null;
        if (sortProp != null && !sortProp.isEmpty()) {
            String column = switch (sortProp) {
                case "createdTime" -> "u.created_time";
                case "lastLoginTime" -> "u.last_login_time";
                case "name" -> "u.name";
                case "loginCount" -> "u.login_count";
                default -> "u.created_time";
            };
            
            String order = "ascending".equals(sortOrder) ? "ASC" : "DESC";
            orderBy = column + " " + order;
        }

        // 使用 MyBatis-Plus 分页
        Page<SysUser> page = new Page<>(pageNum, pageSize);
        Page<SysUser> resultPage = (Page<SysUser>) sysUserMapper.selectEmployeesPage(page, name, phone, storeId, roleName, status, orderBy);

        // 转换为 DTO
        List<EmployeeDTO> employeeDTOList = resultPage.getRecords().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        // 构建 PageInfo
        PageInfo<EmployeeDTO> pageInfo = new PageInfo<>();
        pageInfo.setList(employeeDTOList);
        pageInfo.setTotal(resultPage.getTotal());
        pageInfo.setPageNum((int) resultPage.getCurrent());
        pageInfo.setPageSize((int) resultPage.getSize());
        pageInfo.setPages((int) resultPage.getPages());

        return pageInfo;
    }

    @Override
    public EmployeeDTO getEmployeeById(Long id) {
        log.info("查询员工详情，id={}", id);

        SysUser employee = sysUserMapper.selectEmployeeById(id);
        if (employee == null) {
            throw new BusinessException("员工不存在");
        }

        return convertToDTO(employee);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createEmployee(CreateEmployeeRequest request) {
        log.info("创建员工，phone={}, name={}, roleCode={}", 
                request.getPhone(), request.getName(), request.getRoleCode());

        // 验证角色代码
        if (!UserRole.STORE_ADMIN.getCode().equals(request.getRoleCode()) &&
            !UserRole.CLERK.getCode().equals(request.getRoleCode())) {
            throw new BusinessException("只能创建店长或店员角色");
        }
        
        // 验证门店ID
        if (request.getStoreId() == null) {
            throw new BusinessException("员工必须绑定门店");
        }

        // 检查手机号是否已存在
        SysUser existingUser = sysUserMapper.findByPhone(request.getPhone());
        if (existingUser != null) {
            throw new BusinessException("手机号已被使用");
        }

        // 检查邮箱是否已存在
        SysUser existingEmailUser = sysUserMapper.selectByEmail(request.getEmail());
        if (existingEmailUser != null) {
            throw new BusinessException("邮箱已被使用");
        }

        // 查询角色
        SysRole role = sysRoleMapper.findByRoleCode(request.getRoleCode());
        if (role == null) {
            throw new BusinessException("角色不存在");
        }

        // 创建用户
        SysUser user = new SysUser();
        user.setPhone(request.getPhone());
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setGender(request.getGender());
        user.setBirthday(request.getBirthday());
        user.setStoreId(request.getStoreId());
        user.setStatus(1); // 默认启用
        user.setIsFirstLogin(1); // 首次登录
        user.setCreatedTime(LocalDateTime.now());
        
        // 生成员工编号
        user.setEmployeeNo(generateEmployeeNo(request.getRoleCode()));
        
        // 设置密码
        String password = request.getPassword() != null ? request.getPassword() : DEFAULT_PASSWORD;
        user.setPassword(passwordEncoder.encode(password));

        // 插入用户
        sysUserMapper.insert(user);

        // 关联角色
        sysUserRoleMapper.insertUserRole(user.getId(), role.getId());
        
        // 如果创建的是店长,需要处理店长分配逻辑
        if (UserRole.STORE_ADMIN.getCode().equals(request.getRoleCode())) {
            handleNewManagerAssignment(user.getId(), user.getName(), request.getStoreId());
        }

        // 更新门店员工数统计
        try {
            storeService.updateStoreStatistics(request.getStoreId());
            log.debug("已更新门店{}的员工数统计", request.getStoreId());
        } catch (Exception e) {
            log.warn("更新门店员工数统计失败，门店ID：{}", request.getStoreId(), e);
        }

        log.info("员工创建成功，id={}, employeeNo={}", user.getId(), user.getEmployeeNo());
        return user.getId();
    }
    
    /**
     * 生成员工编号
     * 格式: ADM + 时间戳(店长) 或 EMP + 时间戳(店员)
     */
    private String generateEmployeeNo(String roleCode) {
        String prefix = UserRole.STORE_ADMIN.getCode().equals(roleCode) ? "ADM" : "EMP";
        return prefix + System.currentTimeMillis();
    }
    
    /**
     * 处理新店长分配: 将原店长降级为店员,并更新store表
     */
    private void handleNewManagerAssignment(Long newManagerId, String newManagerName, Long storeId) {
        // 查询该门店所有员工,找出当前店长
        List<SysUser> storeEmployees = sysUserMapper.selectStoreEmployees(storeId);
        
        SysUser currentManager = null;
        for (SysUser emp : storeEmployees) {
            // 跳过新创建的店长
            if (emp.getId().equals(newManagerId)) {
                continue;
            }
            
            // 查询该员工的角色
            SysUserRole userRole = sysUserRoleMapper.selectByUserId(emp.getId());
            if (userRole != null) {
                SysRole role = sysRoleMapper.selectById(userRole.getRoleId());
                if (role != null && UserRole.STORE_ADMIN.getCode().equals(role.getRoleCode())) {
                    currentManager = emp;
                    break;
                }
            }
        }
        
        // 如果存在其他店长,将其降级为店员
        if (currentManager != null) {
            SysRole clerkRole = sysRoleMapper.findByRoleCode(UserRole.CLERK.getCode());
            sysUserRoleMapper.deleteByUserId(currentManager.getId());
            sysUserRoleMapper.insertUserRole(currentManager.getId(), clerkRole.getId());
            log.info("原店长降级为店员: userId={}, storeId={}", currentManager.getId(), storeId);
        }
        
        // 更新门店的店长ID
        storeMapper.updateManager(storeId, newManagerId);
        log.info("门店店长已更新: storeId={}, newManagerId={}", storeId, newManagerId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateEmployee(Long id, UpdateEmployeeRequest request) {
        log.info("更新员工信息，id={}", id);

        // 检查员工是否存在
        SysUser employee = sysUserMapper.selectEmployeeById(id);
        if (employee == null) {
            throw new BusinessException("员工不存在");
        }

        // 如果要更新邮箱，检查邮箱是否已被其他用户使用
        if (request.getEmail() != null && !request.getEmail().isEmpty()) {
            SysUser existingEmailUser = sysUserMapper.selectByEmail(request.getEmail());
            if (existingEmailUser != null && !existingEmailUser.getId().equals(id)) {
                throw new BusinessException("邮箱已被其他用户使用");
            }
        }

        // 检查是否为店长，店长不允许修改所属门店
        if (request.getStoreId() != null && !request.getStoreId().equals(employee.getStoreId())) {
            // 获取当前员工的角色
            SysUserRole currentUserRole = sysUserRoleMapper.selectByUserId(id);
            if (currentUserRole != null) {
                SysRole currentRole = sysRoleMapper.selectById(currentUserRole.getRoleId());
                if (currentRole != null && UserRole.STORE_ADMIN.getCode().equals(currentRole.getRoleCode())) {
                    throw new BusinessException("店长不允许修改所属门店");
                }
            }
        }

        // 记录原始门店ID，用于后续更新统计数据
        Long originalStoreId = employee.getStoreId();
        Long newStoreId = request.getStoreId();

        // 更新基本信息
        SysUser updateUser = new SysUser();
        updateUser.setId(id);
        updateUser.setName(request.getName());
        updateUser.setEmail(request.getEmail());
        updateUser.setAvatar(request.getAvatar());
        updateUser.setGender(request.getGender());
        updateUser.setBirthday(request.getBirthday());
        updateUser.setStoreId(newStoreId);
        sysUserMapper.updateById(updateUser);

        // 处理角色更新
        if (request.getRoleCode() != null) {
            handleRoleUpdate(id, employee, request.getRoleCode(), newStoreId);
        }

        // 更新门店员工数统计（如果门店发生变化）
        try {
            if (originalStoreId != null && !originalStoreId.equals(newStoreId)) {
                // 原门店员工数减少
                storeService.updateStoreStatistics(originalStoreId);
                log.debug("已更新原门店{}的员工数统计", originalStoreId);

                // 新门店员工数增加
                if (newStoreId != null) {
                    storeService.updateStoreStatistics(newStoreId);
                    log.debug("已更新新门店{}的员工数统计", newStoreId);
                }
            }
        } catch (Exception e) {
            log.warn("更新门店员工数统计失败，原门店ID：{}，新门店ID：{}", originalStoreId, newStoreId, e);
        }

        log.info("员工信息更新成功，id={}", id);
    }
    
    /**
     * 处理角色更新逻辑
     */
    private void handleRoleUpdate(Long userId, SysUser employee, String newRoleCode, Long storeId) {
        // 1. 验证角色
        if (!UserRole.STORE_ADMIN.getCode().equals(newRoleCode) &&
            !UserRole.CLERK.getCode().equals(newRoleCode)) {
            throw new BusinessException("只能设置店长或店员角色");
        }
        
        // 2. 查询新角色
        SysRole newRole = sysRoleMapper.findByRoleCode(newRoleCode);
        if (newRole == null) {
            throw new BusinessException("角色不存在");
        }
        
        // 3. 获取当前角色
        SysUserRole currentUserRole = sysUserRoleMapper.selectByUserId(userId);
        SysRole currentRole = currentUserRole != null ? 
            sysRoleMapper.selectById(currentUserRole.getRoleId()) : null;
        String currentRoleCode = currentRole != null ? currentRole.getRoleCode() : null;
        
        // 如果角色没有变化,直接返回
        if (newRoleCode.equals(currentRoleCode)) {
            return;
        }
        
        // 4. 业务规则验证
        if (UserRole.STORE_ADMIN.getCode().equals(currentRoleCode) && 
            UserRole.CLERK.getCode().equals(newRoleCode)) {
            // 不允许店长降级为店员
            throw new BusinessException("不允许将店长降级为店员");
        }
        
        // 5. 处理店员提升为店长
        if (UserRole.CLERK.getCode().equals(currentRoleCode) && 
            UserRole.STORE_ADMIN.getCode().equals(newRoleCode)) {
            handlePromoteToManager(userId, storeId);
        }
        
        // 6. 更新用户角色关联
        sysUserRoleMapper.deleteByUserId(userId);
        sysUserRoleMapper.insertUserRole(userId, newRole.getId());
        
        log.info("角色更新成功: userId={}, {} -> {}", userId, currentRoleCode, newRoleCode);
    }
    
    /**
     * 提升为店长: 更新门店的manager_id,原店长自动降级为店员
     */
    private void handlePromoteToManager(Long newManagerId, Long storeId) {
        if (storeId == null) {
            throw new BusinessException("店长必须绑定门店");
        }
        
        // 查询该门店所有员工,找出当前店长(manager_id指向的用户)
        List<SysUser> storeEmployees = sysUserMapper.selectStoreEmployees(storeId);
        
        // 通过store表查询当前店长ID
        // 注意: 这里需要先查询store表获取当前的manager_id
        // 由于用户已经修改了StoreMapper,我们直接使用selectStoreEmployees
        // 然后通过角色判断谁是店长
        SysUser currentManager = null;
        for (SysUser emp : storeEmployees) {
            // 查询该员工的角色
            SysUserRole userRole = sysUserRoleMapper.selectByUserId(emp.getId());
            if (userRole != null) {
                SysRole role = sysRoleMapper.selectById(userRole.getRoleId());
                if (role != null && UserRole.STORE_ADMIN.getCode().equals(role.getRoleCode())) {
                    currentManager = emp;
                    break;
                }
            }
        }
        
        // 如果存在其他店长且不是当前用户,将其降级为店员
        if (currentManager != null && !currentManager.getId().equals(newManagerId)) {
            SysRole clerkRole = sysRoleMapper.findByRoleCode(UserRole.CLERK.getCode());
            sysUserRoleMapper.deleteByUserId(currentManager.getId());
            sysUserRoleMapper.insertUserRole(currentManager.getId(), clerkRole.getId());
            log.info("原店长降级为店员: userId={}, storeId={}", currentManager.getId(), storeId);
        }
        
        // 更新门店的店长ID
        storeMapper.updateManager(storeId, newManagerId);
        log.info("门店店长已更新: storeId={}, newManagerId={}", storeId, newManagerId);
    }
    
    /**
     * 从店长降级: 已废弃,不再允许此操作
     */
    @Deprecated
    private void handleDemoteFromManager(Long storeId) {
        throw new BusinessException("不允许将店长降级为店员");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteEmployee(Long id) {
        log.info("删除员工（物理删除），id={}", id);

        // 检查员工是否存在
        SysUser employee = sysUserMapper.selectEmployeeById(id);
        if (employee == null) {
            throw new BusinessException("员工不存在");
        }

        // 记录门店ID，用于后续更新统计
        Long storeId = employee.getStoreId();

        try {
            // 1. 删除用户角色关联
            sysUserRoleMapper.deleteByUserId(id);

            // 2. 物理删除用户记录
            sysUserMapper.deleteById(id);

            log.info("员工已物理删除，id={}", id);

        } catch (Exception e) {
            log.error("员工删除失败，id={}", id, e);
            throw new BusinessException("删除员工失败");
        }

        // 更新门店员工数统计
        try {
            if (storeId != null) {
                storeService.updateStoreStatistics(storeId);
                log.debug("已更新门店{}的员工数统计（员工删除）", storeId);
            }
        } catch (Exception e) {
            log.warn("更新门店员工数统计失败，门店ID：{}", storeId, e);
        }
    }

    @Override
    public void updateEmployeeStatus(Long id, Integer status) {
        log.info("更新员工状态，id={}, status={}", id, status);

        // 检查员工是否存在
        SysUser employee = sysUserMapper.selectEmployeeById(id);
        if (employee == null) {
            throw new BusinessException("员工不存在");
        }

        // 更新状态
        sysUserMapper.updateStatus(id, status);

        // 更新门店员工数统计（状态变化影响统计）
        try {
            if (employee.getStoreId() != null) {
                storeService.updateStoreStatistics(employee.getStoreId());
                log.debug("已更新门店{}的员工数统计（员工状态变化）", employee.getStoreId());
            }
        } catch (Exception e) {
            log.warn("更新门店员工数统计失败，门店ID：{}", employee.getStoreId(), e);
        }

        log.info("员工状态更新成功，id={}, status={}", id, status);
    }

    @Override
    public void resetPassword(Long id, String newPassword) {
        log.info("重置员工密码，id={}", id);

        // 检查员工是否存在
        SysUser employee = sysUserMapper.selectEmployeeById(id);
        if (employee == null) {
            throw new BusinessException("员工不存在");
        }

        // 更新密码
        SysUser updateUser = new SysUser();
        updateUser.setId(id);
        updateUser.setPassword(passwordEncoder.encode(newPassword));
        updateUser.setIsFirstLogin(1); // 重置后需要首次登录

        sysUserMapper.updateById(updateUser);
        log.info("员工密码重置成功，id={}", id);
    }

    /**
     * 转换为 DTO
     */
    private EmployeeDTO convertToDTO(SysUser user) {
        return EmployeeDTO.builder()
                .id(user.getId())
                .phone(user.getPhone())
                .name(user.getName())
                .email(user.getEmail())
                .avatar(user.getAvatar())
                .gender(user.getGender())
                .birthday(user.getBirthday())
                .storeId(user.getStoreId())
                .employeeNo(user.getEmployeeNo())
                .status(user.getStatus())
                .roleCode(user.getRoleCode())
                .roleName(user.getRoleName())
                .loginCount(user.getLoginCount())
                .lastLoginTime(user.getLastLoginTime())
                .lastLoginIp(user.getLastLoginIp())
                .createdTime(user.getCreatedTime())
                .updatedTime(user.getUpdatedTime())
                .build();
    }
}
