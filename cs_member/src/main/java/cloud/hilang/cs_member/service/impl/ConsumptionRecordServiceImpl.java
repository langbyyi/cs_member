package cloud.hilang.cs_member.service.impl;

import cloud.hilang.cs_member.entity.ConsumptionRecord;
import cloud.hilang.cs_member.mapper.ConsumptionRecordMapper;
import cloud.hilang.cs_member.service.ConsumptionRecordService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 消费记录服务实现类
 *
 * @author HiLang Cloud Team
 * @since 2025-01-24
 */
@Service
@RequiredArgsConstructor
public class ConsumptionRecordServiceImpl implements ConsumptionRecordService {

    private final ConsumptionRecordMapper consumptionRecordMapper;

    @Override
    public PageInfo<ConsumptionRecord> getConsumptionRecordListPage(int pageNum, int pageSize, String memberNo, String memberName, String consumptionNo, LocalDateTime startDate, LocalDateTime endDate, String paymentMethod) {
        PageHelper.startPage(pageNum, pageSize);
        List<ConsumptionRecord> list = consumptionRecordMapper.selectConsumptionRecordList(memberNo, memberName, consumptionNo, startDate, endDate, paymentMethod);
        return new PageInfo<>(list);
    }

    @Override
    public Map<String, Object> getConsumptionStatistics() {
        // 复用Mapper中已有的统计方法
        Map<String, Object> stats = new HashMap<>();
        
        // 获取今日统计
        LocalDateTime todayStart = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        LocalDateTime todayEnd = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59);
        Map<String, Object> dailyStats = consumptionRecordMapper.getDailyStats(todayStart, todayEnd, null);
        
        // 获取总统计
        Map<String, Object> totalStats = consumptionRecordMapper.getBusinessStatistics(null, null, null);
        
        stats.put("todayOrderCount", dailyStats != null ? dailyStats.get("orderCount") : 0);
        stats.put("todayRevenue", dailyStats != null ? dailyStats.get("revenue") : 0);
        stats.put("totalOrderCount", totalStats != null ? totalStats.get("totalOrders") : 0);
        stats.put("totalRevenue", totalStats != null ? totalStats.get("totalRevenue") : 0);
        
        return stats;
    }
}
