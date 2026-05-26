package cloud.hilang.cs_member.mapper;

import cloud.hilang.cs_member.entity.SysUser;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 系统用户Mapper接口
 *
 * @author HiLang Cloud Team
 * @since 2025-01-14
 */
@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {

    /**
     * 根据手机号查询用户
     *
     * @param phone 手机号
     * @return 用户信息
     */
    SysUser selectByPhone(@Param("phone") String phone);

    /**
     * 根据手机号查找用户（用于登录）
     *
     * @param phone 手机号
     * @return 用户信息
     */
    SysUser findByPhone(@Param("phone") String phone);

    /**
     * 根据ID查找用户
     *
     * @param id 用户ID
     * @return 用户信息
     */
    SysUser findById(@Param("id") Long id);

    /**
     * 根据邮箱查询用户
     *
     * @param email 邮箱
     * @return 用户信息
     */
    SysUser selectByEmail(@Param("email") String email);

    /**
     * 根据ID查询用户及其角色权限
     *
     * @param id 用户ID
     * @return 用户信息
     */
    SysUser selectWithRolesById(@Param("id") Long id);

    /**
     * 更新用户登录信息
     *
     * @param id 用户ID
     * @param loginTime 登录时间
     * @param loginIp 登录IP
     * @return 更新结果
     */
    int updateLoginInfo(@Param("id") Long id,
                       @Param("loginTime") LocalDateTime loginTime,
                       @Param("loginIp") String loginIp);

    /**
     * 更新用户状态
     *
     * @param id 用户ID
     * @param status 新状态
     * @return 更新结果
     */
    int updateStatus(@Param("id") Long id,
                    @Param("status") Integer status);

    /**
     * 重置密码错误次数
     *
     * @param id 用户ID
     * @return 更新结果
     */
    int resetPasswordErrorCount(@Param("id") Long id);

    /**
     * 增加密码错误次数
     *
     * @param id 用户ID
     * @return 更新结果
     */
    int increasePasswordErrorCount(@Param("id") Long id);

    /**
     * 锁定用户账户
     *
     * @param id 用户ID
     * @return 更新结果
     */
    int lockUser(@Param("id") Long id);

    /**
     * 解锁用户账户
     *
     * @param id 用户ID
     * @return 更新结果
     */
    int unlockUser(@Param("id") Long id);

    
    /**
     * 查询用户权限
     *
     * @param userId 用户ID
     * @return 权限代码列表
     */
    List<String> selectUserPermissions(@Param("userId") Long userId);

    /**
     * 更新密码修改时间
     *
     * @param id 用户ID
     * @param passwordUpdateTime 密码修改时间
     * @return 更新结果
     */
    int updatePasswordUpdateTime(@Param("id") Long id,
                                @Param("passwordUpdateTime") LocalDateTime passwordUpdateTime);

    /**
     * 分页查询员工列表（排除系统管理员）
     *
     * @param page 分页对象
     * @param name 姓名（模糊查询）
     * @param phone 手机号（模糊查询）
     * @param storeId 门店ID
     * @param roleName 角色名称
     * @param status 状态
     * @param orderBy 排序字段
     * @return 员工分页列表
     */
    IPage<SysUser> selectEmployeesPage(Page<SysUser> page,
                                       @Param("name") String name,
                                       @Param("phone") String phone,
                                       @Param("storeId") Long storeId,
                                       @Param("roleName") String roleName,
                                       @Param("status") Integer status,
                                       @Param("orderBy") String orderBy);

    /**
     * 根据ID查询员工详情（包含角色和门店信息）
     *
     * @param id 员工ID
     * @return 员工信息
     */
    SysUser selectEmployeeById(@Param("id") Long id);

    /**
     * 获取指定门店的所有员工
     *
     * @param storeId 门店ID
     * @return 门店员工列表
     */
    List<SysUser> selectStoreEmployees(@Param("storeId") Long storeId);
}