package cloud.hilang.cs_member.service.impl;

import cloud.hilang.cs_member.dto.EmailVerificationRequest;
import cloud.hilang.cs_member.dto.EmailVerifyRequest;
import cloud.hilang.cs_member.entity.EmailVerification;
import cloud.hilang.cs_member.mapper.EmailVerificationMapper;
import cloud.hilang.cs_member.service.EmailVerificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

/**
 * 邮箱验证结果枚举
 */
enum EmailVerificationResult {
    SUCCESS("发送成功"),
    RATE_LIMITED("发送频率超限"),
    INVALID_PARAMS("参数无效"),
    SAVE_FAILED("保存验证码失败"),
    SEND_FAILED("邮件发送失败");

    private final String message;

    EmailVerificationResult(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}

/**
 * 邮箱验证服务实现类
 *
 * @author HiLang Cloud Team
 * @since 2025-01-17
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailVerificationServiceImpl implements EmailVerificationService {

    private final EmailVerificationMapper emailVerificationMapper;
    private final JavaMailSender javaMailSender;

    // 验证码有效期（分钟）
    private static final int VERIFICATION_CODE_EXPIRE_MINUTES = 10;
    // 同一邮箱一小时内的发送次数限制
    private static final int HOURLY_SEND_LIMIT = 5;
    // 同一邮箱一分钟内的发送次数限制
    private static final int MINUTELY_SEND_LIMIT = 1;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean sendEmailCode(EmailVerificationRequest request, String ipAddress, String userAgent) {
        try {
            // 参数验证
            if (request == null || request.getEmail() == null || request.getUserType() == null || request.getVerificationType() == null) {
                log.error("发送邮箱验证码失败：参数不完整");
                return false;
            }

            // 验证用户类型和验证类型
            EmailVerification.UserType userType = EmailVerification.UserType.getByCode(request.getUserType());
            EmailVerification.VerificationType verificationType = EmailVerification.VerificationType.getByCode(request.getVerificationType());

            if (userType == null || verificationType == null) {
                log.error("发送邮箱验证码失败：无效的用户类型或验证类型");
                return false;
            }

            // 检查发送频率限制
            if (!checkSendFrequency(request.getEmail(), request.getUserType())) {
                log.warn("邮箱验证码发送频率超限：邮箱={}", request.getEmail());
                return false;
            }

            // 删除该邮箱同类型的旧验证码记录
            emailVerificationMapper.deleteByEmailAndType(request.getEmail(), request.getUserType(), request.getVerificationType());

            // 生成6位数字验证码
            String verificationCode = generateVerificationCode();

            // 创建验证码记录
            EmailVerification emailVerification = new EmailVerification()
                    .setUserType(request.getUserType())
                    .setEmail(request.getEmail())
                    .setVerificationCode(verificationCode)
                    .setVerificationType(request.getVerificationType())
                    .setIsVerified(false)
                    .setSendTime(LocalDateTime.now())
                    .setExpireTime(LocalDateTime.now().plusMinutes(VERIFICATION_CODE_EXPIRE_MINUTES))
                    .setIpAddress(ipAddress)
                    .setUserAgent(userAgent)
                    .setCreatedTime(LocalDateTime.now())
                    .setUpdatedTime(LocalDateTime.now());

            // 保存验证码记录
            int result = emailVerificationMapper.insert(emailVerification);
            if (result <= 0) {
                log.error("保存邮箱验证码记录失败，邮箱：{}", request.getEmail());
                return false;
            }

            // 异步发送邮件
            CompletableFuture.runAsync(() -> {
                try {
                    sendVerificationEmail(request.getEmail(), verificationCode, verificationType, userType);
                    log.info("邮箱验证码发送成功，邮箱：{}，类型：{}", request.getEmail(), request.getVerificationType());
                } catch (Exception e) {
                    log.error("发送邮箱验证码失败，邮箱：{}", request.getEmail(), e);
                    // 如果邮件发送失败，可以考虑删除验证码记录或者标记为失败
                }
            });

            return true;
        } catch (Exception e) {
            log.error("发送邮箱验证码异常，邮箱：{}", request.getEmail(), e);
            return false;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean verifyEmailCode(EmailVerifyRequest request) {
        try {
            if (request == null || request.getEmail() == null || request.getVerificationCode() == null) {
                log.error("验证邮箱验证码失败：参数不完整");
                return false;
            }

            // 查找有效的验证码记录
            EmailVerification verificationRecord = emailVerificationMapper.findValidRecordByCode(
                    request.getEmail(),
                    request.getUserType(),
                    request.getVerificationType(),
                    request.getVerificationCode(),
                    LocalDateTime.now()
            );

            if (verificationRecord == null) {
                log.warn("验证邮箱验证码失败：未找到有效的验证码记录，邮箱：{}", request.getEmail());
                return false;
            }

            // 标记为已验证
            int updateResult = emailVerificationMapper.markAsVerified(
                    request.getEmail(),
                    request.getUserType(),
                    request.getVerificationType(),
                    request.getVerificationCode(),
                    LocalDateTime.now()
            );

            if (updateResult > 0) {
                log.info("邮箱验证码验证成功，邮箱：{}，类型：{}", request.getEmail(), request.getVerificationType());
                return true;
            } else {
                log.warn("标记验证码为已验证失败，邮箱：{}", request.getEmail());
                return false;
            }
        } catch (Exception e) {
            log.error("验证邮箱验证码异常，邮箱：{}", request.getEmail(), e);
            return false;
        }
    }

    @Override
    public boolean validateEmailCode(String email, String userType, String verificationType, String verificationCode) {
        try {
            EmailVerification verificationRecord = emailVerificationMapper.findValidRecordByCode(
                    email,
                    userType,
                    verificationType,
                    verificationCode,
                    LocalDateTime.now()
            );

            return verificationRecord != null;
        } catch (Exception e) {
            log.error("验证邮箱验证码异常，邮箱：{}", email, e);
            return false;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cleanExpiredRecords() {
        try {
            LocalDateTime expireTime = LocalDateTime.now();
            int deletedCount = emailVerificationMapper.deleteExpiredRecords(expireTime);
            if (deletedCount > 0) {
                log.info("清理过期邮箱验证码记录，删除数量：{}", deletedCount);
            }
        } catch (Exception e) {
            log.error("清理过期邮箱验证码记录失败", e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteAllRecords(String email, String userType, String verificationType) {
        try {
            int deletedCount = emailVerificationMapper.deleteByEmailAndType(email, userType, verificationType);
            log.info("删除邮箱验证码记录，邮箱：{}，类型：{}，删除数量：{}", email, verificationType, deletedCount);
        } catch (Exception e) {
            log.error("删除邮箱验证码记录失败，邮箱：{}", email, e);
        }
    }

    @Override
    public EmailVerification getLatestRecord(String email, String userType, String verificationType) {
        try {
            List<EmailVerification> records = emailVerificationMapper.findValidUnverifiedRecords(
                    email, userType, verificationType, LocalDateTime.now()
            );
            return records.isEmpty() ? null : records.get(0);
        } catch (Exception e) {
            log.error("获取最新验证码记录失败，邮箱：{}", email, e);
            return null;
        }
    }

    /**
     * 检查发送频率限制
     */
    private boolean checkSendFrequency(String email, String userType) {
        try {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime oneHourAgo = now.minusHours(1);
            LocalDateTime oneMinuteAgo = now.minusMinutes(1);

            // 检查一小时内发送次数
            int hourlyCount = emailVerificationMapper.countSendAttempts(email, userType, oneHourAgo, now);
            if (hourlyCount >= HOURLY_SEND_LIMIT) {
                log.warn("邮箱验证码发送频率超限：一小时内已发送{}次，限制：{}", hourlyCount, HOURLY_SEND_LIMIT);
                return false;
            }

            // 检查一分钟内发送次数
            int minutelyCount = emailVerificationMapper.countSendAttempts(email, userType, oneMinuteAgo, now);
            if (minutelyCount >= MINUTELY_SEND_LIMIT) {
                log.warn("邮箱验证码发送频率超限：一分钟内已发送{}次，限制：{}", minutelyCount, MINUTELY_SEND_LIMIT);
                return false;
            }

            return true;
        } catch (Exception e) {
            log.error("检查邮箱验证码发送频率失败", e);
            return false;
        }
    }

  
    /**
     * 生成验证码
     */
    private String generateVerificationCode() {
        Random random = new Random();
        int code = random.nextInt(900000) + 100000; // 生成100000-999999之间的数字
        return String.valueOf(code);
    }

