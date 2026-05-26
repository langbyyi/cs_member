package cloud.hilang.cs_member.controller;

import cloud.hilang.cs_member.common.ApiResponse;
import cloud.hilang.cs_member.entity.MemberGradeConfig;
import cloud.hilang.cs_member.service.MemberGradeConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 会员等级配置公共控制器
 * 提供给会员端使用的等级配置接口
 *
 * @author HiLang Cloud Team
 * @since 2025-11-27
 */
@Tag(name = "会员等级配置(公共)", description = "会员等级配置公共接口")
@RestController
@RequestMapping("/api/v1/member/grade-config")
@RequiredArgsConstructor
public class MemberGradeController {

    private final MemberGradeConfigService memberGradeConfigService;

    @Operation(summary = "获取等级配置列表", description = "获取所有会员等级配置，供前端计算等级使用（公开接口）")
    @GetMapping("/list")
    public ResponseEntity<ApiResponse<List<MemberGradeConfig>>> getGradeList() {
        List<MemberGradeConfig> grades = memberGradeConfigService.getGradesBySortOrder();
        return ResponseEntity.ok(ApiResponse.success(grades));
    }
}
