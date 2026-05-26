package cloud.hilang.cs_member.service;

import cloud.hilang.cs_member.dto.EmailVerificationRequest;
import cloud.hilang.cs_member.dto.EmailVerifyRequest;
import cloud.hilang.cs_member.entity.EmailVerification;

/**
 * 邮箱验证服务接口
 *
 * @author HiLang Cloud Team
 * @since 2025-01-17
 */
public interface EmailVerificationService {

    /**
     * 发送邮箱验证码
     *
     * @param request 验证码请求
     * @param ipAddress 请求IP地址
     * @param userAgent 用户代理信息
     * @return 是否发送成功
     */
    boolean sendEmailCode(EmailVerificationRequest request, String ipAddress, String userAgent);

    /**
     * 验证邮箱验证码
     *
     * @param request 验证请求
     * @return 是否验证成功
     */
    boolean verifyEmailCode(EmailVerifyRequest request);

    /**
     * 验证邮箱验证码（内部使用，不标记为已验证）
     *
     * @param email 邮箱地址
     * @param userType 用户类型
     * @param verificationType 验证类型
     * @param verificationCode 验证码
     * @return 是否验证成功
     */
    boolean validateEmailCode(String email, String userType, String verificationType, String verificationCode);

    /**
     * 清理过期的验证码记录
     */
    void cleanExpiredRecords();

    /**
     * 删除指定邮箱的所有验证码记录
     *
     * @param email 邮箱地址
     * @param userType 用户类型
     * @param verificationType 验证类型
     */
    void deleteAllRecords(String email, String userType, String verificationType);

    /**
     * 获取最新的验证码记录
     *
     * @param email 邮箱地址
     * @param userType 用户类型
     * @param verificationType 验证类型
     * @return 验证码记录
     */
    EmailVerification getLatestRecord(String email, String userType, String verificationType);
}