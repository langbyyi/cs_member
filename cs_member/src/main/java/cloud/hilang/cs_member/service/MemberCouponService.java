package cloud.hilang.cs_member.service;

import cloud.hilang.cs_member.entity.Coupon;
import cloud.hilang.cs_member.entity.MemberCoupon;
import cloud.hilang.cs_member.mapper.CouponMapper;
import cloud.hilang.cs_member.mapper.MemberCouponMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberCouponService {

    private final MemberCouponMapper memberCouponMapper;
    private final CouponMapper couponMapper;

    public PageInfo<MemberCoupon> getMemberCoupons(Long memberId, int pageNum, int pageSize,
                                                     Integer status, Integer couponType) {
        PageHelper.startPage(pageNum, pageSize);
        List<MemberCoupon> coupons;
        if (status != null || couponType != null) {
            coupons = memberCouponMapper.selectByMemberIdFiltered(memberId, status, couponType);
        } else {
            coupons = memberCouponMapper.selectByMemberId(memberId);
        }
        return new PageInfo<>(coupons);
    }

    public List<MemberCoupon> getAvailableCoupons(Long memberId) {
        return memberCouponMapper.selectAvailableByMemberId(memberId);
    }

    public int countByMemberId(Long memberId) {
        return memberCouponMapper.countByMemberId(memberId);
    }

    public boolean useCoupon(Long memberCouponId, Long memberId) {
        MemberCoupon mc = memberCouponMapper.selectById(memberCouponId);
        if (mc == null || !mc.getMemberId().equals(memberId) || mc.getUseTime() != null) {
            return false;
        }
        mc.setUseTime(LocalDateTime.now());
        return memberCouponMapper.updateById(mc) > 0;
    }

    public int issueCouponToMembers(Long couponId, List<Long> memberIds) {
        Coupon coupon = couponMapper.selectById(couponId);
        if (coupon == null) return 0;

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expireTime = coupon.getValidEndTime() != null ? coupon.getValidEndTime() : now.plusMonths(3);

        List<MemberCoupon> list = memberIds.stream().map(memberId -> {
            MemberCoupon mc = new MemberCoupon();
            mc.setMemberId(memberId);
            mc.setCouponId(couponId);
            mc.setCouponCode("COUPON-" + couponId + "-" + memberId);
            mc.setReceiveTime(now);
            mc.setExpireTime(expireTime);
            return mc;
        }).toList();

        memberCouponMapper.batchInsert(list);
        return list.size();
    }
}
