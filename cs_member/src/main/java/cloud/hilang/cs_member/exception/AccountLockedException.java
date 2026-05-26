package cloud.hilang.cs_member.exception;

/**
 * 账户锁定异常
 *
 * @author HiLang Cloud Team
 * @since 2025-11-26
 */
public class AccountLockedException extends AuthenticationException {
    
    public AccountLockedException(String message) {
        super("ACCOUNT_LOCKED", message);
    }
    
    public AccountLockedException(Integer lockoutMinutes) {
        super("ACCOUNT_LOCKED", "登录失败次数过多，账户已被锁定" + lockoutMinutes + "分钟");
    }
}
