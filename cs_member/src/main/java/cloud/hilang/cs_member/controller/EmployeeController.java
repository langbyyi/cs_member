package cloud.hilang.cs_member.controller;

import cloud.hilang.cs_member.common.ApiResponse;
import cloud.hilang.cs_member.dto.CreateEmployeeRequest;
import cloud.hilang.cs_member.dto.EmployeeDTO;
import cloud.hilang.cs_member.dto.UpdateEmployeeRequest;
import cloud.hilang.cs_member.service.EmployeeService;
import com.github.pagehelper.PageInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 员工管理控制器
 *
 * @author HiLang Cloud Team
 * @since 2025-12-03
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/admin/employees")
@RequiredArgsConstructor
@Tag(name = "员工管理", description = "员工的增删改查接口（店长和店员）")
public class EmployeeController {

    private final EmployeeService employeeService;

    /**
     * 获取员工列表
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'STORE_ADMIN')")
    @Operation(summary = "获取员工列表", description = "分页查询员工列表（排除系统管理员）")
    public ResponseEntity<ApiResponse<PageInfo<EmployeeDTO>>> getEmployeeList(
            @Parameter(description = "页码", example = "1")
            @RequestParam(defaultValue = "1") int pageNum,

            @Parameter(description = "每页大小", example = "10")
            @RequestParam(defaultValue = "10") int pageSize,

            @Parameter(description = "姓名（模糊查询）")
            @RequestParam(required = false) String name,

            @Parameter(description = "手机号（模糊查询）")
            @RequestParam(required = false) String phone,

            @Parameter(description = "门店ID")
            @RequestParam(required = false) Long storeId,

            @Parameter(description = "角色名称")
            @RequestParam(required = false) String roleName,

            @Parameter(description = "状态：0-禁用 1-启用")
            @RequestParam(required = false) Integer status,

            @Parameter(description = "排序字段")
            @RequestParam(required = false) String sortProp,

            @Parameter(description = "排序方式：ascending/descending")
            @RequestParam(required = false) String sortOrder) {

        log.info("查询员工列表，pageNum={}, pageSize={}, name={}, phone={}, storeId={}, roleName={}, status={}, sortProp={}, sortOrder={}",
                pageNum, pageSize, name, phone, storeId, roleName, status, sortProp, sortOrder);

        PageInfo<EmployeeDTO> pageInfo = employeeService.getEmployeeList(
                pageNum, pageSize, name, phone, storeId, roleName, status, sortProp, sortOrder);

        return ResponseEntity.ok(ApiResponse.success(pageInfo));
    }

    /**
     * 根据ID获取员工详情
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'STORE_ADMIN')")
    @Operation(summary = "获取员工详情", description = "根据员工ID查询详细信息")
    public ResponseEntity<ApiResponse<EmployeeDTO>> getEmployeeById(
            @Parameter(description = "员工ID")
            @PathVariable Long id) {

        log.info("查询员工详情，id={}", id);

        EmployeeDTO employee = employeeService.getEmployeeById(id);
        return ResponseEntity.ok(ApiResponse.success(employee));
    }

    /**
     * 创建员工
     */
    @PostMapping
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @Operation(summary = "创建员工", description = "创建新的员工（店长或店员）")
    public ResponseEntity<ApiResponse<Long>> createEmployee(
            @Valid @RequestBody CreateEmployeeRequest request) {

        log.info("创建员工，phone={}, name={}, roleCode={}",
                request.getPhone(), request.getName(), request.getRoleCode());

        Long employeeId = employeeService.createEmployee(request);
        return ResponseEntity.ok(ApiResponse.success("员工创建成功", employeeId));
    }

    /**
     * 更新员工
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'STORE_ADMIN')")
    @Operation(summary = "更新员工", description = "更新员工信息")
    public ResponseEntity<ApiResponse<String>> updateEmployee(
            @Parameter(description = "员工ID")
            @PathVariable Long id,

            @Valid @RequestBody UpdateEmployeeRequest request) {

        log.info("更新员工，id={}, name={}", id, request.getName());

        employeeService.updateEmployee(id, request);
        return ResponseEntity.ok(ApiResponse.success("员工更新成功"));
    }

    /**
     * 删除员工
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @Operation(summary = "删除员工", description = "删除指定员工")
    public ResponseEntity<ApiResponse<String>> deleteEmployee(
            @Parameter(description = "员工ID")
            @PathVariable Long id) {

        log.info("删除员工，id={}", id);

        employeeService.deleteEmployee(id);
        return ResponseEntity.ok(ApiResponse.success("员工删除成功"));
    }

    /**
     * 启用/禁用员工
     */
    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'STORE_ADMIN')")
    @Operation(summary = "切换员工状态", description = "启用或禁用员工账户")
    public ResponseEntity<ApiResponse<String>> toggleEmployeeStatus(
            @Parameter(description = "员工ID")
            @PathVariable Long id,

            @Parameter(description = "状态：0-禁用 1-启用")
            @RequestParam Integer status) {

        log.info("切换员工状态，id={}, status={}", id, status);

        employeeService.updateEmployeeStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success("员工状态更新成功"));
    }

    /**
     * 重置员工密码
     */
    @PutMapping("/{id}/reset-password")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @Operation(summary = "重置密码", description = "重置员工登录密码")
    public ResponseEntity<ApiResponse<String>> resetPassword(
            @Parameter(description = "员工ID")
            @PathVariable Long id,

            @Parameter(description = "新密码", example = "123456")
            @RequestParam String newPassword) {

        log.info("重置员工密码，id={}", id);

        employeeService.resetPassword(id, newPassword);
        return ResponseEntity.ok(ApiResponse.success("密码重置成功"));
    }
}
