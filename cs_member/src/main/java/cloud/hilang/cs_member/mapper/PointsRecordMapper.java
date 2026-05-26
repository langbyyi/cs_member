package cloud.hilang.cs_member.mapper;

import cloud.hilang.cs_member.entity.PointsRecord;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 积分记录Mapper接口
 *
 * @author HiLang Cloud Team
 * @since 2025-01-17
 */
@Mapper
public interface PointsRecordMapper extends BaseMapper<PointsRecord> {

    /**
     * 根据会员ID查询积分记录
     *
     * @param memberId 会员ID
     * @return 积分记录列表
     */
    List<PointsRecord> selectByMemberId(@Param("memberId") Long memberId);

    /**
     * 分页查询积分记录列表（管理员视角，包含会员信息）
     *
     * @param memberNo 会员编号（模糊查询）
     * @param memberName 会员姓名（模糊查询）
     * @param changeType 积分变动类型
     * @param startDate 开始时间
     * @param endDate 结束时间
     * @return 积分记录列表
     */
    List<PointsRecord> selectPointsRecordList(
        @Param("memberNo") String memberNo,
        @Param("memberName") String memberName,
        @Param("changeType") String changeType,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    /**
     * 创建积分记录
     *
     * @param pointsRecord 积分记录
     * @return 插入结果
     */
    int insertPointsRecord(@Param("pointsRecord") PointsRecord pointsRecord);

    /**
     * 查询会员当前总积分
     *
     * @param memberId 会员ID
     * @return 当前总积分
     */
    Integer selectCurrentPointsByMemberId(@Param("memberId") Long memberId);

    /**
     * 根据过期时间查询过期积分记录
     *
     * @return 过期积分记录列表
     */
    List<PointsRecord> selectExpiredPoints();

    /**
     * 获取积分统计数据
     *
     * @return 统计数据
     */
    java.util.Map<String, Object> selectPointsStatistics();

    /**
     * 按月统计积分发放/使用
     */
    java.util.List<java.util.Map<String, Object>> getMonthlyPointsStats(@Param("startTime") LocalDateTime startTime,
                                                                         @Param("endTime") LocalDateTime endTime);
}