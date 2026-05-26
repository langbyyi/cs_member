package cloud.hilang.cs_member.mapper;

import cloud.hilang.cs_member.entity.Coupon;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * 优惠券Mapper接口
 *
 * @author HiLang Cloud Team
 * @since 2025-01-24
 */
@Mapper
public interface CouponMapper extends BaseMapper<Coupon> {

    /**
     * 查询优惠券列表
     *
     * @param name          优惠券名称
     * @param couponType    优惠券类型
     * @param status        状态
     * @param validStartTime 有效开始时间
     * @param validEndTime   有效结束时间
     * @return 优惠券列表
     */
    List<Coupon> selectCouponList(@Param("name") String name,
                                  @Param("couponType") Integer couponType,
                                  @Param("status") Integer status,
                                  @Param("validStartTime") LocalDate validStartTime,
                                  @Param("validEndTime") LocalDate validEndTime);
}
