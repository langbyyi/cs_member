package cloud.hilang.cs_member.controller;

import cloud.hilang.cs_member.common.ApiResponse;
import cloud.hilang.cs_member.dto.PageParams;
import cloud.hilang.cs_member.entity.Member;
import cloud.hilang.cs_member.entity.PointsRecord;
import cloud.hilang.cs_member.mapper.MemberMapper;
import cloud.hilang.cs_member.service.MemberService;
import cloud.hilang.cs_member.service.PointsRecordService;
import com.github.pagehelper.PageInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 管理端会员管理控制器
 *
 * @author HiLang Cloud Team
 * @since 2025-01-24
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/admin/members")
@RequiredArgsConstructor
@Validated
@Tag(name = "管理端会员管理", description = "管理员对会员信息的增删改查接口")
public class AdminMemberController {

    private final MemberService memberService;
    private final MemberMapper memberMapper;
    private final PointsRecordService pointsRecordService;

    /**
     * 分页查询会员列表
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "分页查询会员列表", description = "管理员分页查询会员列表，支持多条件查询")
    public ResponseEntity<ApiResponse<PageInfo<Member>>> getMembers(
            @Parameter(description = "页码，从1开始", example = "1")
            @RequestParam(defaultValue = "1") int pageNum,

            @Parameter(description = "每页大小", example = "20")
            @RequestParam(defaultValue = "20") int pageSize,

            @Parameter(description = "会员姓名（模糊查询）")
            @RequestParam(required = false) String name,

            @Parameter(description = "手机号（模糊查询）")
            @RequestParam(required = false) String phone,

            @Parameter(description = "会员编号")
            @RequestParam(required = false) String memberNo,

            @Parameter(description = "会员等级")
            @RequestParam(required = false) Integer memberLevel,

            @Parameter(description = "账户状态")
            @RequestParam(required = false) Integer status,

            @Parameter(description = "门店名称（模糊查询）")
            @RequestParam(required = false) String storeName,

            @Parameter(description = "性别")
            @RequestParam(required = false) Integer gender,

            @Parameter(description = "注册开始时间")
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startTime,

            @Parameter(description = "注册结束时间")
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endTime,

            @Parameter(description = "排序字段")
            @RequestParam(required = false) String sortBy,

            @Parameter(description = "排序方向", example = "DESC")
            @RequestParam(defaultValue = "DESC") String sortOrder) {

        log.info("管理员查询会员列表，pageNum：{}，pageSize：{}，name：{}，phone：{}",
                pageNum, pageSize, name, phone);

        PageParams pageParams = new PageParams();
        pageParams.setPageNum(pageNum);
        pageParams.setPageSize(pageSize);
        pageParams.setSortField(sortBy);
        pageParams.setSortOrder(sortOrder);

        LocalDateTime startDateTime = startTime != null ? startTime.atStartOfDay() : null;
        LocalDateTime endDateTime = endTime != null ? endTime.atTime(23, 59, 59) : null;

        PageInfo<Member> members = memberService.getMemberList(pageParams, name, phone, memberNo,
                memberLevel, status, storeName, gender, startDateTime, endDateTime);

        return ResponseEntity.ok(ApiResponse.success(members));
    }

    /**
     * 根据ID查询会员详情
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "查询会员详情", description = "根据会员ID查询详细信息")
    public ResponseEntity<ApiResponse<Member>> getMemberById(
            @Parameter(description = "会员ID", example = "1")
            @PathVariable Long id) {

        log.info("管理员查询会员详情，ID：{}", id);
        Member member = memberService.getMemberById(id);
        if (member == null) {
            return ResponseEntity.ok(ApiResponse.error("会员不存在"));
        }

        return ResponseEntity.ok(ApiResponse.success(member));
    }

    /**
     * 创建会员
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "创建会员", description = "管理员创建新会员")
    public ResponseEntity<ApiResponse<Member>> createMember(@Valid @RequestBody Member member) {
        log.info("管理员创建会员，phone：{}，name：{}", member.getPhone(), member.getName());

        try {
            Member createdMember = memberService.createMember(member);
            return ResponseEntity.ok(ApiResponse.success(createdMember));
        } catch (Exception e) {
            log.error("创建会员失败", e);
            return ResponseEntity.ok(ApiResponse.error("创建会员失败：" + e.getMessage()));
        }
    }

    /**
     * 更新会员信息
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "更新会员信息", description = "管理员更新会员信息")
    public ResponseEntity<ApiResponse<Member>> updateMember(
            @Parameter(description = "会员ID", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody Member member) {

        log.info("管理员更新会员信息，ID：{}，name：{}", id, member.getName());

        try {
            member.setId(id);
            Member updatedMember = memberService.updateMember(member);
            return ResponseEntity.ok(ApiResponse.success(updatedMember));
        } catch (Exception e) {
            log.error("更新会员失败", e);
            return ResponseEntity.ok(ApiResponse.error("更新会员失败：" + e.getMessage()));
        }
    }

    /**
     * 删除会员（逻辑删除）
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "删除会员", description = "管理员删除会员（逻辑删除）")
    public ResponseEntity<ApiResponse<String>> deleteMember(
            @Parameter(description = "会员ID", example = "1")
            @PathVariable Long id) {

        log.info("管理员删除会员，ID：{}", id);

        try {
            memberService.deleteMember(id);
            return ResponseEntity.ok(ApiResponse.success("删除成功"));
        } catch (Exception e) {
            log.error("删除会员失败", e);
            return ResponseEntity.ok(ApiResponse.error("删除会员失败：" + e.getMessage()));
        }
    }

    /**
     * 批量删除会员
     */
    @DeleteMapping("/batch")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "批量删除会员", description = "管理员批量删除会员")
    public ResponseEntity<ApiResponse<String>> batchDeleteMembers(@RequestBody Long[] ids) {
        log.info("管理员批量删除会员，数量：{}", ids.length);

        try {
            memberService.batchDeleteMembers(ids);
            return ResponseEntity.ok(ApiResponse.success("批量删除成功"));
        } catch (Exception e) {
            log.error("批量删除会员失败", e);
            return ResponseEntity.ok(ApiResponse.error("批量删除会员失败：" + e.getMessage()));
        }
    }

    /**
     * 更新会员状态
     */
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "更新会员状态", description = "管理员更新会员账户状态")
    public ResponseEntity<ApiResponse<String>> updateMemberStatus(
            @Parameter(description = "会员ID", example = "1")
            @PathVariable Long id,
            @Parameter(description = "状态", example = "1")
            @RequestParam Integer status) {

        log.info("管理员更新会员状态，ID：{}，status：{}", id, status);

        try {
            memberService.updateMemberStatus(id, status);
            return ResponseEntity.ok(ApiResponse.success("状态更新成功"));
        } catch (Exception e) {
            log.error("更新会员状态失败", e);
            return ResponseEntity.ok(ApiResponse.error("更新会员状态失败：" + e.getMessage()));
        }
    }

    /**
     * 重置会员密码
     */
    @PutMapping("/{id}/reset-password")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "重置会员密码", description = "管理员重置会员密码")
    public ResponseEntity<ApiResponse<String>> resetMemberPassword(
            @Parameter(description = "会员ID", example = "1")
            @PathVariable Long id,
            @Parameter(description = "新密码", example = "123456")
            @RequestParam String newPassword) {

        log.info("管理员重置会员密码，ID：{}", id);

        try {
            memberService.resetMemberPassword(id, newPassword);
            return ResponseEntity.ok(ApiResponse.success("密码重置成功"));
        } catch (Exception e) {
            log.error("重置会员密码失败", e);
            return ResponseEntity.ok(ApiResponse.error("重置会员密码失败：" + e.getMessage()));
        }
    }

    /**
     * 调整会员余额
     */
    @PutMapping("/{id}/balance")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "调整会员余额", description = "管理员调整会员余额")
    public ResponseEntity<ApiResponse<String>> adjustMemberBalance(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body) {

        BigDecimal changeAmount = new BigDecimal(body.get("changeAmount").toString());
        String remark = body.getOrDefault("remark", "管理员调整").toString();

        Member member = memberMapper.selectById(id);
        if (member == null) {
            return ResponseEntity.ok(ApiResponse.error("会员不存在"));
        }

        BigDecimal newBalance = member.getBalance().add(changeAmount);
        if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
            return ResponseEntity.ok(ApiResponse.error("余额不足"));
        }

        member.setBalance(newBalance);
        member.setUpdatedTime(LocalDateTime.now());
        memberMapper.updateMemberInfo(member);

        return ResponseEntity.ok(ApiResponse.success("余额调整成功"));
    }

    /**
     * 调整会员积分
     */
    @PutMapping("/{id}/points")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "调整会员积分", description = "管理员调整会员积分")
    public ResponseEntity<ApiResponse<String>> adjustMemberPoints(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body) {

        int changePoints = Integer.parseInt(body.get("changePoints").toString());
        int changeType = Integer.parseInt(body.getOrDefault("changeType", "1").toString());
        String remark = body.getOrDefault("remark", "管理员调整").toString();

        Member member = memberMapper.selectById(id);
        if (member == null) {
            return ResponseEntity.ok(ApiResponse.error("会员不存在"));
        }

        int actualChange = changeType == 2 ? -Math.abs(changePoints) : Math.abs(changePoints);
        int oldPoints = member.getCurrentPoints();
        int newPoints = oldPoints + actualChange;

        if (newPoints < 0) {
            return ResponseEntity.ok(ApiResponse.error("积分不足"));
        }

        member.setCurrentPoints(newPoints);
        if (actualChange > 0) {
            member.setTotalPoints(member.getTotalPoints() + actualChange);
        }
        member.setUpdatedTime(LocalDateTime.now());
        memberMapper.updateMemberInfo(member);

        PointsRecord record = new PointsRecord();
        record.setMemberId(id);
        record.setChangeType(actualChange > 0 ? "earn" : "use");
        record.setPointsChange(actualChange);
        record.setPointsBefore(oldPoints);
        record.setPointsAfter(newPoints);
        record.setChangeReason(remark);
        record.setReferenceType("manual");
        record.setRecordTime(LocalDateTime.now());
        try {
            pointsRecordService.createPointsRecord(record);
        } catch (Exception e) {
            log.warn("创建积分记录失败", e);
        }

        return ResponseEntity.ok(ApiResponse.success("积分调整成功"));
    }
}