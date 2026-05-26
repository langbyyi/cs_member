package cloud.hilang.cs_member.service;

import cloud.hilang.cs_member.dto.SecurityConfigDTO;
import cloud.hilang.cs_member.exception.WeakPasswordException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
 * 密码验证服务
 * 根据系统配置验证密码强度
 *
 * @author HiLang Cloud Team
 * @since 2025-11-26
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordValidatorService {
    
    private final SystemConfigService systemConfigService;
    
    /**
     * 常见弱密码列表
     */
    private static final List<String> WEAK_PASSWORDS = Arrays.asList(
        "123456", "password", "12345678", "qwerty", "abc123",
        "111111", "123123", "password123", "admin", "root",
        "123456789", "qwerty123", "1q2w3e4r", "000000", "654321"
    );
    
    /**
     * 验证密码强度
     *
     * @param password 密码
     * @throws WeakPasswordException 密码不符合要求时抛出
     */
    public void validatePassword(String password) {
        if (password == null || password.isEmpty()) {
            throw new WeakPasswordException("密码不能为空");
        }
        
        SecurityConfigDTO config = systemConfigService.getSecurityConfig();
        
        // 1. 验证密码长度
        validatePasswordLength(password, config.getPasswordMinLength());
        
        // 2. 验证密码复杂度（如果启用）
        if (Boolean.TRUE.equals(config.getPasswordComplexity())) {
            validatePasswordComplexity(password);
        }
        
        // 3. 检查常见弱密码
        checkWeakPassword(password);
        
        log.debug("密码强度验证通过");
    }
    
    /**
     * 验证密码长度
     *
     * @param password 密码
     * @param minLength 最小长度
     */
    private void validatePasswordLength(String password, Integer minLength) {
        if (minLength == null) {
            minLength = 8; // 默认最小长度
        }
        
        if (password.length() < minLength) {
            throw new WeakPasswordException(
                "密码长度不能少于" + minLength + "个字符");
        }
    }
    
    /**
     * 验证密码复杂度
     * 要求包含大写字母、小写字母、数字、特殊字符中的至少3种
     *
     * @param password 密码
     */
    private void validatePasswordComplexity(String password) {
        boolean hasUpper = password.matches(".*[A-Z].*");
        boolean hasLower = password.matches(".*[a-z].*");
        boolean hasDigit = password.matches(".*\\d.*");
        boolean hasSpecial = password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*");
        
        int complexity = 0;
        if (hasUpper) complexity++;
        if (hasLower) complexity++;
        if (hasDigit) complexity++;
        if (hasSpecial) complexity++;
        
        if (complexity < 3) {
            throw new WeakPasswordException(
                "密码必须包含大写字母、小写字母、数字、特殊字符中的至少3种");
        }
    }
    
    /**
     * 检查是否为常见弱密码
     *
     * @param password 密码
     */
    private void checkWeakPassword(String password) {
        String lowerPassword = password.toLowerCase();
        
        if (WEAK_PASSWORDS.contains(lowerPassword)) {
            throw new WeakPasswordException("密码过于简单，请使用更复杂的密码");
        }
        
        // 检查是否为纯数字
        if (password.matches("^\\d+$")) {
            throw new WeakPasswordException("密码不能为纯数字");
        }
        
        // 检查是否为连续字符
        if (isSequentialChars(password)) {
            throw new WeakPasswordException("密码不能为连续字符（如123456、abcdef）");
        }
        
        // 检查是否为重复字符
        if (isRepeatingChars(password)) {
            throw new WeakPasswordException("密码不能为重复字符（如111111、aaaaaa）");
        }
    }
    
    /**
     * 检查是否为连续字符
     *
     * @param password 密码
     * @return 是否为连续字符
     */
    private boolean isSequentialChars(String password) {
        if (password.length() < 3) {
            return false;
        }
        
        int sequentialCount = 1;
        for (int i = 1; i < password.length(); i++) {
            if (password.charAt(i) == password.charAt(i - 1) + 1) {
                sequentialCount++;
                if (sequentialCount >= 4) {
                    return true;
                }
            } else {
                sequentialCount = 1;
            }
        }
        
        return false;
    }
    
    /**
     * 检查是否为重复字符
     *
     * @param password 密码
     * @return 是否为重复字符
     */
    private boolean isRepeatingChars(String password) {
        if (password.length() < 3) {
            return false;
        }
        
        char firstChar = password.charAt(0);
        int repeatCount = 1;
        
        for (int i = 1; i < password.length(); i++) {
            if (password.charAt(i) == firstChar) {
                repeatCount++;
            }
        }
        
        // 如果超过80%的字符都相同，认为是重复字符
        return (double) repeatCount / password.length() > 0.8;
    }
}
