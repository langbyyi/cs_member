package cloud.hilang.cs_member.mapper;

import cloud.hilang.cs_member.entity.MemberCoupon;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MemberCouponMapper extends BaseMapper<MemberCoupon> {

    List<MemberCoupon> selectByMemberId(@Param("memberId") Long memberId);

    List<MemberCoupon> selectByMemberIdFiltered(@Param("memberId") Long memberId,
                                                 @Param("status") Integer status,
                                                 @Param("couponType") Integer couponType);

    List<MemberCoupon> selectAvailableByMemberId(@Param("memberId") Long memberId);

    int countByMemberId(@Param("memberId") Long memberId);

    int countAvailableByMemberId(@Param("memberId") Long memberId);

    int batchInsert(@Param("list") List<MemberCoupon> list);
}
