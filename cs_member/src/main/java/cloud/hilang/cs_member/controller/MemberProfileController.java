package cloud.hilang.cs_member.controller;

import cloud.hilang.cs_member.common.ApiResponse;
import cloud.hilang.cs_member.entity.Member;
import cloud.hilang.cs_member.dto.UpdateMemberProfileRequest;
import cloud.hilang.cs_member.dto.ChangePasswordRequest;
import cloud.hilang.cs_member.mapper.MemberMapper;
import cloud.hilang.cs_member.service.MemberCouponService;
import cloud.hilang.cs_member.service.MemberPointsService;
import cloud.hilang.cs_member.util.MemberJwtUtil;
import cloud.hilang.cs_member.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/member/profile")
@RequiredArgsConstructor
@Tag(name = "会员个人信息", description = "会员个人信息管理相关接口")
public class MemberProfileController {

    private final MemberMapper memberMapper;
    private final MemberJwtUtil memberJwtUtil;
    private final MemberPointsService memberPointsService;
    private final MemberCouponService memberCouponService;
    private final PasswordEncoder passwordEncoder;

    private Long getCurrentMemberId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
        return userDetails.getUserId();
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('MEMBER')")
    @Operation(summary = "获取个人信息", description = "获取当前登录会员的详细信息")
    public ResponseEntity<ApiResponse<Member>> getCurrentMemberProfile() {
        Long memberId = getCurrentMemberId();
        Member member = memberMapper.selectById(memberId);
        if (member == null) {
            return ResponseEntity.ok(ApiResponse.error(404, "会员信息不存在"));
        }
        return ResponseEntity.ok(ApiResponse.success(member));
    }

    @GetMapping("/current")
    @PreAuthorize("hasRole('MEMBER')")
    @Operation(summary = "获取当前会员详细信息", description = "获取当前登录会员的完整信息，页面刷新时使用")
    public ResponseEntity<ApiResponse<Object>> getCurrentMemberInfo(
            @RequestHeader("Authorization") String authorization) {
        try {
            String token = authorization.replace("Bearer ", "");
            Long currentMemberId = memberJwtUtil.getUserIdFromToken(token);
            if (currentMemberId == null) {
                return ResponseEntity.ok(ApiResponse.error(401, "无效的访问令牌"));
            }
            Member member = memberMapper.selectById(currentMemberId);
            if (member == null) {
                return ResponseEntity.ok(ApiResponse.error(404, "会员信息不存在"));
            }
            return ResponseEntity.ok(ApiResponse.success(member));
        } catch (Exception e) {
            log.error("获取会员信息失败", e);
            return ResponseEntity.ok(ApiResponse.error(500, "获取会员信息失败"));
        }
    }

    @PutMapping("/me")
    @PreAuthorize("hasRole('MEMBER')")
    @Operation(summary = "更新个人信息", description = "更新当前登录会员的个人信息")
    public ResponseEntity<ApiResponse<Member>> updateCurrentMemberProfile(
            @Valid @RequestBody UpdateMemberProfileRequest request) {
        Long memberId = getCurrentMemberId();
        Member member = memberMapper.selectById(memberId);
        if (member == null) {
            return ResponseEntity.ok(ApiResponse.error(404, "会员信息不存在"));
        }
        if (request.getName() != null) member.setName(request.getName());
        if (request.getEmail() != null) member.setEmail(request.getEmail());
        if (request.getGender() != null) member.setGender(request.getGender());
        if (request.getBirthday() != null) member.setBirthday(LocalDate.parse(request.getBirthday()));
        if (request.getAddress() != null) member.setAddress(request.getAddress());
        if (request.getAvatar() != null) member.setAvatar(request.getAvatar());
        member.setUpdatedTime(LocalDateTime.now());
        memberMapper.updateMemberInfo(member);
        return ResponseEntity.ok(ApiResponse.success("个人信息更新成功", member));
    }

    @PutMapping("/change-password")
    @PreAuthorize("hasRole('MEMBER')")
    @Operation(summary = "修改密码", description = "会员修改登录密码")
    public ResponseEntity<ApiResponse<Boolean>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request) {
        Long memberId = getCurrentMemberId();
        Member member = memberMapper.selectById(memberId);
        if (member == null) {
            return ResponseEntity.ok(ApiResponse.error(404, "会员信息不存在"));
        }
        if (!passwordEncoder.matches(request.getOldPassword(), member.getPassword())) {
            return ResponseEntity.ok(ApiResponse.error("原密码不正确"));
        }
        member.setPassword(passwordEncoder.encode(request.getNewPassword()));
        member.setUpdatedTime(LocalDateTime.now());
        memberMapper.updateMemberInfo(member);
        return ResponseEntity.ok(ApiResponse.success("密码修改成功", true));
    }

    @GetMapping("/points")
    @PreAuthorize("hasRole('MEMBER')")
    @Operation(summary = "获取积分余额", description = "获取当前会员的积分余额信息")
    public ResponseEntity<ApiResponse<Object>> getMemberPoints() {
        Long memberId = getCurrentMemberId();
        Map<String, Object> pointsInfo = memberPointsService.getPointsBalance(memberId);
        return ResponseEntity.ok(ApiResponse.success(pointsInfo));
    }

    @GetMapping("/coupons-count")
    @PreAuthorize("hasRole('MEMBER')")
    @Operation(summary = "获取优惠券数量", description = "获取当前会员的可用优惠券数量")
    public ResponseEntity<ApiResponse<Object>> getMemberCouponsCount() {
        Long memberId = getCurrentMemberId();
        Map<String, Object> result = new HashMap<>();
        result.put("total", memberCouponService.countByMemberId(memberId));
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