    /**
     * 发送验证码邮件
     */
    private void sendVerificationEmail(String toEmail, String verificationCode,
                                    EmailVerification.VerificationType verificationType,
                                    EmailVerification.UserType userType) throws MessagingException {

        String subject = getEmailSubject(verificationType, userType);
        String content = getEmailContent(verificationCode, verificationType, userType);

        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setFrom("your-email@qq.com");
        helper.setTo(toEmail);
        helper.setSubject(subject);
        helper.setText(content, true); // 设置为HTML格式

        javaMailSender.send(message);
    }

    /**
     * 获取邮件主题
     */
    private String getEmailSubject(EmailVerification.VerificationType verificationType, EmailVerification.UserType userType) {
        String userTypeDesc = userType.getDescription();
        switch (verificationType) {
            case REGISTER:
                return userTypeDesc + "注册邮箱验证码";
            case RESET:
                return userTypeDesc + "密码重置邮箱验证码";
            case BIND:
                return userTypeDesc + "绑定邮箱验证码";
            default:
                return "邮箱验证码";
        }
    }

    /**
     * 获取邮件内容
     */
    private String getEmailContent(String verificationCode, EmailVerification.VerificationType verificationType, EmailVerification.UserType userType) {
        String userTypeDesc = userType.getDescription();

        StringBuilder content = new StringBuilder();
        content.append("<html><body style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px;'>");
        content.append("<div style='background-color: #f8f9fa; padding: 30px; border-radius: 8px; text-align: center;'>");

        if (verificationType == EmailVerification.VerificationType.REGISTER) {
            content.append("<h2 style='color: #007bff; margin-bottom: 20px;'>欢迎使用便利店会员管理系统</h2>");
            content.append("<p style='font-size: 16px; color: #666; margin-bottom: 20px;'>您正在进行").append(userTypeDesc).append("注册，请使用以下验证码完成邮箱验证：</p>");
        } else if (verificationType == EmailVerification.VerificationType.RESET) {
            content.append("<h2 style='color: #dc3545; margin-bottom: 20px;'>密码重置验证</h2>");
            content.append("<p style='font-size: 16px; color: #666; margin-bottom: 20px;'>您正在进行密码重置操作，请使用以下验证码：</p>");
        } else if (verificationType == EmailVerification.VerificationType.BIND) {
            content.append("<h2 style='color: #28a745; margin-bottom: 20px;'>绑定邮箱验证</h2>");
            content.append("<p style='font-size: 16px; color: #666; margin-bottom: 20px;'>您正在进行邮箱绑定操作，请使用以下验证码：</p>");
        }

        content.append("<div style='background-color: #fff; border: 2px dashed #007bff; padding: 20px; margin: 20px 0; border-radius: 8px;'>");
        content.append("<span style='font-size: 32px; font-weight: bold; color: #007bff; letter-spacing: 5px;'>").append(verificationCode).append("</span>");
        content.append("</div>");

        content.append("<p style='font-size: 14px; color: #999; margin-top: 20px;'>验证码有效期为10分钟，请尽快使用</p>");
        content.append("<p style='font-size: 12px; color: #ccc; margin-top: 10px;'>如非本人操作，请忽略此邮件</p>");
        content.append("</div>");
        content.append("</body></html>");

        return content.toString();
    }
}