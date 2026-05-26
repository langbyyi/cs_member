package cloud.hilang.cs_member.service;

import cloud.hilang.cs_member.entity.PointsRecord;
import cloud.hilang.cs_member.mapper.PointsRecordMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 积分记录管理服务类
 *
 * @author HiLang Cloud Team
 * @since 2025-01-24
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PointsRecordService {

    private final PointsRecordMapper pointsRecordMapper;

    /**
     * 分页查询积分记录列表（管理员视角，包含会员信息）
     *
     * @param pageNum 页码，从1开始
     * @param pageSize 每页大小
     * @param memberNo 会员编号（模糊查询）
     * @param memberName 会员姓名（模糊查询）
     * @param changeType 积分变动类型
     * @param startDate 开始时间
     * @param endDate 结束时间
     * @return 分页结果
     */
    public PageInfo<PointsRecord> getPointsRecordListPage(int pageNum, int pageSize,
                                                         String memberNo, String memberName,
                                                         String changeType,
                                                         LocalDateTime startDate, LocalDateTime endDate) {
        log.debug("开始分页查询积分记录列表，页码：{}，页大小：{}", pageNum, pageSize);

        // 使用PageHelper.startPage设置分页参数
        PageHelper.startPage(pageNum, pageSize);

        // 执行查询 - 返回包含会员信息的积分记录
        List<PointsRecord> pointsRecordList = pointsRecordMapper.selectPointsRecordList(
                memberNo, memberName, changeType, startDate, endDate);

        // 使用PageInfo包装查询结果，获取完整的分页信息
        PageInfo<PointsRecord> pageInfo = new PageInfo<>(pointsRecordList);

        log.debug("分页查询完成，总记录数：{}，总页数：{}，当前页：{}",
                pageInfo.getTotal(), pageInfo.getPages(), pageInfo.getPageNum());

        return pageInfo;
    }

    /**
     * 获取积分统计数据
     *
     * @return 统计数据
     */
    public Map<String, Object> getPointsStatistics() {
        log.debug("获取积分统计数据");

        Map<String, Object> statistics = pointsRecordMapper.selectPointsStatistics();

        // 计算总记录数（这里简化处理，实际可以从数据库查询）
        statistics.put("totalRecords", 0);

        log.debug("积分统计数据获取完成：{}", statistics);
        return statistics;
    }

    /**
     * 根据会员ID查询积分记录
     *
     * @param memberId 会员ID
     * @return 积分记录列表
     */
    public List<PointsRecord> getPointsRecordsByMemberId(Long memberId) {
        log.debug("查询会员积分记录，memberId={}", memberId);
        return pointsRecordMapper.selectByMemberId(memberId);
    }

    /**
     * 创建积分记录
     *
     * @param pointsRecord 积分记录
     * @return 插入结果
     */
    public int createPointsRecord(PointsRecord pointsRecord) {
        log.debug("创建积分记录，memberId={}, pointsChange={}",
                pointsRecord.getMemberId(), pointsRecord.getPointsChange());
        return pointsRecordMapper.insertPointsRecord(pointsRecord);
    }

    /**
     * 获取会员当前总积分
     *
     * @param memberId 会员ID
     * @return 当前总积分
     */
    public Integer getCurrentPointsByMemberId(Long memberId) {
        log.debug("查询会员当前积分，memberId={}", memberId);
        return pointsRecordMapper.selectCurrentPointsByMemberId(memberId);
    }
}