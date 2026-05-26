package cloud.hilang.cs_member.mapper;

import cloud.hilang.cs_member.entity.ConsumptionRecord;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 消费记录Mapper接口
 *
 * @author HiLang Cloud Team
 * @since 2025-01-18
 */
@Mapper
public interface ConsumptionRecordMapper extends BaseMapper<ConsumptionRecord> {

    /**
     * 获取每日统计数据
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @param storeId   门店ID（可选）
     * @return 统计数据
     */
    Map<String, Object> getDailyStats(@Param("startTime") LocalDateTime startTime,
                                       @Param("endTime") LocalDateTime endTime,
                                       @Param("storeId") Long storeId);

    /**
     * 获取支付方式统计
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @param storeId   门店ID（可选）
     * @return 支付方式统计
     */
    List<Map<String, Object>> getPaymentMethodStats(@Param("startTime") LocalDateTime startTime,
                                                     @Param("endTime") LocalDateTime endTime,
                                                     @Param("storeId") Long storeId);

    /**
     * 获取消费时段统计
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @param storeId   门店ID（可选）
     * @return 消费时段统计
     */
    List<Map<String, Object>> getConsumptionTimeStats(@Param("startTime") LocalDateTime startTime,
                                                      @Param("endTime") LocalDateTime endTime,
                                                      @Param("storeId") Long storeId);

    /**
     * 获取最新动态
     *
     * @param storeId 门店ID（可选）
     * @param limit   限制数量
     * @return 最新动态列表
     */
    List<Map<String, Object>> getRecentActivities(@Param("storeId") Long storeId,
                                                  @Param("limit") Integer limit);

    /**
     * 获取业务统计数据
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @param storeId   门店ID（可选）
     * @return 业务统计数据
     */
    Map<String, Object> getBusinessStatistics(@Param("startTime") LocalDateTime startTime,
                                               @Param("endTime") LocalDateTime endTime,
                                               @Param("storeId") Long storeId);

    /**
     * 根据日期范围统计订单数量
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @param storeId   门店ID（可选）
     * @return 订单数量
     */
    Long countByDateRange(@Param("startTime") LocalDateTime startTime,
                          @Param("endTime") LocalDateTime endTime,
                          @Param("storeId") Long storeId);

    /**
     * 根据门店统计订单数量
     *
     * @param storeId 门店ID（可选）
     * @return 订单数量
     */
    Long countByStore(@Param("storeId") Long storeId);

    /**
     * 根据日期范围统计营收
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @param storeId   门店ID（可选）
     * @return 营收金额
     */
    BigDecimal getRevenueByDateRange(@Param("startTime") LocalDateTime startTime,
                                     @Param("endTime") LocalDateTime endTime,
                                     @Param("storeId") Long storeId);

    /**
     * 根据门店统计总营收
     *
     * @param storeId 门店ID（可选）
     * @return 总营收
     */
    BigDecimal getRevenueByStore(@Param("storeId") Long storeId);

    /**
     * 获取活跃会员数量（指定时间范围内有消费的会员）
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @param storeId   门店ID（可选）
     * @return 活跃会员数量
     */
    Long getActiveMemberCount(@Param("startTime") LocalDateTime startTime,
                             @Param("endTime") LocalDateTime endTime,
                             @Param("storeId") Long storeId);

    /**
     * 分页查询消费记录列表
     *
     * @param memberNo      会员编号
     * @param memberName    会员姓名
     * @param consumptionNo 消费单号
     * @param startDate     开始时间
     * @param endDate       结束时间
     * @param paymentMethod 支付方式
     * @return 消费记录列表
     */
    List<ConsumptionRecord> selectConsumptionRecordList(@Param("memberNo") String memberNo,
                                                        @Param("memberName") String memberName,
                                                        @Param("consumptionNo") String consumptionNo,
                                                        @Param("startDate") LocalDateTime startDate,
                                                        @Param("endDate") LocalDateTime endDate,
                                                        @Param("paymentMethod") String paymentMethod);

    /**
     * 按月统计营收数据
     */
    List<Map<String, Object>> getMonthlyRevenue(@Param("startTime") LocalDateTime startTime,
                                                 @Param("endTime") LocalDateTime endTime);

    /**
     * 按月统计新增消费会员数
     */
    List<Map<String, Object>> getMonthlyMemberGrowth(@Param("startTime") LocalDateTime startTime,
                                                      @Param("endTime") LocalDateTime endTime);
}