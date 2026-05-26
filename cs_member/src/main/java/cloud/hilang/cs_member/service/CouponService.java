package cloud.hilang.cs_member.service;

import cloud.hilang.cs_member.entity.Coupon;
import com.github.pagehelper.PageInfo;

import java.time.LocalDate;
import java.util.Map;

/**
 * 优惠券服务接口
 */
public interface CouponService {

    /**
     * 分页查询优惠券列表
     *
     * @param pageNum        页码
     * @param pageSize       每页大小
     * @param name           优惠券名
     * @param couponType     优惠券类型
     * @param status         状态
     * @param validStartTime 有效开始时间
     * @param validEndTime   有效结束时间
     * @return 优惠券分页对象
     */
    PageInfo<Coupon> getCouponListPage(int pageNum, int pageSize, String name, Integer couponType, Integer status, LocalDate validStartTime, LocalDate validEndTime);

    /**
     * 创建优惠券
     *
     * @param coupon 优惠券信息
     * @return 是否成功
     */
    boolean createCoupon(Coupon coupon);

    /**
     * 更新优惠券
     *
     * @param coupon 优惠券信息
     * @return 是否成功
     */
    boolean updateCoupon(Coupon coupon);

    /**
     * 删除优惠券
     *
     * @param id 优惠券ID
     * @return 是否成功
     */
    boolean deleteCoupon(Long id);

    /**
     * 获取优惠券详情
     *
     * @param id 优惠券ID
     * @return 优惠券信息
     */
    Coupon getCouponById(Long id);

    /**
     * 更新优惠券状态
     *
     * @param id     优惠券ID
     * @param status 状态
     * @return 是否成功
     */
    boolean updateCouponStatus(Long id, Integer status);

    /**
     * 获取全局优惠券统计信息
     *
     * @return 统计数据
     */
    Map<String, Object> getGlobalCouponStatistics();
}
