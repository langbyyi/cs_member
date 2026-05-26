package cloud.hilang.cs_member.exception;

/**
 * 手机号格式错误异常
 *
 * @author HiLang Cloud Team
 * @since 2025-11-26
 */
public class InvalidPhoneException extends AuthenticationException {
    
    public InvalidPhoneException() {
        super("INVALID_PHONE", "手机号格式不正确");
    }
    
    public InvalidPhoneException(String message) {
        super("INVALID_PHONE", message);
    }
}
