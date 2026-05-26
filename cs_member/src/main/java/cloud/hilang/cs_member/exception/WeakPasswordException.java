package cloud.hilang.cs_member.exception;

/**
 * 弱密码异常
 * 当用户设置的密码不符合安全要求时抛出此异常
 *
 * @author HiLang Cloud Team
 * @since 2025-11-26
 */
public class WeakPasswordException extends RuntimeException {

    public WeakPasswordException(String message) {
        super(message);
    }

    public WeakPasswordException(String message, Throwable cause) {
        super(message, cause);
    }
}