package cloud.hilang.cs_member.controller;

import cloud.hilang.cs_member.common.ApiResponse;
import cloud.hilang.cs_member.entity.SysUser;
import cloud.hilang.cs_member.service.UserService;
import com.github.pagehelper.PageInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
@Tag(name = "用户管理", description = "系统用户的增删改查接口")
public class UserController {

    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "获取用户列表", description = "分页查询系统用户列表")
    public ResponseEntity<ApiResponse<PageInfo<SysUser>>> getUsers(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) Integer status) {
        PageInfo<SysUser> pageInfo = userService.getUserList(pageNum, pageSize, name, phone, status);
        return ResponseEntity.ok(ApiResponse.success(pageInfo));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "获取用户详情", description = "根据用户ID查询详细信息")
    public ResponseEntity<ApiResponse<SysUser>> getUserById(@PathVariable Long id) {
        SysUser user = userService.getUserById(id);
        if (user == null) {
            return ResponseEntity.ok(ApiResponse.error(404, "用户不存在"));
        }
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    @PostMapping
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @Operation(summary = "创建用户", description = "创建新的系统用户")
    public ResponseEntity<ApiResponse<SysUser>> createUser(@Valid @RequestBody SysUser user) {
        SysUser created = userService.createUser(user);
        return ResponseEntity.ok(ApiResponse.success("用户创建成功", created));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "更新用户", description = "更新用户信息")
    public ResponseEntity<ApiResponse<SysUser>> updateUser(
            @PathVariable Long id, @Valid @RequestBody SysUser user) {
        SysUser updated = userService.updateUser(id, user);
        return ResponseEntity.ok(ApiResponse.success("用户更新成功", updated));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @Operation(summary = "删除用户", description = "删除指定用户")
    public ResponseEntity<ApiResponse<Boolean>> deleteUser(@PathVariable Long id) {
        boolean success = userService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.success("用户删除成功", success));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "切换用户状态", description = "启用或禁用用户账户")
    public ResponseEntity<ApiResponse<Boolean>> toggleUserStatus(
            @PathVariable Long id, @RequestParam Integer status) {
        boolean success = userService.toggleUserStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success("用户状态更新成功", success));
    }

    @PutMapping("/{id}/reset-password")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @Operation(summary = "重置密码", description = "重置用户登录密码")
    public ResponseEntity<ApiResponse<Boolean>> resetPassword(
            @PathVariable Long id, @RequestParam String newPassword) {
        boolean success = userService.resetPassword(id, newPassword);
        return ResponseEntity.ok(ApiResponse.success("密码重置成功", success));
    }
}
