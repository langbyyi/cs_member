package cloud.hilang.cs_member.controller;

import cloud.hilang.cs_member.common.ApiResponse;
import cloud.hilang.cs_member.entity.ConsumptionRecord;
import cloud.hilang.cs_member.mapper.ConsumptionRecordMapper;
import cloud.hilang.cs_member.security.CustomUserDetails;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.pagehelper.PageHelper;
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

@Slf4j
@RestController
@RequestMapping("/api/v1/member/consumption")
@RequiredArgsConstructor
@Tag(name = "会员消费记录", description = "会员消费记录查询相关接口")
public class MemberConsumptionController {

    private final ConsumptionRecordMapper consumptionRecordMapper;

    private Long getCurrentMemberId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
        return userDetails.getUserId();
    }

    @GetMapping("/records")
    @PreAuthorize("hasRole('MEMBER')")
    @Operation(summary = "获取消费记录", description = "分页获取当前会员的消费记录")
    public ResponseEntity<ApiResponse<PageInfo<ConsumptionRecord>>> getMemberConsumptionRecords(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @RequestParam(required = false) Integer paymentMethod) {
        Long memberId = getCurrentMemberId();
        PageHelper.startPage(pageNum, pageSize);
        QueryWrapper<ConsumptionRecord> wrapper = new QueryWrapper<>();
        wrapper.eq("member_id", memberId);
        if (startDate != null) wrapper.ge("consumption_time", startDate.atStartOfDay());
        if (endDate != null) wrapper.le("consumption_time", endDate.atTime(23, 59, 59));
        wrapper.orderByDesc("consumption_time");
        List<ConsumptionRecord> records = consumptionRecordMapper.selectList(wrapper);
        return ResponseEntity.ok(ApiResponse.success(new PageInfo<>(records)));
    }

    @GetMapping("/records/{recordId}")
    @PreAuthorize("hasRole('MEMBER')")
    @Operation(summary = "获取消费详情", description = "根据记录ID获取消费详情")
    public ResponseEntity<ApiResponse<ConsumptionRecord>> getConsumptionDetail(
            @PathVariable Long recordId) {
        Long memberId = getCurrentMemberId();
        QueryWrapper<ConsumptionRecord> wrapper = new QueryWrapper<>();
        wrapper.eq("id", recordId).eq("member_id", memberId);
        ConsumptionRecord record = consumptionRecordMapper.selectOne(wrapper);
        if (record == null) {
            return ResponseEntity.ok(ApiResponse.error(404, "消费记录不存在"));
        }
        return ResponseEntity.ok(ApiResponse.success(record));
    }
}
