package cloud.hilang.cs_member.controller;

import cloud.hilang.cs_member.common.ApiResponse;
import cloud.hilang.cs_member.entity.Store;
import cloud.hilang.cs_member.entity.SysUser;
import cloud.hilang.cs_member.service.StoreService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 门店管理控制器
 *
 * @author HiLang Cloud Team
 * @since 2025-01-21
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/admin/stores")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "门店管理", description = "门店信息的增删改查接口")
public class StoreController {

    private final StoreService storeService;

    /**
     * 获取所有营业中的门店
     */
    @GetMapping("/open")
    @Operation(summary = "获取营业中的门店", description = "获取所有营业状态正常的门店")
    public ResponseEntity<ApiResponse<List<Store>>> getOpenStores() {
        log.debug("获取营业中的门店列表");

        try {
            List<Store> stores = storeService.getOpenStores();
            return ResponseEntity.ok(ApiResponse.success(stores));
        } catch (Exception e) {
            log.error("获取营业中的门店列表失败", e);
            return ResponseEntity.ok(ApiResponse.error(500, "服务器内部错误"));
        }
    }

    /**
     * 获取所有门店（分页）
     */
    @GetMapping
    @Operation(summary = "获取所有门店", description = "获取所有门店列表（分页）")
    public ResponseEntity<ApiResponse<com.github.pagehelper.PageInfo<Store>>> getAllStores(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int pageNum,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") int pageSize,
            @Parameter(description = "门店状态") @RequestParam(required = false) Integer status,
            @Parameter(description = "搜索关键字") @RequestParam(required = false) String keyword) {
        log.debug("获取所有门店列表，pageNum={}, pageSize={}, status={}, keyword={}", pageNum, pageSize, status, keyword);

        try {
            com.github.pagehelper.PageInfo<Store> pageInfo = storeService.getAllStores(pageNum, pageSize, status, keyword);
            return ResponseEntity.ok(ApiResponse.success(pageInfo));
        } catch (Exception e) {
            log.error("获取门店列表失败", e);
            return ResponseEntity.ok(ApiResponse.error(500, "服务器内部错误"));
        }
    }

