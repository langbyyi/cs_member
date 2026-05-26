package cloud.hilang.cs_member.controller;

import cloud.hilang.cs_member.common.ApiResponse;
import cloud.hilang.cs_member.entity.Member;
import cloud.hilang.cs_member.entity.PointsRecord;
import cloud.hilang.cs_member.entity.SystemConfig;
import cloud.hilang.cs_member.mapper.MemberMapper;
import cloud.hilang.cs_member.service.PointsRecordService;
import cloud.hilang.cs_member.service.SystemConfigService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 管理员积分管理控制器
 * 处理管理员对会员积分的调整操作
 *
 * @author HiLang Cloud Team
 * @since 2025-01-21
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/admin/points")
@RequiredArgsConstructor
@Validated
@Tag(name = "管理员积分管理", description = "管理员积分管理相关接口")
@PreAuthorize("hasRole('ADMIN')")
public class AdminPointsController {

    private final MemberMapper memberMapper;
    private final PointsRecordService pointsRecordService;
    private final SystemConfigService systemConfigService;

    private static final String CONFIG_KEY_OPERATION_TYPES = "points_operation_types";
    private static final String DEFAULT_OPERATION_TYPES_JSON = "["
        + "{\"value\":1,\"label\":\"签到奖励\",\"description\":\"会员每日签到获得的积分\"},"
        + "{\"value\":2,\"label\":\"消费返积分\",\"description\":\"消费后返还的积分\"},"
        + "{\"value\":3,\"label\":\"管理员调整\",\"description\":\"管理员手动调整的积分\"},"
        + "{\"value\":4,\"label\":\"积分兑换\",\"description\":\"兑换商品或服务使用的积分\"},"
        + "{\"value\":5,\"label\":\"积分扣减\",\"description\":\"违规或其他原因扣减的积分\"}"
        + "]";

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 调整会员积分
     */
    @PostMapping("/adjust")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "调整会员积分", description = "管理员手动调整会员积分")
    public ResponseEntity<ApiResponse<String>> adjustMemberPoints(
            @Parameter(description = "会员ID")
            @RequestParam Long memberId,

            @Parameter(description = "调整积分数量，正数为增加，负数为扣减")
            @RequestParam Integer points,

            @Parameter(description = "调整原因")
            @RequestParam String reason,

            @Parameter(description = "备注")
            @RequestParam(required = false) String remark) {

        log.info("管理员调整会员积分：memberId={}, points={}, reason={}", memberId, points, reason);

        try {
            // 1. 检查会员是否存在
            Member member = memberMapper.selectById(memberId);
            if (member == null) {
                return ResponseEntity.ok(ApiResponse.error("会员不存在"));
            }

            // 2. 检查积分是否足够（扣减时）
            if (points < 0 && member.getCurrentPoints() + points < 0) {
                return ResponseEntity.ok(ApiResponse.error("积分不足，无法扣减"));
            }

            // 3. 更新会员积分
            int oldCurrentPoints = member.getCurrentPoints();
            int newCurrentPoints = oldCurrentPoints + points;
            int newTotalPoints = points > 0 ? member.getTotalPoints() + points : member.getTotalPoints();

            // 更新member表
            member.setCurrentPoints(newCurrentPoints);
            member.setTotalPoints(newTotalPoints);
            member.setUpdatedTime(LocalDateTime.now());

            int updateResult = memberMapper.updateMemberInfo(member);

            // 4. 更新member_card表的积分
            int cardUpdateResult = memberMapper.updateMemberCardPoints(
                memberId, newCurrentPoints, newTotalPoints, LocalDateTime.now()
            );

            // 5. 创建积分记录
            if (updateResult > 0) {
                PointsRecord pointsRecord = new PointsRecord();
                pointsRecord.setMemberId(memberId);
                pointsRecord.setChangeType(points > 0 ? "earn" : "use");
                pointsRecord.setPointsChange(points);
                pointsRecord.setPointsBefore(oldCurrentPoints);
                pointsRecord.setPointsAfter(newCurrentPoints);
                pointsRecord.setChangeReason(reason);
                pointsRecord.setReferenceType("manual");

                // 获取当前操作员信息
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                if (auth != null) {
                    pointsRecord.setOperatorName(auth.getName());
                }

                // 设置过期时间（积分获得时设置过期时间）
                if (points > 0) {
                    pointsRecord.setExpireTime(LocalDateTime.now().plusYears(1)); // 积分一年后过期
                }

                pointsRecord.setRecordTime(LocalDateTime.now());

                try {
                    pointsRecordService.createPointsRecord(pointsRecord);
                } catch (Exception e) {
                    log.warn("创建积分记录失败，但积分调整成功：", e);
                }
            }

            if (updateResult > 0) {
                String message = points > 0 ?
                    String.format("成功为会员增加 %d 积分，当前积分：%d", points, newCurrentPoints) :
                    String.format("成功为会员扣减 %d 积分，当前积分：%d", Math.abs(points), newCurrentPoints);
                log.info("积分调整成功：memberId={}, adjustment={}, currentPoints={}", memberId, points, newCurrentPoints);
                return ResponseEntity.ok(ApiResponse.success(message, "积分调整成功"));
            } else {
                return ResponseEntity.ok(ApiResponse.error("积分调整失败"));
            }
        } catch (Exception e) {
            log.error("积分调整失败：memberId={}, points={}", memberId, points, e);
            return ResponseEntity.ok(ApiResponse.error("积分调整失败：" + e.getMessage()));
        }
    }

  
    /**
     * 批量调整积分
     */
    @PostMapping("/batch-adjust")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "批量调整积分", description = "批量为多个会员调整积分")
    public ResponseEntity<ApiResponse<String>> batchAdjustPoints(
            @Parameter(description = "批量调整请求")
            @RequestBody List<Map<String, Object>> adjustments) {

        log.info("管理员批量调整积分，共 {} 个会员", adjustments.size());

        int successCount = 0;
        for (Map<String, Object> adj : adjustments) {
            try {
                Long memberId = Long.valueOf(adj.get("memberId").toString());
                Integer points = Integer.valueOf(adj.get("points").toString());
                String reason = adj.getOrDefault("reason", "批量调整").toString();

                Member member = memberMapper.selectById(memberId);
                if (member == null) continue;
                if (points < 0 && member.getCurrentPoints() + points < 0) continue;

                int oldPoints = member.getCurrentPoints();
                int newPoints = oldPoints + points;
                member.setCurrentPoints(newPoints);
                if (points > 0) member.setTotalPoints(member.getTotalPoints() + points);
                member.setUpdatedTime(LocalDateTime.now());
                memberMapper.updateMemberInfo(member);

                PointsRecord record = new PointsRecord();
                record.setMemberId(memberId);
                record.setChangeType(points > 0 ? "earn" : "use");
                record.setPointsChange(points);
                record.setPointsBefore(oldPoints);
                record.setPointsAfter(newPoints);
                record.setChangeReason(reason);
                record.setReferenceType("manual");
                record.setRecordTime(LocalDateTime.now());
                pointsRecordService.createPointsRecord(record);
                successCount++;
            } catch (Exception e) {
                log.warn("批量调整积分失败：{}", adj, e);
            }
        }

        return ResponseEntity.ok(ApiResponse.success(
            String.format("批量调整完成，成功处理 %d 个会员", successCount),
            "批量调整成功"));
    }

    /**
     * 获取积分操作类型列表
     */
    @GetMapping("/operation-types")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "获取积分操作类型", description = "获取可用的积分调整操作类型")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getOperationTypes() {
        log.info("获取积分操作类型列表");

        try {
            SystemConfig config = systemConfigService.getConfigByKey(CONFIG_KEY_OPERATION_TYPES);
            String json;
            if (config != null && config.getConfigValue() != null) {
                json = config.getConfigValue();
            } else {
                systemConfigService.saveConfig(CONFIG_KEY_OPERATION_TYPES, DEFAULT_OPERATION_TYPES_JSON);
                json = DEFAULT_OPERATION_TYPES_JSON;
            }
            List<Map<String, Object>> operationTypes = objectMapper.readValue(json, new TypeReference<>() {});
            return ResponseEntity.ok(ApiResponse.success(operationTypes));
        } catch (Exception e) {
            log.warn("读取积分操作类型失败，返回默认值", e);
            List<Map<String, Object>> operationTypes = List.of(
                Map.of("value", 1, "label", "签到奖励", "description", "会员每日签到获得的积分"),
                Map.of("value", 2, "label", "消费返积分", "description", "消费后返还的积分"),
                Map.of("value", 3, "label", "管理员调整", "description", "管理员手动调整的积分"),
                Map.of("value", 4, "label", "积分兑换", "description", "兑换商品或服务使用的积分"),
                Map.of("value", 5, "label", "积分扣减", "description", "违规或其他原因扣减的积分")
            );
            return ResponseEntity.ok(ApiResponse.success(operationTypes));
        }
    }

    /**
     * 分页查询积分记录列表（兼容前端参数）
     */
    @GetMapping("/records")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "分页查询积分记录", description = "分页查询积分记录列表（管理员视角），兼容前端参数")
    public ResponseEntity<ApiResponse<PageInfo<PointsRecord>>> getPointsRecords(
            @Parameter(description = "页码", example = "1")
            @RequestParam(defaultValue = "1") int pageNum,

            @Parameter(description = "每页大小", example = "10")
            @RequestParam(defaultValue = "10") int pageSize,

            @Parameter(description = "会员编号（模糊查询）")
            @RequestParam(required = false) String memberNo,

            @Parameter(description = "会员姓名（模糊查询）")
            @RequestParam(required = false) String memberName,

            @Parameter(description = "积分变动类型")
            @RequestParam(required = false) String changeType,

            @Parameter(description = "开始时间")
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
            LocalDateTime startDate,

            @Parameter(description = "结束时间")
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
            LocalDateTime endDate) {

        log.debug("分页查询积分记录列表，参数：pageNum={}, pageSize={}, memberNo={}, memberName={}, changeType={}, startDate={}, endDate={}",
                pageNum, pageSize, memberNo, memberName, changeType, startDate, endDate);

        try {
            // 调用Service层分页查询
            PageInfo<PointsRecord> pageInfo = pointsRecordService.getPointsRecordListPage(
                    pageNum, pageSize, memberNo, memberName, changeType, startDate, endDate);

            log.debug("分页查询积分记录列表完成，总记录数：{}", pageInfo.getTotal());
            return ResponseEntity.ok(ApiResponse.success(pageInfo));
        } catch (Exception e) {
            log.error("分页查询积分记录列表失败：", e);
            return ResponseEntity.ok(ApiResponse.error("查询积分记录失败：" + e.getMessage()));
        }
    }

    /**
     * 分页查询积分记录列表
     */
    @GetMapping("/list")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "分页查询积分记录", description = "分页查询积分记录列表（管理员视角）")
    public ResponseEntity<ApiResponse<PageInfo<PointsRecord>>> getPointsRecordList(
            @Parameter(description = "页码", example = "1")
            @RequestParam(defaultValue = "1") int pageNum,

            @Parameter(description = "每页大小", example = "10")
            @RequestParam(defaultValue = "10") int pageSize,

            @Parameter(description = "会员编号（模糊查询）")
            @RequestParam(required = false) String memberNo,

            @Parameter(description = "会员姓名（模糊查询）")
            @RequestParam(required = false) String memberName,

            @Parameter(description = "积分变动类型")
            @RequestParam(required = false) String changeType,

            @Parameter(description = "开始时间")
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
            LocalDateTime startDate,

            @Parameter(description = "结束时间")
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
            LocalDateTime endDate) {

        log.debug("分页查询积分记录列表，参数：pageNum={}, pageSize={}, memberNo={}, memberName={}, changeType={}, startDate={}, endDate={}",
                pageNum, pageSize, memberNo, memberName, changeType, startDate, endDate);

        try {
            // 调用Service层分页查询
            PageInfo<PointsRecord> pageInfo = pointsRecordService.getPointsRecordListPage(
                    pageNum, pageSize, memberNo, memberName, changeType, startDate, endDate);

            log.debug("分页查询积分记录列表完成，总记录数：{}", pageInfo.getTotal());
            return ResponseEntity.ok(ApiResponse.success(pageInfo));
        } catch (Exception e) {
            log.error("分页查询积分记录列表失败：", e);
            return ResponseEntity.ok(ApiResponse.error("查询积分记录失败：" + e.getMessage()));
        }
    }

    /**
     * 获取积分统计数据
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "获取积分统计数据", description = "获取积分相关的统计数据")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getPointsStatistics() {
        log.info("获取积分统计数据");

        try {
            Map<String, Object> statistics = pointsRecordService.getPointsStatistics();
            return ResponseEntity.ok(ApiResponse.success(statistics));
        } catch (Exception e) {
            log.error("获取积分统计数据失败：", e);
            return ResponseEntity.ok(ApiResponse.error("获取统计数据失败：" + e.getMessage()));
        }
    }

    /**
     * 获取会员积分记录
     */
    @GetMapping("/records/{memberId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "获取会员积分记录", description = "管理员查看指定会员的积分变动记录")
    public ResponseEntity<ApiResponse<List<PointsRecord>>> getMemberPointsRecords(
            @Parameter(description = "会员ID")
            @PathVariable Long memberId) {

        log.info("管理员查看会员积分记录：memberId={}", memberId);

        try {
            List<PointsRecord> records = pointsRecordService.getPointsRecordsByMemberId(memberId);
            return ResponseEntity.ok(ApiResponse.success("查询成功", records));
        } catch (Exception e) {
            log.error("查看会员积分记录失败：memberId={}", memberId, e);
            return ResponseEntity.ok(ApiResponse.error("查询积分记录失败：" + e.getMessage()));
        }
    }
}