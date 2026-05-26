package cloud.hilang.cs_member.service;

import cloud.hilang.cs_member.dto.EmployeeDTO;
import cloud.hilang.cs_member.entity.Store;
import cloud.hilang.cs_member.entity.SysUser;
import cloud.hilang.cs_member.mapper.StoreMapper;
import cloud.hilang.cs_member.mapper.SysRoleMapper;
import cloud.hilang.cs_member.mapper.SysUserMapper;
import cloud.hilang.cs_member.mapper.SysUserRoleMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 门店管理服务类
 *
 * @author HiLang Cloud Team
 * @since 2025-01-21
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StoreService {

    private final StoreMapper storeMapper;
    private final SysUserMapper sysUserMapper;
    private final SysUserRoleMapper sysUserRoleMapper;
    private final SysRoleMapper sysRoleMapper;

    /**
     * 获取所有营业中的门店
     *
     * @return 门店列表
     */
    public List<Store> getOpenStores() {
        log.debug("获取营业中的门店列表");
        List<Store> stores = storeMapper.selectOpenStores();

        // 填充店长姓名
        fillManagerNames(stores);
        return stores;
    }

    /**
     * 获取所有门店（分页）
     *
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param status 门店状态（可选）
     * @param keyword 搜索关键字（可选）
     * @return 分页结果
     */
    public com.github.pagehelper.PageInfo<Store> getAllStores(int pageNum, int pageSize, Integer status, String keyword) {
        log.debug("获取门店列表，pageNum={}, pageSize={}, status={}, keyword={}", pageNum, pageSize, status, keyword);

        com.github.pagehelper.PageHelper.startPage(pageNum, pageSize);
        
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Store> wrapper = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        
        // 状态筛选
        if (status != null) {
            wrapper.eq(Store::getStatus, status);
        }
        
        // 关键字搜索
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.and(w -> w
                .like(Store::getStoreName, keyword)
                .or()
                .like(Store::getStoreNo, keyword)
                .or()
                .like(Store::getAddress, keyword)
                .or()
                .like(Store::getContactPhone, keyword)
            );
        }
        
        // 按创建时间倒序
        wrapper.orderByDesc(Store::getId);

        List<Store> list = storeMapper.selectList(wrapper);

        // 填充店长姓名
        fillManagerNames(list);

        return new com.github.pagehelper.PageInfo<>(list);
    }

    /**
     * 更新门店状态
     *
     * @param id 门店ID
     * @param status 状态（0-停业，1-营业）
     * @return 是否更新成功
     */
    public boolean updateStoreStatus(Long id, Integer status) {
        log.info("更新门店状态，ID：{}，status：{}", id, status);

        try {
            int result = storeMapper.updateStatus(id, status);
            return result > 0;
        } catch (Exception e) {
            log.error("更新门店状态失败，ID：{}，status：{}", id, status, e);
            return false;
        }
    }

    /**
     * 创建新门店
     *
     * @param store 门店信息
     * @return 创建后的门店信息
     */
    public Store createStore(Store store) {
        log.debug("创建门店，门店名称：{}", store.getStoreName());

        // 生成门店编号
        if (store.getStoreNo() == null || store.getStoreNo().isEmpty()) {
            store.setStoreNo(generateStoreNo());
        }

        // 设置默认值
        if (store.getStatus() == null) {
            store.setStatus(1); // 默认营业状态
        }
        if (store.getStoreType() == null || store.getStoreType().isEmpty()) {
            store.setStoreType("direct"); // 默认直营
        }
        if (store.getEmployeeCount() == null) {
            store.setEmployeeCount(0);
        }
        if (store.getMemberCount() == null) {
            store.setMemberCount(0);
        }

        storeMapper.insert(store);
        return store;
    }

    /**
     * 根据ID获取门店信息
     *
     * @param id 门店ID
     * @return 门店信息
     */
    public Store getStoreById(Long id) {
        log.debug("获取门店信息，ID：{}", id);
        Store store = storeMapper.selectById(id);
        if (store != null) {
            fillManagerNames(List.of(store));
        }
        return store;
    }

    /**
     * 更新门店信息
     *
     * @param store 门店信息
     * @return 更新后的门店信息
     */
    public Store updateStore(Store store) {
        log.debug("更新门店信息，ID：{}", store.getId());

        storeMapper.updateById(store);
        return store;
    }

    /**
     * 完整更新门店信息（包含店长变更处理）
     *
     * @param storeId 门店ID
     * @param storeInfo 门店信息
     * @return 更新后的门店信息
     */
    @Transactional
    public Store updateStoreInfo(Long storeId, Store storeInfo) {
        log.info("更新门店信息，门店ID：{}，门店名称：{}", storeId, storeInfo.getStoreName());

        try {
            // 1. 获取当前门店信息
            Store currentStore = storeMapper.selectById(storeId);
            if (currentStore == null) {
                throw new RuntimeException("门店不存在，ID：" + storeId);
            }

            // 2. 设置门店ID
            storeInfo.setId(storeId);

            // 3. 检查店长是否发生变化
            Long currentManagerId = currentStore.getManagerId();
            Long newManagerId = storeInfo.getManagerId();

            boolean managerChanged = false;
            if (currentManagerId == null && newManagerId != null) {
                managerChanged = true;
            } else if (currentManagerId != null && !currentManagerId.equals(newManagerId)) {
                managerChanged = true;
            }

            // 4. 如果店长发生变化，处理店长角色切换
            if (managerChanged) {
                log.info("门店店长发生变化，原店长ID：{}，新店长ID：{}", currentManagerId, newManagerId);
                boolean managerUpdateResult = updateStoreManager(storeId, newManagerId);
                if (!managerUpdateResult) {
                    throw new RuntimeException("店长更新失败");
                }
                // 店长更新后，从storeInfo中移除managerId，避免重复更新
                storeInfo.setManagerId(null);
            }

            // 5. 更新门店基本信息（除了managerId，因为店长更新已经处理过了）
            if (hasUpdatableFields(storeInfo)) {
                int updateResult = storeMapper.updateById(storeInfo);
                if (updateResult <= 0) {
                    throw new RuntimeException("门店信息更新失败");
                }
            }

            // 6. 返回更新后的门店信息
            Store updatedStore = storeMapper.selectById(storeId);
            if (updatedStore != null) {
                fillManagerNames(List.of(updatedStore));
            }

            log.info("门店信息更新成功，门店ID：{}", storeId);
            return updatedStore;

        } catch (Exception e) {
            log.error("更新门店信息失败，门店ID：{}", storeId, e);
            throw e;
        }
    }

    /**
     * 检查门店信息是否包含需要更新的字段
     *
     * @param store 门店信息
     * @return 是否有可更新字段
     */
    private boolean hasUpdatableFields(Store store) {
        return store.getStoreName() != null ||
               store.getStoreType() != null ||
               store.getProvince() != null ||
               store.getCity() != null ||
               store.getDistrict() != null ||
               store.getAddress() != null ||
               store.getContactPhone() != null ||
               store.getContactEmail() != null ||
               store.getBusinessHours() != null ||
               store.getArea() != null ||
               store.getEmployeeCount() != null ||
               store.getMemberCount() != null ||
               store.getStatus() != null ||
               store.getOpenTime() != null;
    }

    /**
     * 删除门店
     *
     * @param id 门店ID
     * @return 是否删除成功
     */
    public boolean deleteStore(Long id) {
        log.debug("删除门店，ID：{}", id);

        try {
            int result = storeMapper.deleteById(id);
            return result > 0;
        } catch (Exception e) {
            log.error("删除门店失败，ID：{}", id, e);
            return false;
        }
    }

    /**
     * 生成门店编号
     *
     * @return 门店编号
     */
    private String generateStoreNo() {
        // 获取当前门店数量
        Long count = storeMapper.selectCount(null);
        // 生成门店编号，格式：ST + 4位数字，如：ST0001
        return String.format("ST%04d", count + 1);
    }

    /**
     * 获取指定门店的员工列表(包含角色信息)
     *
     * @param storeId 门店ID
     * @return 门店员工列表(包含角色信息)
     */
    public List<EmployeeDTO> getStoreEmployees(Long storeId) {
        log.debug("获取门店员工列表，门店ID：{}", storeId);

        if (storeId == null) {
            return List.of();
        }

        // 获取门店所有员工
        List<SysUser> employees = sysUserMapper.selectStoreEmployees(storeId);
        
        // 转换为 EmployeeDTO,包含角色信息
        return employees.stream()
                .map(this::convertToEmployeeDTO)
                .toList();
    }
    
    /**
     * 将 SysUser 转换为 EmployeeDTO(包含角色信息)
     */
    private EmployeeDTO convertToEmployeeDTO(SysUser user) {
        EmployeeDTO dto = EmployeeDTO.builder()
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
                .loginCount(user.getLoginCount())
                .lastLoginTime(user.getLastLoginTime())
                .lastLoginIp(user.getLastLoginIp())
                .createdTime(user.getCreatedTime())
                .updatedTime(user.getUpdatedTime())
                .build();
        
        // 查询用户角色
        var userRole = sysUserRoleMapper.selectByUserId(user.getId());
        if (userRole != null) {
            var role = sysRoleMapper.selectById(userRole.getRoleId());
            if (role != null) {
                dto.setRoleCode(role.getRoleCode());
                dto.setRoleName(role.getRoleName());
            }
        }
        
        return dto;
    }

    /**
     * 更新门店店长（包含角色切换）
     *
     * @param storeId 门店ID
     * @param newManagerId 新店长ID
     * @return 是否更新成功
     */
    @Transactional
    public boolean updateStoreManager(Long storeId, Long newManagerId) {
        log.info("更新门店店长，门店ID：{}，新店长ID：{}", storeId, newManagerId);

        try {
            // 1. 获取门店当前店长ID
            Store store = storeMapper.selectById(storeId);
            if (store == null) {
                log.error("门店不存在，ID：{}", storeId);
                return false;
            }

            Long oldManagerId = store.getManagerId();

            // 2. 如果新店长与当前店长相同，直接返回
            if (newManagerId != null && newManagerId.equals(oldManagerId)) {
                log.info("新店长与当前店长相同，无需更新");
                return true;
            }

            // 3. 如果有原店长，将其角色降为店员
            if (oldManagerId != null) {
                log.info("将原店长（ID：{}）角色降为店员", oldManagerId);
                // 删除原店长的门店管理员角色
                sysUserRoleMapper.deleteByUserIdAndRoleId(oldManagerId, 2L); // 2L是门店管理员角色ID

                // 如果原店长没有其他角色，则添加店员角色
                List<Long> userRoles = sysUserRoleMapper.selectRoleIdsByUserId(oldManagerId);
                if (userRoles.isEmpty()) {
                    sysUserRoleMapper.insertUserRole(oldManagerId, 3L); // 3L是店员角色ID
                }
            }

            // 4. 设置新店长
            if (newManagerId != null) {
                // 获取新店长信息
                SysUser newManager = sysUserMapper.selectById(newManagerId);
                if (newManager == null) {
                    log.error("新店长不存在，ID：{}", newManagerId);
                    return false;
                }

                // 将新店长的角色设置为门店管理员
                log.info("将新店长（ID：{}）角色设置为门店管理员", newManagerId);

                // 删除用户现有的店员角色（如果有）
                sysUserRoleMapper.deleteByUserIdAndRoleId(newManagerId, 3L);

                // 添加门店管理员角色
                List<Long> existingRoles = sysUserRoleMapper.selectRoleIdsByUserId(newManagerId);
                boolean hasStoreAdminRole = existingRoles.contains(2L);

                if (!hasStoreAdminRole) {
                    sysUserRoleMapper.insertUserRole(newManagerId, 2L);
                }

                // 更新门店信息
                store.setManagerId(newManagerId);
            } else {
                // 如果新店长为null，清空店长信息
                store.setManagerId(null);
            }

            // 5. 更新门店信息
            int result = storeMapper.updateById(store);

            if (result > 0) {
                log.info("门店店长更新成功，门店ID：{}", storeId);
                return true;
            } else {
                log.error("门店店长更新失败，门店ID：{}", storeId);
                return false;
            }

        } catch (Exception e) {
            log.error("更新门店店长失败，门店ID：{}，新店长ID：{}", storeId, newManagerId, e);
            throw e;
        }
    }

    /**
     * 填充门店列表的店长姓名
     *
     * @param stores 门店列表
     */
    private void fillManagerNames(List<Store> stores) {
        if (stores == null || stores.isEmpty()) {
            return;
        }

        // 收集所有店长ID
        List<Long> managerIds = stores.stream()
                .filter(store -> store.getManagerId() != null)
                .map(Store::getManagerId)
                .distinct()
                .toList();

        if (managerIds.isEmpty()) {
            return;
        }

        // 批量查询店长信息
        List<SysUser> managers = sysUserMapper.selectBatchIds(managerIds);

        // 创建店长ID到姓名的映射
        java.util.Map<Long, String> managerNameMap = managers.stream()
                .collect(java.util.stream.Collectors.toMap(
                        SysUser::getId,
                        SysUser::getName,
                        (existing, replacement) -> existing
                ));

        // 填充店长姓名
        for (Store store : stores) {
            if (store.getManagerId() != null) {
                store.setManagerName(managerNameMap.get(store.getManagerId()));
            }
        }
    }

    /**
     * 更新门店统计数据（员工数和会员数）
     *
     * @param storeId 门店ID，如果为null则更新所有门店
     */
    public void updateStoreStatistics(Long storeId) {
        log.info("更新门店统计数据，门店ID：{}", storeId);

        try {
            List<Store> storesToUpdate;

            if (storeId != null) {
                // 更新指定门店
                Store store = storeMapper.selectById(storeId);
                if (store != null) {
                    storesToUpdate = List.of(store);
                } else {
                    log.warn("门店不存在，ID：{}", storeId);
                    return;
                }
            } else {
                // 更新所有门店
                storesToUpdate = storeMapper.selectList(null);
            }

            for (Store store : storesToUpdate) {
                // 统计员工数
                Integer employeeCount = storeMapper.countEmployeesByStoreId(store.getId());

                // 统计会员数
                Integer memberCount = storeMapper.countMembersByStoreId(store.getId());

                // 更新数据库
                storeMapper.updateEmployeeCount(store.getId(), employeeCount);
                storeMapper.updateMemberCount(store.getId(), memberCount);

                // 更新当前对象的统计数据
                store.setEmployeeCount(employeeCount);
                store.setMemberCount(memberCount);

                log.debug("门店ID：{}，员工数：{}，会员数：{}", store.getId(), employeeCount, memberCount);
            }

            log.info("门店统计数据更新完成");
        } catch (Exception e) {
            log.error("更新门店统计数据失败，门店ID：{}", storeId, e);
            throw new RuntimeException("更新门店统计数据失败", e);
        }
    }

    /**
     * 批量更新所有门店的统计数据
     */
    public void updateAllStoreStatistics() {
        updateStoreStatistics(null);
    }
}