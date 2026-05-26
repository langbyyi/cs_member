package cloud.hilang.cs_member.service.impl;

import cloud.hilang.cs_member.entity.Coupon;
import cloud.hilang.cs_member.entity.MemberCoupon;
import cloud.hilang.cs_member.mapper.CouponMapper;
import cloud.hilang.cs_member.mapper.MemberCouponMapper;
import cloud.hilang.cs_member.service.CouponService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 优惠券服务实现类
 *
 * @author HiLang Cloud Team
 * @since 2025-01-24
 */
@Service
@RequiredArgsConstructor
public class CouponServiceImpl implements CouponService {

    private final CouponMapper couponMapper;
    private final MemberCouponMapper memberCouponMapper;

    @Override
    public PageInfo<Coupon> getCouponListPage(int pageNum, int pageSize, String name, Integer couponType, Integer status, LocalDate validStartTime, LocalDate validEndTime) {
        PageHelper.startPage(pageNum, pageSize);
        List<Coupon> list = couponMapper.selectCouponList(name, couponType, status, validStartTime, validEndTime);
        return new PageInfo<>(list);
    }

    @Override
    public boolean createCoupon(Coupon coupon) {
        return couponMapper.insert(coupon) > 0;
    }

    @Override
    public boolean updateCoupon(Coupon coupon) {
        return couponMapper.updateById(coupon) > 0;
    }

    @Override
    public boolean deleteCoupon(Long id) {
        return couponMapper.deleteById(id) > 0;
    }

    @Override
    public Coupon getCouponById(Long id) {
        return couponMapper.selectById(id);
    }

    @Override
    public boolean updateCouponStatus(Long id, Integer status) {
        Coupon coupon = new Coupon();
        coupon.setId(id);
        coupon.setStatus(status);
        return couponMapper.updateById(coupon) > 0;
    }

    @Override
    public Map<String, Object> getGlobalCouponStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        // 统计总数
        Long totalCoupons = couponMapper.selectCount(null);
        
        // 统计有效优惠券
        QueryWrapper<Coupon> activeWrapper = new QueryWrapper<>();
        activeWrapper.eq("status", 1);
        Long activeCoupons = couponMapper.selectCount(activeWrapper);
        
        stats.put("totalCoupons", totalCoupons);
        stats.put("activeCoupons", activeCoupons);

        // 已领取数量
        QueryWrapper<MemberCoupon> usedWrapper = new QueryWrapper<>();
        usedWrapper.isNotNull("use_time");
        Long usedCoupons = memberCouponMapper.selectCount(usedWrapper);
        stats.put("usedCoupons", usedCoupons);

        // 已发放数量
        Long issuedCoupons = memberCouponMapper.selectCount(null);
        stats.put("issuedCoupons", issuedCoupons);
        stats.put("totalSaved", 0);
        
        return stats;
    }
}
