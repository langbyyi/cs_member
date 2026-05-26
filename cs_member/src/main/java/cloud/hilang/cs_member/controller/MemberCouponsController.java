package cloud.hilang.cs_member.controller;

import cloud.hilang.cs_member.common.ApiResponse;
import cloud.hilang.cs_member.entity.MemberCoupon;
import cloud.hilang.cs_member.security.CustomUserDetails;
import cloud.hilang.cs_member.service.MemberCouponService;
import com.github.pagehelper.PageInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/member/coupons")
@RequiredArgsConstructor
@Tag(name = "会员优惠券管理", description = "会员优惠券查询和使用相关接口")
public class MemberCouponsController {

    private final MemberCouponService memberCouponService;

    private Long getCurrentMemberId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
        return userDetails.getUserId();
    }

    @GetMapping("/my-coupons")
    @PreAuthorize("hasRole('MEMBER')")
    @Operation(summary = "获取我的优惠券", description = "分页获取当前会员的优惠券列表")
    public ResponseEntity<ApiResponse<PageInfo<MemberCoupon>>> getMemberCoupons(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer couponType) {
        Long memberId = getCurrentMemberId();
        PageInfo<MemberCoupon> coupons = memberCouponService.getMemberCoupons(
                memberId, pageNum, pageSize, status, couponType);
        return ResponseEntity.ok(ApiResponse.success(coupons));
    }

    @GetMapping("/available")
    @PreAuthorize("hasRole('MEMBER')")
    @Operation(summary = "获取可用优惠券", description = "获取当前会员可用的优惠券列表")
    public ResponseEntity<ApiResponse<List<MemberCoupon>>> getAvailableCoupons(
            @RequestParam(required = false) Double orderAmount) {
        Long memberId = getCurrentMemberId();
        List<MemberCoupon> coupons = memberCouponService.getAvailableCoupons(memberId);
        return ResponseEntity.ok(ApiResponse.success(coupons));
    }

    @PostMapping("/use/{id}")
    @PreAuthorize("hasRole('MEMBER')")
    @Operation(summary = "使用优惠券", description = "使用指定的优惠券")
    public ResponseEntity<ApiResponse<Boolean>> useCoupon(@PathVariable Long id) {
        Long memberId = getCurrentMemberId();
        boolean success = memberCouponService.useCoupon(id, memberId);
        if (success) {
            return ResponseEntity.ok(ApiResponse.success("优惠券使用成功", true));
        }
        return ResponseEntity.ok(ApiResponse.error("优惠券使用失败"));
    }
}
