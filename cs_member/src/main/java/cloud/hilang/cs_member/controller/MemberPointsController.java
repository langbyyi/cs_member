package cloud.hilang.cs_member.controller;

import cloud.hilang.cs_member.common.ApiResponse;
import cloud.hilang.cs_member.entity.Coupon;
import cloud.hilang.cs_member.entity.PointsRecord;
import cloud.hilang.cs_member.mapper.CouponMapper;
import cloud.hilang.cs_member.security.CustomUserDetails;
import cloud.hilang.cs_member.service.MemberPointsService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.pagehelper.PageInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/v1/member/points")
@RequiredArgsConstructor
@Tag(name = "会员积分管理", description = "会员积分查询和兑换相关接口")
public class MemberPointsController {

    private final MemberPointsService memberPointsService;
    private final CouponMapper couponMapper;

    private Long getCurrentMemberId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
        return userDetails.getUserId();
    }

    @GetMapping("/balance")
    @PreAuthorize("hasRole('MEMBER')")
    @Operation(summary = "获取积分余额", description = "获取当前会员的积分余额和积分等级信息")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getMemberPointsBalance() {
        Long memberId = getCurrentMemberId();
        Map<String, Object> balance = memberPointsService.getPointsBalance(memberId);
        return ResponseEntity.ok(ApiResponse.success(balance));
    }

    @GetMapping("/records")
    @PreAuthorize("hasRole('MEMBER')")
    @Operation(summary = "获取积分记录", description = "分页获取当前会员的积分收支记录")
    public ResponseEntity<ApiResponse<PageInfo<PointsRecord>>> getMemberPointsRecords(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) Integer changeType,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        Long memberId = getCurrentMemberId();
        PageInfo<PointsRecord> records = memberPointsService.getPointsRecords(
                memberId, pageNum, pageSize, changeType, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(records));
    }

    @GetMapping("/exchange-items")
    @PreAuthorize("hasRole('MEMBER')")
    @Operation(summary = "获取兑换商品", description = "获取可用积分兑换的商品列表（可用优惠券）")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getExchangeItems() {
        QueryWrapper<Coupon> wrapper = new QueryWrapper<>();
        wrapper.eq("status", 1)
               .gt("required_points", 0)
               .gt("valid_end_time", LocalDateTime.now())
               .orderByAsc("required_points");
        List<Coupon> coupons = couponMapper.selectList(wrapper);
        List<Map<String, Object>> items = coupons.stream().map(c -> {
            Map<String, Object> map = new java.util.LinkedHashMap<>();
            map.put("id", c.getId());
            map.put("name", c.getName());
            map.put("points", c.getRequiredPoints());
            map.put("type", c.getCouponType());
            map.put("description", c.getDescription());
            if (c.getDiscountAmount() != null) map.put("discountAmount", c.getDiscountAmount());
            if (c.getMinAmount() != null) map.put("minAmount", c.getMinAmount());
            map.put("validEndTime", c.getValidEndTime());
            return map;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(items));
    }

    @PostMapping("/exchange")
    @PreAuthorize("hasRole('MEMBER')")
    @Operation(summary = "积分兑换", description = "使用积分兑换商品或优惠券")
    public ResponseEntity<ApiResponse<Boolean>> exchangePoints(
            @RequestParam Long itemId, @RequestParam Integer points) {
        Long memberId = getCurrentMemberId();
        boolean success = memberPointsService.exchangePoints(memberId, itemId, points);
        if (success) {
            return ResponseEntity.ok(ApiResponse.success("积分兑换成功", true));
        }
        return ResponseEntity.ok(ApiResponse.error("积分不足或兑换失败"));
    }

    @GetMapping("/statistics")
    @PreAuthorize("hasRole('MEMBER')")
    @Operation(summary = "获取积分统计", description = "获取会员积分的统计信息")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getPointsStatistics() {
        Long memberId = getCurrentMemberId();
        Map<String, Object> statistics = memberPointsService.getPointsStatistics(memberId);
        return ResponseEntity.ok(ApiResponse.success(statistics));
    }
}
