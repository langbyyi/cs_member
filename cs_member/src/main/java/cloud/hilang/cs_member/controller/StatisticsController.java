package cloud.hilang.cs_member.controller;

import cloud.hilang.cs_member.common.ApiResponse;
import cloud.hilang.cs_member.service.StatisticsService;
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
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/admin/statistics")
@RequiredArgsConstructor
@Tag(name = "统计数据管理", description = "业务统计数据相关接口")
public class StatisticsController {

    private final StatisticsService statisticsService;

    @GetMapping("/business")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "获取业务统计", description = "获取业务统计数据")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getBusinessStatistics(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @RequestParam(required = false) Integer storeId,
            @RequestParam(required = false) String statisticsType,
            @RequestParam(required = false) String groupBy) {
        Map<String, Object> statistics = statisticsService.getBusinessStatistics(startDate, endDate, storeId);
        return ResponseEntity.ok(ApiResponse.success(statistics));
    }

    @GetMapping("/members")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "获取会员统计", description = "获取会员增长和留存统计数据")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getMemberStatistics(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @RequestParam(required = false) Integer storeId) {
        Map<String, Object> statistics = statisticsService.getMemberStatistics(startDate, endDate, storeId);
        return ResponseEntity.ok(ApiResponse.success(statistics));
    }

    @GetMapping("/products")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "获取商品统计", description = "获取商品销售统计数据")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getProductStatistics(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @RequestParam(required = false) Integer storeId) {
        Map<String, Object> statistics = statisticsService.getProductStatistics(startDate, endDate, storeId);
        return ResponseEntity.ok(ApiResponse.success(statistics));
    }

    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "获取仪表板概览", description = "获取仪表板概览数据")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDashboardOverview(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @RequestParam(required = false) Integer storeId) {
        Map<String, Object> overview = statisticsService.getDashboardOverview(startDate, endDate, storeId);
        return ResponseEntity.ok(ApiResponse.success(overview));
    }

    @GetMapping("/employees")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "获取员工统计", description = "获取员工统计数据")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getEmployeeStatistics(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @RequestParam(required = false) Integer storeId) {
        Map<String, Object> statistics = statisticsService.getEmployeeStatistics(startDate, endDate, storeId);
        return ResponseEntity.ok(ApiResponse.success(statistics));
    }

    @GetMapping("/monthly-trend")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "获取月度趋势", description = "获取营收、会员、积分的月度趋势数据")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getMonthlyTrend(
            @RequestParam(defaultValue = "12") int months) {
        Map<String, Object> trend = statisticsService.getMonthlyTrend(months);
        return ResponseEntity.ok(ApiResponse.success(trend));
    }
}
