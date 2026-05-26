package cloud.hilang.cs_member.service;

import cloud.hilang.cs_member.entity.ConsumptionRecord;
import com.github.pagehelper.PageInfo;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 消费记录服务接口
 *
 * @author HiLang Cloud Team
 * @since 2025-01-24
 */
public interface ConsumptionRecordService {

    /**
     * 分页查询消费记录列表
     *
     * @param pageNum       页码
     * @param pageSize      每页大小
     * @param memberNo      会员编号
     * @param memberName    会员姓名
     * @param consumptionNo 消费单号
     * @param startDate     开始时间
     * @param endDate       结束时间
     * @param paymentMethod 支付方式
     * @return 消费记录分页对象
     */
    PageInfo<ConsumptionRecord> getConsumptionRecordListPage(int pageNum, int pageSize, String memberNo, String memberName, String consumptionNo, LocalDateTime startDate, LocalDateTime endDate, String paymentMethod);

    /**
     * 获取消费统计数据
     *
     * @return 统计数据
     */
    Map<String, Object> getConsumptionStatistics();
}