    /**
     * 更新门店状态
     */
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "更新门店状态", description = "管理员更新门店营业状态")
    public ResponseEntity<ApiResponse<String>> updateStoreStatus(
            @Parameter(description = "门店ID", example = "1")
            @PathVariable Long id,
            @Parameter(description = "状态", example = "1")
            @RequestParam Integer status) {

        log.info("管理员更新门店状态，ID：{}，status：{}", id, status);

        try {
            boolean result = storeService.updateStoreStatus(id, status);
            if (result) {
                String statusText = status == 1 ? "启用" : "停用";
                return ResponseEntity.ok(ApiResponse.success("门店状态更新成功：" + statusText));
            } else {
                return ResponseEntity.ok(ApiResponse.error(400, "门店状态更新失败"));
            }
        } catch (Exception e) {
            log.error("更新门店状态失败，ID：{}，status：{}", id, status, e);
            return ResponseEntity.ok(ApiResponse.error(500, "服务器内部错误"));
        }
    }

    /**
     * 新增门店
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "新增门店", description = "管理员新增门店信息")
    public ResponseEntity<ApiResponse<Store>> createStore(
            @Parameter(description = "门店信息", required = true)
            @RequestBody Store store) {

        log.info("管理员新增门店，门店名称：{}", store.getStoreName());

        try {
            Store createdStore = storeService.createStore(store);
            return ResponseEntity.ok(ApiResponse.success("门店创建成功", createdStore));
        } catch (Exception e) {
            log.error("新增门店失败，门店名称：{}", store.getStoreName(), e);
            return ResponseEntity.ok(ApiResponse.error(500, "服务器内部错误"));
        }
    }

    /**
     * 根据ID获取门店详情
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "获取门店详情", description = "根据ID获取门店详细信息")
    public ResponseEntity<ApiResponse<Store>> getStoreById(
            @Parameter(description = "门店ID", example = "1")
            @PathVariable Long id) {

        log.debug("获取门店详情，ID：{}", id);

        try {
            Store store = storeService.getStoreById(id);
            if (store != null) {
                return ResponseEntity.ok(ApiResponse.success(store));
            } else {
                return ResponseEntity.ok(ApiResponse.error(404, "门店不存在"));
            }
        } catch (Exception e) {
            log.error("获取门店详情失败，ID：{}", id, e);
            return ResponseEntity.ok(ApiResponse.error(500, "服务器内部错误"));
        }
    }

    /**
     * 更新门店信息
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "更新门店信息", description = "管理员更新门店信息，支持店长变更的多表操作")
    public ResponseEntity<ApiResponse<Store>> updateStore(
            @Parameter(description = "门店ID", example = "1")
            @PathVariable Long id,
            @Parameter(description = "门店信息", required = true)
            @RequestBody Store store) {

        log.info("管理员更新门店信息，ID：{}，门店名称：{}", id, store.getStoreName());

        try {
            // 使用新的完整更新方法，自动处理店长变更的多表操作
            Store updatedStore = storeService.updateStoreInfo(id, store);
            return ResponseEntity.ok(ApiResponse.success("门店信息更新成功", updatedStore));
        } catch (RuntimeException e) {
            log.error("更新门店信息失败，ID：{}，门店名称：{}，错误：{}", id, store.getStoreName(), e.getMessage());

            // 根据错误信息返回不同的错误码
            if (e.getMessage().contains("门店不存在")) {
                return ResponseEntity.ok(ApiResponse.error(404, e.getMessage()));
            } else if (e.getMessage().contains("店长更新失败") || e.getMessage().contains("门店信息更新失败")) {
                return ResponseEntity.ok(ApiResponse.error(400, e.getMessage()));
            } else {
                return ResponseEntity.ok(ApiResponse.error(500, "服务器内部错误"));
            }
        } catch (Exception e) {
            log.error("更新门店信息失败，ID：{}，门店名称：{}", id, store.getStoreName(), e);
            return ResponseEntity.ok(ApiResponse.error(500, "服务器内部错误"));
        }
    }

    /**
     * 删除门店
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "删除门店", description = "管理员删除门店")
    public ResponseEntity<ApiResponse<String>> deleteStore(
            @Parameter(description = "门店ID", example = "1")
            @PathVariable Long id) {

        log.info("管理员删除门店，ID：{}", id);

        try {
            boolean result = storeService.deleteStore(id);
            if (result) {
                return ResponseEntity.ok(ApiResponse.success("门店删除成功"));
            } else {
                return ResponseEntity.ok(ApiResponse.error(400, "门店删除失败"));
            }
        } catch (Exception e) {
            log.error("删除门店失败，ID：{}", id, e);
            return ResponseEntity.ok(ApiResponse.error(500, "服务器内部错误"));
        }
    }

    /**
     * 获取门店的员工列表
     */
    @GetMapping("/{storeId}/employees")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "获取门店的员工列表", description = "获取指定门店的所有员工(包含角色信息),用于店长分配")
    public ResponseEntity<ApiResponse<List<cloud.hilang.cs_member.dto.EmployeeDTO>>> getStoreEmployees(
            @Parameter(description = "门店ID", example = "1")
            @PathVariable Long storeId) {

        log.debug("获取门店员工列表，门店ID：{}", storeId);

        try {
            List<cloud.hilang.cs_member.dto.EmployeeDTO> employees = storeService.getStoreEmployees(storeId);
            return ResponseEntity.ok(ApiResponse.success(employees));
        } catch (Exception e) {
            log.error("获取门店员工列表失败", e);
            return ResponseEntity.ok(ApiResponse.error(500, "服务器内部错误"));
        }
    }

    /**
     * 更新门店统计数据（员工数和会员数）
     */
    @PostMapping("/{storeId}/statistics/refresh")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "更新门店统计数据", description = "更新指定门店的员工数和会员数统计")
    public ResponseEntity<ApiResponse<String>> refreshStoreStatistics(
            @Parameter(description = "门店ID", example = "1")
            @PathVariable Long storeId) {

        log.info("更新门店统计数据，门店ID：{}", storeId);

        try {
            storeService.updateStoreStatistics(storeId);
            return ResponseEntity.ok(ApiResponse.success("门店统计数据更新成功"));
        } catch (Exception e) {
            log.error("更新门店统计数据失败，门店ID：{}", storeId, e);
            return ResponseEntity.ok(ApiResponse.error(500, "服务器内部错误"));
        }
    }

    /**
     * 更新所有门店统计数据
     */
    @PostMapping("/statistics/refresh-all")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "更新所有门店统计数据", description = "批量更新所有门店的员工数和会员数统计")
    public ResponseEntity<ApiResponse<String>> refreshAllStoreStatistics() {

        log.info("批量更新所有门店统计数据");

        try {
            storeService.updateAllStoreStatistics();
            return ResponseEntity.ok(ApiResponse.success("所有门店统计数据更新成功"));
        } catch (Exception e) {
            log.error("批量更新门店统计数据失败", e);
            return ResponseEntity.ok(ApiResponse.error(500, "服务器内部错误"));
        }
    }
}