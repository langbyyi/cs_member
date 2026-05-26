package cloud.hilang.cs_member.service;

import cloud.hilang.cs_member.dto.CreateEmployeeRequest;
import cloud.hilang.cs_member.dto.EmployeeDTO;
import cloud.hilang.cs_member.dto.UpdateEmployeeRequest;
import com.github.pagehelper.PageInfo;

/**
 * 员工管理服务接口
 *
 * @author HiLang Cloud Team
 * @since 2025-12-03
 */
public interface EmployeeService {

    /**
     * 分页查询员工列表
     *
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param name 姓名（模糊查询）
     * @param phone 手机号（模糊查询）
     * @param storeId 门店ID
     * @param roleName 角色名称
     * @param status 状态
     * @return 员工分页列表
     */
    PageInfo<EmployeeDTO> getEmployeeList(int pageNum, int pageSize,
                                          String name, String phone,
                                          Long storeId, String roleName, Integer status,
                                          String sortProp, String sortOrder);

    /**
     * 根据ID查询员工详情
     *
     * @param id 员工ID
     * @return 员工详情
     */
    EmployeeDTO getEmployeeById(Long id);

    /**
     * 创建员工
     *
     * @param request 创建员工请求
     * @return 员工ID
     */
    Long createEmployee(CreateEmployeeRequest request);

    /**
     * 更新员工信息
     *
     * @param id 员工ID
     * @param request 更新员工请求
     */
    void updateEmployee(Long id, UpdateEmployeeRequest request);

    /**
     * 删除员工
     *
     * @param id 员工ID
     */
    void deleteEmployee(Long id);

    /**
     * 更新员工状态
     *
     * @param id 员工ID
     * @param status 状态：0-禁用 1-启用
     */
    void updateEmployeeStatus(Long id, Integer status);

    /**
     * 重置员工密码
     *
     * @param id 员工ID
     * @param newPassword 新密码
     */
    void resetPassword(Long id, String newPassword);
}
