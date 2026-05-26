package cloud.hilang.cs_member.mapper;

import cloud.hilang.cs_member.entity.Member;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 会员Mapper接口
 *
 * @author HiLang Cloud Team
 * @since 2025-01-14
 */
@Mapper
public interface MemberMapper extends BaseMapper<Member> {

    /**
     * 根据手机号查询会员
     *
     * @param phone 手机号
     * @return 会员信息
     */
    Member selectByPhone(@Param("phone") String phone);

    /**
     * 根据手机号或会员号查找会员
     *
     * @param phoneOrMemberNo 手机号或会员号
     * @return 会员信息
     */
    Member findByPhoneOrMemberNo(@Param("phoneOrMemberNo") String phoneOrMemberNo);

    /**
     * 根据ID查找会员
     *
     * @param id 会员ID
     * @return 会员信息
     */
    Member findById(@Param("id") Long id);

    /**
     * 根据邮箱查询会员
     *
     * @param email 邮箱
     * @return 会员信息
     */
    Member selectByEmail(@Param("email") String email);

    /**
     * 根据会员编号查询会员
     *
     * @param memberNo 会员编号
     * @return 会员信息
     */
    Member selectByMemberNo(@Param("memberNo") String memberNo);

    /**
     * 查询会员列表
     *
     * @param name 会员姓名（模糊查询）
     * @param phone 手机号（模糊查询）
     * @param memberNo 会员编号
     * @param status 账户状态
     * @param storeName 门店名称（模糊查询）
     * @param gender 性别
     * @param startTime 注册开始时间
     * @param endTime 注册结束时间
     * @return 会员列表
     */
    List<Member> selectMemberList(@Param("name") String name,
                                   @Param("phone") String phone,
                                   @Param("memberNo") String memberNo,
                                   @Param("status") Integer status,
                                   @Param("storeName") String storeName,
                                   @Param("gender") Integer gender,
                                   @Param("startTime") LocalDateTime startTime,
                                   @Param("endTime") LocalDateTime endTime);

    /**
     * 更新会员登录信息
     *
     * @param id 会员ID
     * @param loginTime 登录时间
     * @param loginIp 登录IP
     * @return 更新结果
     */
    int updateLoginInfo(@Param("id") Long id,
                       @Param("loginTime") LocalDateTime loginTime,
                       @Param("loginIp") String loginIp);

    /**
     * 更新会员状态
     *
     * @param id 会员ID
     * @param status 新状态
     * @param updatedBy 更新人
     * @return 更新结果
     */
    int updateStatus(@Param("id") Long id,
                    @Param("status") Integer status,
                    @Param("updatedBy") String updatedBy);

    /**
     * 更新会员积分
     *
     * @param id 会员ID
     * @param points 当前积分
     * @param totalPoints 累计积分
     * @return 更新结果
     */
    int updatePoints(@Param("id") Long id,
                    @Param("points") Integer points,
                    @Param("totalPoints") Integer totalPoints);

    
    /**
     * 根据状态查询会员数量
     *
     * @param status 账户状态
     * @return 会员数量
     */
    Long countByStatus(@Param("status") Integer status);

    /**
     * 查询指定时间段内注册的会员数量
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 会员数量
     */
    Long countByRegisterTime(@Param("startTime") LocalDateTime startTime,
                            @Param("endTime") LocalDateTime endTime);

    /**
     * 查询即将生日的会员（未来30天内）
     *
     * @return 会员列表
     */
    List<Member> selectUpcomingBirthdayMembers();

    /**
     * 查询长期未活跃会员（超过90天未登录）
     *
     * @return 会员列表
     */
    List<Member> selectInactiveMembers();

    
    /**
     * 验证手机号是否存在（包括已删除的记录）
     *
     * @param phone 手机号
     * @return 会员信息
     */
    Member selectPhoneExists(@Param("phone") String phone);

    /**
     * 验证邮箱是否存在（包括已删除的记录）
     *
     * @param email 邮箱
     * @return 会员信息
     */
    Member selectEmailExists(@Param("email") String email);

    /**
     * 更新会员信息
     *
     * @param member 会员信息
     * @return 更新结果
     */
    int updateMemberInfo(@Param("member") Member member);

    /**
     * 更新会员卡积分
     *
     * @param memberCardId 会员卡ID
     * @param points 当前积分
     * @param totalPoints 累计积分
     * @param updateTime 更新时间
     * @return 更新结果
     */
    int updateMemberCardPoints(@Param("memberId") Long memberId,
                               @Param("points") Integer points,
                               @Param("totalPoints") Integer totalPoints,
                               @Param("updateTime") LocalDateTime updateTime);

    /**
     * 根据门店统计会员数量
     *
     * @param storeId 门店ID
     * @return 会员数量
     */
    Long countByStore(@Param("storeId") Long storeId);

    /**
     * 根据日期范围和门店统计会员数量
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param storeId 门店ID
     * @return 会员数量
     */
    Long countByDateRange(@Param("startTime") LocalDateTime startTime,
                          @Param("endTime") LocalDateTime endTime,
                          @Param("storeId") Long storeId);

    /**
     * 获取会员增长数据
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param storeId 门店ID
     * @return 会员增长数据列表
     */
    List<Map<String, Object>> getMemberGrowthData(@Param("startDate") LocalDateTime startDate,
                                     @Param("endDate") LocalDateTime endDate,
                                     @Param("storeId") Long storeId);
}