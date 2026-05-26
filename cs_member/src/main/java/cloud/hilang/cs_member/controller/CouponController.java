package cloud.hilang.cs_member.controller;

import cloud.hilang.cs_member.common.ApiResponse;
import cloud.hilang.cs_member.dto.PageParams;
import com.github.pagehelper.PageInfo;
import cloud.hilang.cs_member.entity.Coupon;
import cloud.hilang.cs_member.service.CouponService;
import cloud.hilang.cs_member.service.MemberCouponService;
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
 * 优惠券管理控制器
 * 处理优惠券创建、分发、使用等管理操作
 *
 * @author HiLang Cloud Team
 * @since 2025-01-19
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/admin/coupons")
@RequiredArgsConstructor
@Tag(name = "优惠券管理", description = "优惠券管理相关接口")
public class CouponController {

    private final CouponService couponService;
    private final MemberCouponService memberCouponService;

    /**
     * 分页查询优惠券列表
     */
    @GetMapping("/page")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "获取优惠券列表", description = "分页获取优惠券列表")
    public ResponseEntity<ApiResponse<PageInfo<Coupon>>> getCouponList(
            @Parameter(description = "页码", example = "1")
            @RequestParam(defaultValue = "1") int pageNum,

            @Parameter(description = "每页大小", example = "10")
            @RequestParam(defaultValue = "20") int pageSize,

            @Parameter(description = "优惠券名称")
            @RequestParam(required = false) String couponName,

            @Parameter(description = "优惠券类型")
            @RequestParam(required = false) Integer couponType,

            @Parameter(description = "状态：0-草稿 1-已发布 2-已过期")
            @RequestParam(required = false) Integer status,
            
            @Parameter(description = "开始日期")
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate validStartTime,
            
            @Parameter(description = "结束日期")
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate validEndTime) {

        log.info("获取优惠券列表，参数：pageNum={}, pageSize={}, couponName={}, couponType={}, status={}",
                pageNum, pageSize, couponName, couponType, status);

        PageInfo<Coupon> pageInfo = couponService.getCouponListPage(pageNum, pageSize, couponName, couponType, status, validStartTime, validEndTime);

        return ResponseEntity.ok(ApiResponse.success(pageInfo));
    }

    /**
     * 创建优惠券
     */
    @PostMapping("")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "创建优惠券", description = "创建新的优惠券")
    public ResponseEntity<ApiResponse<Boolean>> createCoupon(
            @RequestBody Coupon coupon) {

        log.info("创建优惠券：{}", coupon);
        boolean result = couponService.createCoupon(coupon);
        return ResponseEntity.ok(ApiResponse.success("优惠券创建成功", result));
    }

    /**
     * 获取优惠券详情
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "获取优惠券详情", description = "根据ID获取优惠券详情")
    public ResponseEntity<ApiResponse<Coupon>> getCouponDetail(
            @Parameter(description = "优惠券ID")
            @PathVariable Long id) {

        log.info("获取优惠券详情，ID：{}", id);
        Coupon coupon = couponService.getCouponById(id);
        return ResponseEntity.ok(ApiResponse.success(coupon));
    }

    /**
     * 更新优惠券
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "更新优惠券", description = "更新优惠券信息")
    public ResponseEntity<ApiResponse<Boolean>> updateCoupon(
            @Parameter(description = "优惠券ID")
            @PathVariable Long id,
            @RequestBody Coupon coupon) {

        log.info("更新优惠券，ID：{}，数据：{}", id, coupon);
        coupon.setId(id);
        boolean result = couponService.updateCoupon(coupon);
        return ResponseEntity.ok(ApiResponse.success("优惠券更新成功", result));
    }

    /**
     * 删除优惠券
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "删除优惠券", description = "删除指定优惠券")
    public ResponseEntity<ApiResponse<Boolean>> deleteCoupon(
            @Parameter(description = "优惠券ID")
            @PathVariable Long id) {

        log.info("删除优惠券，ID：{}", id);
        boolean result = couponService.deleteCoupon(id);
        return ResponseEntity.ok(ApiResponse.success("优惠券删除成功", result));
    }

    /**
     * 发放优惠券（单个）
     */
    @PostMapping("/issue")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "发放优惠券", description = "向指定会员发放优惠券")
    public ResponseEntity<ApiResponse<Boolean>> issueCoupon(
            @RequestBody Map<String, Object> issueData) {

        log.info("发放优惠券：{}", issueData);
        Long couponId = Long.valueOf(issueData.get("couponId").toString());
        @SuppressWarnings("unchecked")
        List<Long> memberIds = ((List<Number>) issueData.get("memberIds")).stream()
                .map(Number::longValue).toList();
        int count = memberCouponService.issueCouponToMembers(couponId, memberIds);
        return ResponseEntity.ok(ApiResponse.success("优惠券发放成功，共发放" + count + "张", true));
    }

    /**
     * 批量发放优惠券
     */
    @PostMapping("/batch-issue")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "批量发放优惠券", description = "批量向会员发放优惠券")
    public ResponseEntity<ApiResponse<Boolean>> batchIssueCoupon(
            @RequestBody Map<String, Object> batchIssueData) {

        log.info("批量发放优惠券：{}", batchIssueData);
        Long couponId = Long.valueOf(batchIssueData.get("couponId").toString());
        @SuppressWarnings("unchecked")
        List<Long> memberIds = ((List<Number>) batchIssueData.get("memberIds")).stream()
                .map(Number::longValue).toList();
        int count = memberCouponService.issueCouponToMembers(couponId, memberIds);
        return ResponseEntity.ok(ApiResponse.success("批量发放成功，共发放" + count + "张", true));
    }

    /**
     * 获取全局优惠券统计
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "获取优惠券统计", description = "获取全局优惠券统计信息")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getGlobalCouponStatistics() {
        log.info("获取全局优惠券统计");
        Map<String, Object> statistics = couponService.getGlobalCouponStatistics();
        return ResponseEntity.ok(ApiResponse.success(statistics));
    }

    /**
     * 获取单个优惠券使用统计
     */
    @GetMapping("/{id}/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "获取单个优惠券统计", description = "获取单个优惠券使用统计信息")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getCouponStatistics(
            @Parameter(description = "优惠券ID")
            @PathVariable Long id) {
        Coupon coupon = couponService.getCouponById(id);
        if (coupon == null) {
            return ResponseEntity.ok(ApiResponse.error(404, "优惠券不存在"));
        }
        Map<String, Object> stats = new java.util.HashMap<>();
        stats.put("couponId", id);
        stats.put("couponName", coupon.getName());
        stats.put("totalQuantity", coupon.getTotalQuantity());
        stats.put("issuedQuantity", coupon.getIssuedQuantity());
        stats.put("usedQuantity", coupon.getUsedQuantity());
        stats.put("remainingQuantity",
            coupon.getTotalQuantity() != null && coupon.getIssuedQuantity() != null
                ? coupon.getTotalQuantity() - coupon.getIssuedQuantity() : 0);
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    /**
     * 更新优惠券状态
     */
    @PutMapping("/{id}/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "更新优惠券状态", description = "启用或停用优惠券")
    public ResponseEntity<ApiResponse<Boolean>> updateCouponStatus(
            @Parameter(description = "优惠券ID")
            @PathVariable Long id,
            @Parameter(description = "状态")
            @PathVariable Integer status) {

        log.info("更新优惠券状态，ID：{}，status：{}", id, status);
        boolean result = couponService.updateCouponStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success("优惠券状态更新成功", result));
    }
}