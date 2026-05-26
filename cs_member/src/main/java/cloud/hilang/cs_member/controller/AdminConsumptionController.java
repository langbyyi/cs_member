package cloud.hilang.cs_member.controller;

import cloud.hilang.cs_member.common.ApiResponse;
import cloud.hilang.cs_member.entity.ConsumptionRecord;
import cloud.hilang.cs_member.mapper.ConsumptionRecordMapper;
import cloud.hilang.cs_member.service.ConsumptionRecordService;
import com.github.pagehelper.PageInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 管理端消费记录控制器
 *
 * @author HiLang Cloud Team
 * @since 2025-01-24
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/admin/consumption")
@RequiredArgsConstructor
@Tag(name = "管理端消费记录", description = "消费记录查询与统计")
public class AdminConsumptionController {

    private final ConsumptionRecordService consumptionRecordService;
    private final ConsumptionRecordMapper consumptionRecordMapper;

    /**
     * 分页查询消费记录
     */
    @GetMapping("/records")
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE_ADMIN')")
    @Operation(summary = "查询消费记录", description = "分页查询消费记录，支持多条件筛选")
    public ResponseEntity<ApiResponse<PageInfo<ConsumptionRecord>>> getConsumptionRecords(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int pageNum,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") int pageSize,
            @Parameter(description = "会员编号") @RequestParam(required = false) String memberNo,
            @Parameter(description = "会员姓名") @RequestParam(required = false) String memberName,
            @Parameter(description = "消费单号") @RequestParam(required = false) String consumptionNo,
            @Parameter(description = "支付方式") @RequestParam(required = false) String paymentMethod,
            @Parameter(description = "开始时间") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startDate,
            @Parameter(description = "结束时间") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endDate) {

        log.info("查询消费记录：pageNum={}, pageSize={}, memberNo={}, memberName={}, consumptionNo={}",
                pageNum, pageSize, memberNo, memberName, consumptionNo);

        PageInfo<ConsumptionRecord> pageInfo = consumptionRecordService.getConsumptionRecordListPage(
                pageNum, pageSize, memberNo, memberName, consumptionNo, startDate, endDate, paymentMethod);

        return ResponseEntity.ok(ApiResponse.success(pageInfo));
    }

    /**
     * 获取消费统计数据
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE_ADMIN')")
    @Operation(summary = "获取消费统计", description = "获取消费概览统计数据")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getConsumptionStatistics() {
        log.info("获取消费统计数据");
        Map<String, Object> statistics = consumptionRecordService.getConsumptionStatistics();
        return ResponseEntity.ok(ApiResponse.success(statistics));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE_ADMIN')")
    @Operation(summary = "更新消费状态", description = "更新消费记录的状态")
    public ResponseEntity<ApiResponse<String>> updateStatus(
            @PathVariable Long id, @RequestParam String status) {
        ConsumptionRecord record = consumptionRecordMapper.selectById(id);
        if (record == null) return ResponseEntity.ok(ApiResponse.error("记录不存在"));
        record.setStatus(status);
        consumptionRecordMapper.updateById(record);
        return ResponseEntity.ok(ApiResponse.success("状态更新成功"));
    }

    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE_ADMIN')")
    @Operation(summary = "取消消费记录", description = "取消指定消费记录")
    public ResponseEntity<ApiResponse<String>> cancelConsumption(
            @PathVariable Long id, @RequestParam(required = false) String remark) {
        ConsumptionRecord record = consumptionRecordMapper.selectById(id);
        if (record == null) return ResponseEntity.ok(ApiResponse.error("记录不存在"));
        record.setStatus("cancelled");
        consumptionRecordMapper.updateById(record);
        return ResponseEntity.ok(ApiResponse.success("取消成功"));
    }

    @PutMapping("/{id}/refund")
    @PreAuthorize("hasAnyRole('ADMIN', 'STORE_ADMIN')")
    @Operation(summary = "退款消费记录", description = "对指定消费记录执行退款")
    public ResponseEntity<ApiResponse<String>> refundConsumption(
            @PathVariable Long id, @RequestParam(required = false) String remark) {
        ConsumptionRecord record = consumptionRecordMapper.selectById(id);
        if (record == null) return ResponseEntity.ok(ApiResponse.error("记录不存在"));
        record.setStatus("refunded");
        consumptionRecordMapper.updateById(record);
        return ResponseEntity.ok(ApiResponse.success("退款成功"));
    }
}
