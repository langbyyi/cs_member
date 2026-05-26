package cloud.hilang.cs_member.controller;

import cloud.hilang.cs_member.common.ApiResponse;
import cloud.hilang.cs_member.entity.SystemConfig;
import cloud.hilang.cs_member.service.SystemConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 系统配置管理控制器
 *
 * @author HiLang Cloud Team
 * @since 2025-01-24
 */
@Tag(name = "系统配置管理", description = "系统配置相关接口")
@RestController
@RequestMapping("/api/v1/admin/system-config")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class SystemConfigController {

    private final SystemConfigService systemConfigService;

    @Operation(summary = "获取所有配置", description = "获取系统所有配置信息")
    @GetMapping("/list")
    public ResponseEntity<ApiResponse<List<SystemConfig>>> getAllConfigs() {
        List<SystemConfig> configs = systemConfigService.getAllConfigs();
        return ResponseEntity.ok(ApiResponse.success(configs));
    }

    @Operation(summary = "保存配置", description = "保存单个配置")
    @PostMapping("/save")
    public ResponseEntity<ApiResponse<Boolean>> saveConfig(@RequestBody Map<String, String> request) {
        String configKey = request.get("configKey");
        String configValue = request.get("configValue");

        if (configKey == null || configValue == null) {
            return ResponseEntity.ok(ApiResponse.error("配置键和配置值不能为空"));
        }

        boolean result = systemConfigService.saveConfig(configKey, configValue);
        if (result) {
            return ResponseEntity.ok(ApiResponse.success("保存成功", true));
        } else {
            return ResponseEntity.ok(ApiResponse.error("保存失败"));
        }
    }

    @Operation(summary = "批量保存配置", description = "批量保存配置")
    @PostMapping("/batch")
    public ResponseEntity<ApiResponse<Boolean>> batchSaveConfigs(@RequestBody Map<String, String> configs) {
        if (configs == null || configs.isEmpty()) {
            return ResponseEntity.ok(ApiResponse.error("配置数据不能为空"));
        }

        boolean result = systemConfigService.batchSaveConfigs(configs);
        if (result) {
            return ResponseEntity.ok(ApiResponse.success("批量保存成功", true));
        } else {
            return ResponseEntity.ok(ApiResponse.error("批量保存失败"));
        }
    }
}