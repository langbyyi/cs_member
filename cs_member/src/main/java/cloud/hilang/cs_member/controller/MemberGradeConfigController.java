package cloud.hilang.cs_member.controller;

import cloud.hilang.cs_member.common.ApiResponse;
import cloud.hilang.cs_member.entity.MemberGradeConfig;
import cloud.hilang.cs_member.service.MemberGradeConfigService;
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
 * 会员等级配置管理控制器
 *
 * @author HiLang Cloud Team
 * @since 2025-01-24
 */
@Tag(name = "会员等级配置管理", description = "会员等级配置相关接口")
@RestController
@RequestMapping("/api/v1/admin/member-grade")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class MemberGradeConfigController {

    private final MemberGradeConfigService memberGradeConfigService;

    @Operation(summary = "获取会员等级列表", description = "获取会员等级列表（按排序）")
    @GetMapping("/sorted")
    public ResponseEntity<ApiResponse<List<MemberGradeConfig>>> getSortedGrades() {
        List<MemberGradeConfig> grades = memberGradeConfigService.getGradesBySortOrder();
        return ResponseEntity.ok(ApiResponse.success(grades));
    }

    @Operation(summary = "创建会员等级", description = "创建新的会员等级配置")
    @PostMapping("/create")
    public ResponseEntity<ApiResponse<Boolean>> createGrade(@RequestBody MemberGradeConfig grade) {
        boolean result = memberGradeConfigService.createGrade(grade);
        if (result) {
            return ResponseEntity.ok(ApiResponse.success("创建成功", true));
        } else {
            return ResponseEntity.ok(ApiResponse.error("创建失败"));
        }
    }

    @Operation(summary = "更新会员等级", description = "更新会员等级配置")
    @PutMapping("/update")
    public ResponseEntity<ApiResponse<Boolean>> updateGrade(@RequestBody MemberGradeConfig grade) {
        boolean result = memberGradeConfigService.updateGrade(grade);
        if (result) {
            return ResponseEntity.ok(ApiResponse.success("更新成功", true));
        } else {
            return ResponseEntity.ok(ApiResponse.error("更新失败"));
        }
    }

    @Operation(summary = "删除会员等级", description = "根据ID删除会员等级配置")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Boolean>> deleteGrade(
            @Parameter(description = "等级ID")
            @PathVariable Long id) {
        boolean result = memberGradeConfigService.deleteGrade(id);
        if (result) {
            return ResponseEntity.ok(ApiResponse.success("删除成功", true));
        } else {
            return ResponseEntity.ok(ApiResponse.error("删除失败"));
        }
    }

    @Operation(summary = "获取等级配置列表（前端用）", description = "获取所有会员等级配置，供前端计算等级使用")
    @GetMapping("/public/list")
    @PreAuthorize("hasRole('MEMBER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<MemberGradeConfig>>> getPublicGradeList() {
        List<MemberGradeConfig> grades = memberGradeConfigService.getGradesBySortOrder();
        return ResponseEntity.ok(ApiResponse.success(grades));
    }
}