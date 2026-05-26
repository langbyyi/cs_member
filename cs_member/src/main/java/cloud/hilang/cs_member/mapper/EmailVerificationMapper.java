package cloud.hilang.cs_member.mapper;

import cloud.hilang.cs_member.entity.EmailVerification;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 邮箱验证Mapper接口
 *
 * @author HiLang Cloud Team
 * @since 2025-01-17
 */
@Mapper
public interface EmailVerificationMapper extends BaseMapper<EmailVerification> {

    /**
     * 根据邮箱、用户类型和验证类型查询未验证的有效验证码记录
     *
     * @param email 邮箱地址
     * @param userType 用户类型
     * @param verificationType 验证类型
     * @param currentTime 当前时间
     * @return 验证码记录列表
     */
    List<EmailVerification> findValidUnverifiedRecords(
            @Param("email") String email,
            @Param("userType") String userType,
            @Param("verificationType") String verificationType,
            @Param("currentTime") LocalDateTime currentTime
    );

    /**
     * 根据邮箱、用户类型、验证类型和验证码查询有效的验证码记录
     *
     * @param email 邮箱地址
     * @param userType 用户类型
     * @param verificationType 验证类型
     * @param verificationCode 验证码
     * @param currentTime 当前时间
     * @return 验证码记录
     */
    EmailVerification findValidRecordByCode(
            @Param("email") String email,
            @Param("userType") String userType,
            @Param("verificationType") String verificationType,
            @Param("verificationCode") String verificationCode,
            @Param("currentTime") LocalDateTime currentTime
    );

    /**
     * 将指定邮箱、用户类型和验证类型的所有未验证记录标记为已验证（验证码使用后失效）
     *
     * @param email 邮箱地址
     * @param userType 用户类型
     * @param verificationType 验证类型
     * @param verificationCode 验证码
     * @param verifiedTime 验证时间
     * @return 影响的行数
     */
    int markAsVerified(
            @Param("email") String email,
            @Param("userType") String userType,
            @Param("verificationType") String verificationType,
            @Param("verificationCode") String verificationCode,
            @Param("verifiedTime") LocalDateTime verifiedTime
    );

    /**
     * 删除过期的验证码记录
     *
     * @param expireTime 过期时间
     * @return 删除的行数
     */
    int deleteExpiredRecords(@Param("expireTime") LocalDateTime expireTime);

    /**
     * 统计指定邮箱在指定时间窗口内发送验证码的次数
     *
     * @param email 邮箱地址
     * @param userType 用户类型
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 发送次数
     */
    int countSendAttempts(
            @Param("email") String email,
            @Param("userType") String userType,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    /**
     * 删除指定邮箱的所有验证码记录
     *
     * @param email 邮箱地址
     * @param userType 用户类型
     * @param verificationType 验证类型
     * @return 删除的行数
     */
    int deleteByEmailAndType(
            @Param("email") String email,
            @Param("userType") String userType,
            @Param("verificationType") String verificationType
    );
}