package cloud.hilang.cs_member.controller;

import cloud.hilang.cs_member.common.ApiResponse;
import cloud.hilang.cs_member.entity.SysPermission;
import cloud.hilang.cs_member.entity.SysRole;
import cloud.hilang.cs_member.entity.SysRolePermission;
import cloud.hilang.cs_member.mapper.SysPermissionMapper;
import cloud.hilang.cs_member.mapper.SysRolePermissionMapper;
import cloud.hilang.cs_member.service.RoleService;
import com.github.pagehelper.PageInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/admin/roles")
@RequiredArgsConstructor
@Tag(name = "角色管理", description = "角色管理相关接口")
public class RoleController {

    private final RoleService roleService;
    private final SysRolePermissionMapper sysRolePermissionMapper;
    private final SysPermissionMapper sysPermissionMapper;

    @GetMapping("/page")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @Operation(summary = "获取角色列表", description = "分页获取角色列表")
    public ResponseEntity<ApiResponse<PageInfo<SysRole>>> getRoleList(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String roleName,
            @RequestParam(required = false) Integer status) {
        PageInfo<SysRole> pageInfo = roleService.getRoleList(pageNum, pageSize, roleName, status);
        return ResponseEntity.ok(ApiResponse.success(pageInfo));
    }

    @GetMapping("")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @Operation(summary = "获取所有角色", description = "获取所有启用的角色")
    public ResponseEntity<ApiResponse<List<SysRole>>> getAllRoles(
            @RequestParam(defaultValue = "true") boolean onlyEnabled) {
        List<SysRole> roles = roleService.getAllRoles(onlyEnabled);
        return ResponseEntity.ok(ApiResponse.success(roles));
    }

    @PostMapping("")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @Operation(summary = "创建角色", description = "创建新的角色")
    public ResponseEntity<ApiResponse<SysRole>> createRole(@RequestBody SysRole role) {
        SysRole created = roleService.createRole(role);
        return ResponseEntity.ok(ApiResponse.success("角色创建成功", created));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @Operation(summary = "获取角色详情", description = "根据ID获取角色详情")
    public ResponseEntity<ApiResponse<SysRole>> getRoleDetail(@PathVariable Long id) {
        SysRole role = roleService.getRoleDetail(id);
        if (role == null) {
            return ResponseEntity.ok(ApiResponse.error(404, "角色不存在"));
        }
        return ResponseEntity.ok(ApiResponse.success(role));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @Operation(summary = "更新角色", description = "更新角色信息")
    public ResponseEntity<ApiResponse<SysRole>> updateRole(
            @PathVariable Long id, @RequestBody SysRole role) {
        SysRole updated = roleService.updateRole(id, role);
        return ResponseEntity.ok(ApiResponse.success("角色更新成功", updated));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @Operation(summary = "删除角色", description = "删除指定角色")
    public ResponseEntity<ApiResponse<Boolean>> deleteRole(@PathVariable Long id) {
        boolean success = roleService.deleteRole(id);
        return ResponseEntity.ok(ApiResponse.success("角色删除成功", success));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @Operation(summary = "切换角色状态", description = "启用或禁用角色")
    public ResponseEntity<ApiResponse<Boolean>> toggleRoleStatus(
            @PathVariable Long id, @RequestParam Integer status) {
        boolean success = roleService.toggleRoleStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success("角色状态更新成功", success));
    }

    @GetMapping("/{id}/permissions")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @Operation(summary = "获取角色权限", description = "获取指定角色的权限ID列表")
    public ResponseEntity<ApiResponse<List<Long>>> getRolePermissions(@PathVariable Long id) {
        List<Long> permissionIds = sysRolePermissionMapper.selectPermissionIdsByRoleId(id);
        return ResponseEntity.ok(ApiResponse.success(permissionIds));
    }

    @PutMapping("/{id}/permissions")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @Operation(summary = "分配角色权限", description = "为指定角色分配权限")
    public ResponseEntity<ApiResponse<String>> assignPermissions(
            @PathVariable Long id, @RequestBody List<Long> permissionIds) {
        sysRolePermissionMapper.deleteByRoleId(id);
        if (permissionIds != null && !permissionIds.isEmpty()) {
            List<SysRolePermission> list = permissionIds.stream().map(pid -> {
                SysRolePermission rp = new SysRolePermission();
                rp.setRoleId(id);
                rp.setPermissionId(pid);
                return rp;
            }).toList();
            sysRolePermissionMapper.batchInsertRolePermissions(list);
        }
        return ResponseEntity.ok(ApiResponse.success("权限分配成功"));
    }

    @GetMapping("/permissions/all")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @Operation(summary = "获取所有权限", description = "获取系统中所有权限列表")
    public ResponseEntity<ApiResponse<List<SysPermission>>> getAllPermissions() {
        List<SysPermission> permissions = sysPermissionMapper.selectList(null);
        return ResponseEntity.ok(ApiResponse.success(permissions));
    }
}
