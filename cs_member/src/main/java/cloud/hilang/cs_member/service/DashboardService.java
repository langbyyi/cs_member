package cloud.hilang.cs_member.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 仪表盘服务接�? *
 * @author HiLang Cloud Team
 * @since 2025-01-18
 */
public interface DashboardService {

    /**
     * 获取仪表盘统计数�?     * 支持角色差异化数据返�?     *
     * @return 统计数据
     */
    Map<String, Object> getDashboardStats();

    /**
     * 获取营收趋势数据
     *
     * @param period 统计周期（天数）
     * @return 营收趋势数据
     */
    Map<String, Object> getRevenueTrend(Integer period);

    /**
     * 获取会员等级分布
     *
     * @return 会员等级分布数据
     */
    List<Map<String, Object>> getMemberLevelDistribution();

    /**
     * 获取支付方式统计
     *
     * @return 支付方式统计数据
     */
    Map<String, Object> getPaymentMethodStats();

    /**
     * 获取消费时段分布
     *
     * @return 消费时段分布数据
     */
    Map<String, Object> getConsumptionTimeDistribution();

    /**
     * 获取最新动�?     *
     * @return 最新动态列�?     */
    List<Map<String, Object>> getRecentActivities();

    /**
     * 获取业务统计数据
     *
     * @param startDate 开始日�?     * @param endDate   结束日期
     * @param storeId   门店ID（可选）
     * @return 业务统计数据
     */
    Map<String, Object> getBusinessStatistics(LocalDate startDate, LocalDate endDate, Long storeId);

    /**
     * 获取会员增长趋势
     *
     * @param startDate 开始日�?     * @param endDate   结束日期
     * @param storeId   门店ID（可选）
     * @return 会员增长趋势数据
     */
    Map<String, Object> getMemberGrowth(LocalDate startDate, LocalDate endDate, Long storeId);

    /**
     * 获取实时数据统计
     *
     * @return 实时统计数据
     */
    Map<String, Object> getRealTimeStats();
}