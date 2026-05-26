package cloud.hilang.cs_member.service;

import cloud.hilang.cs_member.mapper.ConsumptionRecordMapper;
import cloud.hilang.cs_member.mapper.MemberMapper;
import cloud.hilang.cs_member.mapper.PointsRecordMapper;
import cloud.hilang.cs_member.mapper.SysUserMapper;
import cloud.hilang.cs_member.entity.Member;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatisticsService {

    private final ConsumptionRecordMapper consumptionRecordMapper;
    private final MemberMapper memberMapper;
    private final PointsRecordMapper pointsRecordMapper;
    private final SysUserMapper sysUserMapper;

    public Map<String, Object> getBusinessStatistics(LocalDate startDate, LocalDate endDate, Integer storeId) {
        Map<String, Object> result = new HashMap<>();
        try {
            LocalDateTime start = startDate != null ? startDate.atStartOfDay() : null;
            LocalDateTime end = endDate != null ? endDate.atTime(23, 59, 59) : null;
            Long sid = storeId != null ? storeId.longValue() : null;
            Map<String, Object> bizStats = consumptionRecordMapper.getBusinessStatistics(start, end, sid);
            if (bizStats != null) result.putAll(bizStats);
        } catch (Exception e) {
            log.warn("获取消费统计失败", e);
        }
        result.put("totalMembers", memberMapper.selectCount(null));
        result.put("totalUsers", sysUserMapper.selectCount(null));
        return result;
    }

    public Map<String, Object> getMemberStatistics(LocalDate startDate, LocalDate endDate, Integer storeId) {
        Map<String, Object> result = new HashMap<>();
        long totalMembers = memberMapper.selectCount(null);
        result.put("totalMembers", totalMembers);

        // 计算新增会员数（基于时间范围）
        if (startDate != null) {
            QueryWrapper<Member> newMemberWrapper = new QueryWrapper<>();
            newMemberWrapper.ge("created_time", startDate.atStartOfDay());
            if (endDate != null) newMemberWrapper.le("created_time", endDate.atTime(23, 59, 59));
            result.put("newMembers", memberMapper.selectCount(newMemberWrapper));
        } else {
            result.put("newMembers", 0);
        }

        // 活跃会员（有消费记录的）
        try {
            LocalDateTime start = startDate != null ? startDate.atStartOfDay() : null;
            LocalDateTime end = endDate != null ? endDate.atTime(23, 59, 59) : null;
            Long sid = storeId != null ? storeId.longValue() : null;
            Long activeMembers = consumptionRecordMapper.getActiveMemberCount(start, end, sid);
            result.put("activeMembers", activeMembers != null ? activeMembers : 0);
        } catch (Exception e) {
            result.put("activeMembers", 0);
        }

        return result;
    }

    public Map<String, Object> getProductStatistics(LocalDate startDate, LocalDate endDate, Integer storeId) {
        Map<String, Object> result = new HashMap<>();
        try {
            LocalDateTime start = startDate != null ? startDate.atStartOfDay() : null;
            LocalDateTime end = endDate != null ? endDate.atTime(23, 59, 59) : null;
            Long sid = storeId != null ? storeId.longValue() : null;
            Map<String, Object> bizStats = consumptionRecordMapper.getBusinessStatistics(start, end, sid);
            if (bizStats != null) result.putAll(bizStats);
        } catch (Exception e) {
            log.warn("获取商品统计失败", e);
        }
        return result;
    }

    public Map<String, Object> getDashboardOverview(LocalDate startDate, LocalDate endDate, Integer storeId) {
        Map<String, Object> result = new HashMap<>();
        result.put("totalMembers", memberMapper.selectCount(null));
        result.put("totalUsers", sysUserMapper.selectCount(null));
        try {
            LocalDateTime start = startDate != null ? startDate.atStartOfDay() : null;
            LocalDateTime end = endDate != null ? endDate.atTime(23, 59, 59) : null;
            Long sid = storeId != null ? storeId.longValue() : null;
            Map<String, Object> bizStats = consumptionRecordMapper.getBusinessStatistics(start, end, sid);
            if (bizStats != null) result.putAll(bizStats);
        } catch (Exception e) {
            log.warn("获取仪表板数据失败", e);
        }
        return result;
    }

    public Map<String, Object> getEmployeeStatistics(LocalDate startDate, LocalDate endDate, Integer storeId) {
        Map<String, Object> result = new HashMap<>();
        result.put("totalEmployees", sysUserMapper.selectCount(null));
        return result;
    }

    public Map<String, Object> getMonthlyTrend(int months) {
        Map<String, Object> result = new HashMap<>();
        LocalDateTime endTime = LocalDateTime.now();
        LocalDateTime startTime = endTime.minusMonths(months).withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);

        List<Map<String, Object>> revenueTrend = consumptionRecordMapper.getMonthlyRevenue(startTime, endTime);
        List<Map<String, Object>> memberTrend = consumptionRecordMapper.getMonthlyMemberGrowth(startTime, endTime);
        List<Map<String, Object>> pointsTrend = pointsRecordMapper.getMonthlyPointsStats(startTime, endTime);

        result.put("revenueTrend", revenueTrend);
        result.put("memberTrend", memberTrend);
        result.put("pointsTrend", pointsTrend);
        return result;
    }
}
