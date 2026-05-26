package cloud.hilang.cs_member.service.impl;

import cloud.hilang.cs_member.dto.PhoneValidateRequest;
import cloud.hilang.cs_member.dto.ForgetPasswordRequest;
import cloud.hilang.cs_member.dto.ResetPasswordRequest;
import cloud.hilang.cs_member.dto.ResetPasswordTokenResponse;
import cloud.hilang.cs_member.dto.EmailVerificationRequest;
import cloud.hilang.cs_member.service.ForgetPasswordService;
import cloud.hilang.cs_member.service.EmailVerificationService;
import cloud.hilang.cs_member.entity.Member;
import cloud.hilang.cs_member.entity.SysUser;
import cloud.hilang.cs_member.mapper.MemberMapper;
import cloud.hilang.cs_member.mapper.SysUserMapper;
import cloud.hilang.cs_member.util.ResetPasswordTokenUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.regex.Pattern;

/**
 * 忘记密码服务实现类
 *
 * @author HiLang Cloud Team
 * @since 2025-01-18
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ForgetPasswordServiceImpl implements ForgetPasswordService {

    private final EmailVerificationService emailVerificationService;
    private final MemberMapper memberMapper;
    private final SysUserMapper sysUserMapper;
    private final ResetPasswordTokenUtil resetPasswordTokenUtil;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // 手机号格式正则表达式
    private static final Pattern PHONE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");

    @Override
    public boolean validatePhoneFormat(PhoneValidateRequest request) {
        log.info("校验手机号格式: phone={}, userType={}", request.getPhone(), request.getUserType());

        // 只校验手机号格式，不检查是否存在
        if (!PHONE_PATTERN.matcher(request.getPhone()).matches()) {
            log.warn("手机号格式不正确: {}", request.getPhone());
            return false;
        }

        // 校验用户类型
        if (!"member".equals(request.getUserType()) && !"admin".equals(request.getUserType())) {
            log.warn("用户类型不正确: {}", request.getUserType());
            return false;
        }

        log.info("手机号格式校验通过: phone={}, userType={}", request.getPhone(), request.getUserType());
        return true;
    }

    @Override
    public boolean sendResetCode(ForgetPasswordRequest request, String ipAddress, String userAgent) {
        log.info("发送重置密码验证码: phone={}, email={}, userType={}",
                request.getPhone(), request.getEmail(), request.getUserType());

        try {
            // 直接发送验证码，不在发送阶段验证手机号邮箱匹配
            // 验证将在下一步（验证验证码阶段）进行

            // 创建邮箱验证请求
            EmailVerificationRequest emailRequest = new EmailVerificationRequest();
            emailRequest.setEmail(request.getEmail());
            emailRequest.setUserType(request.getUserType());
            emailRequest.setVerificationType("reset");

            // 发送重置密码验证码
            boolean sent = emailVerificationService.sendEmailCode(emailRequest, ipAddress, userAgent);

            if (sent) {
                log.info("重置密码验证码发送成功: email={}, userType={}", request.getEmail(), request.getUserType());
            } else {
                log.warn("重置密码验证码发送失败: email={}, userType={}", request.getEmail(), request.getUserType());
            }

            return sent;

        } catch (Exception e) {
            log.error("发送重置密码验证码异常: phone={}, email={}, userType={}, error={}",
                    request.getPhone(), request.getEmail(), request.getUserType(), e.getMessage(), e);
            return false;
        }
    }

    @Override
    @Transactional
    public ResetPasswordTokenResponse verifyCodeAndGenerateToken(ForgetPasswordRequest request) {
        log.info("验证验证码并生成重置令牌: phone={}, email={}, userType={}",
                request.getPhone(), request.getEmail(), request.getUserType());

        try {
            // 步骤1：先验证邮箱验证码是否正确
            boolean isCodeValid = emailVerificationService.validateEmailCode(
                    request.getEmail(),
                    request.getUserType(),
                    "reset",
                    request.getEmailCode()
            );

            if (!isCodeValid) {
                throw new RuntimeException("邮箱验证码无效或已过期");
            }

            // 步骤2：验证码正确后，再验证手机号和邮箱是否匹配
            if (!validatePhoneEmailMatch(request)) {
                throw new RuntimeException("手机号和邮箱不匹配");
            }

            // 获取用户信息
            Object user = getUserByPhoneAndEmail(request);
            if (user == null) {
                throw new RuntimeException("用户信息不存在");
            }

            // 生成重置令牌
            String resetToken = resetPasswordTokenUtil.generateResetToken();

            // 构建用户信息
            ResetPasswordTokenResponse.UserInfo userInfo = buildUserInfo(user, request.getUserType());

            // 将重置令牌存储到Redis
            resetPasswordTokenUtil.storeResetToken(resetToken, userInfo);

            // 消耗邮箱验证码
            try {
                emailVerificationService.deleteAllRecords(request.getEmail(), request.getUserType(), "reset");
                log.info("邮箱验证码消耗成功: email={}, userType={}", request.getEmail(), request.getUserType());
            } catch (Exception e) {
                log.error("消耗邮箱验证码失败: email={}, error={}", request.getEmail(), e.getMessage());
                // 不影响主流程
            }

            // 构建响应
            ResetPasswordTokenResponse response = ResetPasswordTokenResponse.builder()
                    .resetToken(resetToken)
                    .expiresIn(resetPasswordTokenUtil.getResetTokenExpireSeconds())
                    .userInfo(userInfo)
                    .build();

            log.info("重置令牌生成成功: phone={}, userType={}, resetToken={}",
                    request.getPhone(), request.getUserType(), resetToken);

            return response;

        } catch (Exception e) {
            log.error("验证验证码并生成重置令牌失败: phone={}, email={}, userType={}, error={}",
                    request.getPhone(), request.getEmail(), request.getUserType(), e.getMessage(), e);
            throw new RuntimeException("验证失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public boolean resetPassword(ResetPasswordRequest request) {
        log.info("重置密码: resetToken={}", request.getResetToken());

        try {
            // 验证两次密码是否一致
            if (!request.getNewPassword().equals(request.getConfirmPassword())) {
                throw new RuntimeException("两次输入的密码不一致");
            }

            // 验证重置令牌
            ResetPasswordTokenResponse.UserInfo userInfo = resetPasswordTokenUtil.getResetTokenInfo(request.getResetToken());
            if (userInfo == null) {
                throw new RuntimeException("重置令牌无效或已过期");
            }

            // 更新密码
            boolean updated = updatePassword(userInfo, request.getNewPassword());

            if (updated) {
                // 撤销重置令牌
                resetPasswordTokenUtil.revokeResetToken(request.getResetToken());
                log.info("密码重置成功: userId={}, userType={}", userInfo.getUserId(), userInfo.getUserType());
            } else {
                log.warn("密码重置失败: userId={}, userType={}", userInfo.getUserId(), userInfo.getUserType());
            }

            return updated;

        } catch (Exception e) {
            log.error("重置密码失败: resetToken={}, error={}", request.getResetToken(), e.getMessage(), e);
            throw new RuntimeException("重置密码失败: " + e.getMessage());
        }
    }

    @Override
    public boolean validateResetToken(String resetToken) {
        return resetPasswordTokenUtil.validateResetToken(resetToken);
    }

    @Override
    public void revokeResetToken(String resetToken) {
        resetPasswordTokenUtil.revokeResetToken(resetToken);
    }

    /**
     * 验证手机号和邮箱是否匹配
     */
    private boolean validatePhoneEmailMatch(ForgetPasswordRequest request) {
        if ("member".equals(request.getUserType())) {
            Member member = memberMapper.selectByPhone(request.getPhone());
            return member != null && request.getEmail().equals(member.getEmail());
        } else if ("admin".equals(request.getUserType())) {
            SysUser user = sysUserMapper.selectByPhone(request.getPhone());
            return user != null && request.getEmail().equals(user.getEmail());
        }
        return false;
    }

    /**
     * 根据手机号和邮箱获取用户信息
     */
    private Object getUserByPhoneAndEmail(ForgetPasswordRequest request) {
        if ("member".equals(request.getUserType())) {
            return memberMapper.selectByPhone(request.getPhone());
        } else if ("admin".equals(request.getUserType())) {
            return sysUserMapper.selectByPhone(request.getPhone());
        }
        return null;
    }

    /**
     * 构建用户信息
     */
    private ResetPasswordTokenResponse.UserInfo buildUserInfo(Object user, String userType) {
        if ("member".equals(userType) && user instanceof Member) {
            Member member = (Member) user;
            return ResetPasswordTokenResponse.UserInfo.builder()
                    .userId(member.getId())
                    .name(member.getName())
                    .phone(member.getPhone())
                    .email(member.getEmail())
                    .userType(userType)
                    .build();
        } else if ("admin".equals(userType) && user instanceof SysUser) {
            SysUser sysUser = (SysUser) user;
            return ResetPasswordTokenResponse.UserInfo.builder()
                    .userId(sysUser.getId())
                    .name(sysUser.getName())
                    .phone(sysUser.getPhone())
                    .email(sysUser.getEmail())
                    .userType(userType)
                    .build();
        }
        return null;
    }

    /**
     * 更新用户密码
     */
    private boolean updatePassword(ResetPasswordTokenResponse.UserInfo userInfo, String newPassword) {
        try {
            String encodedPassword = passwordEncoder.encode(newPassword);

            if ("member".equals(userInfo.getUserType())) {
                Member member = new Member();
                member.setId(userInfo.getUserId());
                member.setPassword(encodedPassword);
                member.setUpdatedTime(LocalDateTime.now());
                return memberMapper.updateById(member) > 0;
            } else if ("admin".equals(userInfo.getUserType())) {
                SysUser user = new SysUser();
                user.setId(userInfo.getUserId());
                user.setPassword(encodedPassword);
                user.setUpdatedTime(LocalDateTime.now());
                return sysUserMapper.updateById(user) > 0;
            }

            return false;

        } catch (Exception e) {
            log.error("更新密码失败: userId={}, userType={}, error={}",
                    userInfo.getUserId(), userInfo.getUserType(), e.getMessage(), e);
            return false;
        }
    }
}