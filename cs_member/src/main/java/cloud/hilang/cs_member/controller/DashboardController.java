package cloud.hilang.cs_member.controller;

import cloud.hilang.cs_member.common.ApiResponse;
import cloud.hilang.cs_member.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 仪表盘控制器
 *
 * @author HiLang Cloud Team
 * @since 2025-01-18
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/admin/dashboard")
@RequiredArgsConstructor
@Tag(name = "仪表盘管理", description = "仪表盘数据统计相关接口")
public class DashboardController {

    private final DashboardService dashboardService;

    /**
     * 获取仪表盘统计数据
     * 支持角色差异化数据返回
     */
    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "获取仪表盘统计数据", description = "获取仪表盘统计数据，根据用户角色返回不同的数据")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDashboardStats() {
        log.info("获取仪表盘统计数据");
        Map<String, Object> stats = dashboardService.getDashboardStats();
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    /**
     * 获取营收趋势数据
     *
     * @param period 统计周期（天数）
     */
    @GetMapping("/revenue-trend")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "获取营收趋势数据", description = "获取指定时间段的营收趋势数据")
    public ApiResponse<Map<String, Object>> getRevenueTrend(
            @Parameter(description = "统计周期（天数）", example = "7")
            @RequestParam(defaultValue = "7") Integer period) {
        log.info("获取营收趋势数据，周期：{}天", period);
        Map<String, Object> trend = dashboardService.getRevenueTrend(period);
        return ApiResponse.success(trend);
    }

    /**
     * 获取会员等级分布
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/member-level-distribution")
    @Operation(summary = "获取会员等级分布", description = "获取各等级会员数量分布")
    public ApiResponse<List<Map<String, Object>>> getMemberLevelDistribution() {
        log.info("获取会员等级分布");
        List<Map<String, Object>> distribution = dashboardService.getMemberLevelDistribution();
        return ApiResponse.success(distribution);
    }

    /**
     * 获取支付方式统计
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/payment-method-stats")
    @Operation(summary = "获取支付方式统计", description = "获取各种支付方式的使用统计")
    public ApiResponse<Map<String, Object>> getPaymentMethodStats() {
        log.info("获取支付方式统计");
        Map<String, Object> stats = dashboardService.getPaymentMethodStats();
        return ApiResponse.success(stats);
    }

    /**
     * 获取消费时段分布
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/consumption-time-distribution")
    @Operation(summary = "获取消费时段分布", description = "获取不同时段的消费分布统计")
    public ApiResponse<Map<String, Object>> getConsumptionTimeDistribution() {
        log.info("获取消费时段分布");
        Map<String, Object> distribution = dashboardService.getConsumptionTimeDistribution();
        return ApiResponse.success(distribution);
    }

    /**
     * 获取最新动态
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/recent-activities")
    @Operation(summary = "获取最新动态", description = "获取系统最新动态信息，根据角色返回不同内容")
    public ApiResponse<List<Map<String, Object>>> getRecentActivities() {
        log.info("获取最新动态");
        List<Map<String, Object>> activities = dashboardService.getRecentActivities();
        return ApiResponse.success(activities);
    }

    /**
     * 获取业务统计数据
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @param storeId   门店ID（可选）
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/business-stats")
    @Operation(summary = "获取业务统计数据", description = "获取指定时间范围的业务统计数据")
    public ApiResponse<Map<String, Object>> getBusinessStatistics(
            @Parameter(description = "开始日期", example = "2025-01-01")
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @Parameter(description = "结束日期", example = "2025-01-31")
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @Parameter(description = "门店ID", example = "1")
            @RequestParam(required = false) Long storeId) {
        log.info("获取业务统计数据，startDate：{}，endDate：{}，storeId：{}", startDate, endDate, storeId);
        Map<String, Object> stats = dashboardService.getBusinessStatistics(startDate, endDate, storeId);
        return ApiResponse.success(stats);
    }

    /**
     * 获取会员增长趋势
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @param storeId   门店ID（可选）
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/member-growth")
    @Operation(summary = "获取会员增长趋势", description = "获取会员增长趋势数据")
    public ApiResponse<Map<String, Object>> getMemberGrowth(
            @Parameter(description = "开始日期", example = "2025-01-01")
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @Parameter(description = "结束日期", example = "2025-01-31")
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @Parameter(description = "门店ID", example = "1")
            @RequestParam(required = false) Long storeId) {
        log.info("获取会员增长趋势，startDate：{}，endDate：{}，storeId：{}", startDate, endDate, storeId);
        Map<String, Object> growth = dashboardService.getMemberGrowth(startDate, endDate, storeId);
        return ApiResponse.success(growth);
    }

    /**
     * 获取实时数据统计
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/real-time-stats")
    @Operation(summary = "获取实时数据统计", description = "获取实时数据统计信息")
    public ApiResponse<Map<String, Object>> getRealTimeStats() {
        log.info("获取实时数据统计");
        Map<String, Object> stats = dashboardService.getRealTimeStats();
        return ApiResponse.success(stats);
    }
}