package cloud.hilang.cs_member.service;

import cloud.hilang.cs_member.dto.PhoneValidateRequest;
import cloud.hilang.cs_member.dto.ForgetPasswordRequest;
import cloud.hilang.cs_member.dto.ResetPasswordRequest;
import cloud.hilang.cs_member.dto.ResetPasswordTokenResponse;

/**
 * 忘记密码服务接口
 *
 * @author HiLang Cloud Team
 * @since 2025-01-18
 */
public interface ForgetPasswordService {

    /**
     * 校验手机号格式
     * 无论手机号是否存在都通过，只做格式校验
     *
     * @param request 手机号校验请求
     * @return 是否校验通过
     */
    boolean validatePhoneFormat(PhoneValidateRequest request);

    /**
     * 发送重置密码验证码
     *
     * @param request 忘记密码请求
     * @param ipAddress 请求IP地址
     * @param userAgent 用户代理信息
     * @return 是否发送成功
     */
    boolean sendResetCode(ForgetPasswordRequest request, String ipAddress, String userAgent);

    /**
     * 验证验证码并校验邮箱手机号匹配
     * 验证成功后生成重置密码令牌
     *
     * @param request 忘记密码请求
     * @return 重置密码令牌响应
     */
    ResetPasswordTokenResponse verifyCodeAndGenerateToken(ForgetPasswordRequest request);

    /**
     * 重置密码
     *
     * @param request 重置密码请求
     * @return 是否重置成功
     */
    boolean resetPassword(ResetPasswordRequest request);

    /**
     * 验证重置令牌的有效性
     *
     * @param resetToken 重置令牌
     * @return 是否有效
     */
    boolean validateResetToken(String resetToken);

    /**
     * 撤销重置令牌
     *
     * @param resetToken 重置令牌
     */
    void revokeResetToken(String resetToken);
}