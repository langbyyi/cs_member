package cloud.hilang.cs_member.mapper;

import cloud.hilang.cs_member.entity.MemberCard;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 会员卡Mapper接口
 *
 * @author HiLang Cloud Team
 * @since 2025-01-17
 */
@Mapper
public interface MemberCardMapper extends BaseMapper<MemberCard> {

    /**
     * 根据会员ID查询会员卡
     *
     * @param memberId 会员ID
     * @return 会员卡信息
     */
    MemberCard selectByMemberId(@Param("memberId") Long memberId);

    /**
     * 根据会员编号查询会员卡
     *
     * @param memberNo 会员编号
     * @return 会员卡信息
     */
    MemberCard selectByMemberNo(@Param("memberNo") String memberNo);

    /**
     * 创建会员卡
     *
     * @param memberCard 会员卡信息
     * @return 插入结果
     */
    int insertMemberCard(@Param("memberCard") MemberCard memberCard);
}