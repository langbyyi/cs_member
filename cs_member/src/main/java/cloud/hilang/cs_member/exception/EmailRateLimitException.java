package cloud.hilang.cs_member.exception;

/**
 * 邮箱验证码发送频率限制异常
 *
 * @author HiLang Cloud Team
 * @since 2025-01-18
 */
public class EmailRateLimitException extends RuntimeException {

    private final String email;
    private final int remainingTime;

    public EmailRateLimitException(String message, String email, int remainingTime) {
        super(message);
        this.email = email;
        this.remainingTime = remainingTime;
    }

    public EmailRateLimitException(String message, String email) {
        super(message);
        this.email = email;
        this.remainingTime = 60; // 默认等待60秒
    }

    public String getEmail() {
        return email;
    }

    public int getRemainingTime() {
        return remainingTime;
    }
}