package cloud.hilang.cs_member.mapper;

import cloud.hilang.cs_member.entity.Store;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 门店Mapper接口
 *
 * @author HiLang Cloud Team
 * @since 2025-01-18
 */
@Mapper
public interface StoreMapper extends BaseMapper<Store> {

    /**
     * 查询所有营业中的门店
     *
     * @return 门店列表
     */
    @Select("SELECT * FROM store WHERE status = 1 ORDER BY store_no")
    List<Store> selectOpenStores();

    /**
     * 根据状态查询门店
     *
     * @param status 门店状态
     * @return 门店列表
     */
    @Select("SELECT * FROM store WHERE status = #{status} ORDER BY store_no")
    List<Store> selectByStatus(@Param("status") Integer status);

    /**
     * 根据日期范围统计门店数量
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 门店数量
     */
    @Select("SELECT COUNT(*) " +
            "FROM store " +
            "WHERE open_time BETWEEN #{startTime} AND #{endTime}")
    Long countByDateRange(@Param("startTime") LocalDateTime startTime,
                          @Param("endTime") LocalDateTime endTime);

    /**
     * 更新门店状态
     *
     * @param id 门店ID
     * @param status 状态
     * @return 影响行数
     */
    @Update("UPDATE store SET status = #{status} WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);
    
    /**
     * 更新门店的店长信息
     *
     * @param storeId 门店ID
     * @param managerId 店长ID，可以为null
     * @return 影响行数
     */
    @Update("UPDATE store SET manager_id = #{managerId} WHERE id = #{storeId}")
    int updateManager(@Param("storeId") Long storeId,
                     @Param("managerId") Long managerId);

    /**
     * 更新门店员工数
     *
     * @param storeId 门店ID
     * @param employeeCount 员工数量
     * @return 影响行数
     */
    @Update("UPDATE store SET employee_count = #{employeeCount} WHERE id = #{storeId}")
    int updateEmployeeCount(@Param("storeId") Long storeId,
                           @Param("employeeCount") Integer employeeCount);

    /**
     * 更新门店会员数
     *
     * @param storeId 门店ID
     * @param memberCount 会员数量
     * @return 影响行数
     */
    @Update("UPDATE store SET member_count = #{memberCount} WHERE id = #{storeId}")
    int updateMemberCount(@Param("storeId") Long storeId,
                         @Param("memberCount") Integer memberCount);

    /**
     * 统计指定门店的员工数量
     *
     * @param storeId 门店ID
     * @return 员工数量
     */
    @Select("SELECT COUNT(*) FROM sys_user WHERE store_id = #{storeId} AND status = 1")
    Integer countEmployeesByStoreId(@Param("storeId") Long storeId);

    /**
     * 统计指定门店的会员数量
     *
     * @param storeId 门店ID
     * @return 会员数量
     */
    @Select("SELECT COUNT(*) FROM member WHERE register_store_id = #{storeId}")
    Integer countMembersByStoreId(@Param("storeId") Long storeId);
}