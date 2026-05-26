package cloud.hilang.cs_member.exception;

import lombok.Getter;

/**
 * 认证异常基类
 *
 * @author HiLang Cloud Team
 * @since 2025-11-26
 */
@Getter
public class AuthenticationException extends RuntimeException {
    
    /**
     * 错误代码
     */
    private final String errorCode;
    
    /**
     * 构造函数
     *
     * @param errorCode 错误代码
     * @param message 错误消息
     */
    public AuthenticationException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
    
    /**
     * 构造函数
     *
     * @param errorCode 错误代码
     * @param message 错误消息
     * @param cause 原因
     */
    public AuthenticationException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
}
