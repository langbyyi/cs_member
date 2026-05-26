package cloud.hilang.cs_member.util;

import cloud.hilang.cs_member.dto.ResetPasswordTokenResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 重置密码令牌工具类
 *
 * @author HiLang Cloud Team
 * @since 2025-01-18
 */
@Slf4j
@Component
public class ResetPasswordTokenUtil {

    private final StringRedisTemplate redisTemplate;

    // 重置令牌Redis键前缀
    private static final String RESET_TOKEN_PREFIX = "reset_password:token:";
    // 重置令牌有效期（10分钟）
    private static final long RESET_TOKEN_EXPIRE_MINUTES = 10;

    public ResetPasswordTokenUtil(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 生成重置密码令牌
     *
     * @return 重置令牌
     */
    public String generateResetToken() {
        return "reset_" + UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 存储重置令牌到Redis
     *
     * @param resetToken 重置令牌
     * @param userInfo 用户信息
     */
    public void storeResetToken(String resetToken, ResetPasswordTokenResponse.UserInfo userInfo) {
        try {
            String key = RESET_TOKEN_PREFIX + resetToken;
            // 将用户信息序列化为JSON存储
            String userInfoJson = String.format("{\"userId\":%d,\"name\":\"%s\",\"phone\":\"%s\",\"email\":\"%s\",\"userType\":\"%s\"}",
                    userInfo.getUserId(), userInfo.getName(), userInfo.getPhone(),
                    userInfo.getEmail(), userInfo.getUserType());

            redisTemplate.opsForValue().set(key, userInfoJson, RESET_TOKEN_EXPIRE_MINUTES, TimeUnit.MINUTES);
            log.info("重置令牌已存储: resetToken={}, key={}", resetToken, key);
        } catch (Exception e) {
            log.error("存储重置令牌失败: resetToken={}, error={}", resetToken, e.getMessage(), e);
            throw new RuntimeException("存储重置令牌失败");
        }
    }

    /**
     * 获取重置令牌信息
     *
     * @param resetToken 重置令牌
     * @return 用户信息
     */
    public ResetPasswordTokenResponse.UserInfo getResetTokenInfo(String resetToken) {
        try {
            String key = RESET_TOKEN_PREFIX + resetToken;
            String userInfoJson = redisTemplate.opsForValue().get(key);

            if (userInfoJson == null) {
                return null;
            }

            // 简单的JSON解析（实际项目中建议使用Jackson等JSON库）
            // 这里为了简化，手动解析JSON字符串
            String[] parts = userInfoJson.replace("{", "").replace("}", "").replace("\"", "").split(",");
            ResetPasswordTokenResponse.UserInfo.UserInfoBuilder builder = ResetPasswordTokenResponse.UserInfo.builder();

            for (String part : parts) {
                String[] keyValue = part.split(":");
                if (keyValue.length == 2) {
                    String jsonKey = keyValue[0].trim();
                    String value = keyValue[1].trim();

                    switch (jsonKey) {
                        case "userId":
                            builder.userId(Long.parseLong(value));
                            break;
                        case "name":
                            builder.name(value);
                            break;
                        case "phone":
                            builder.phone(value);
                            break;
                        case "email":
                            builder.email(value);
                            break;
                        case "userType":
                            builder.userType(value);
                            break;
                    }
                }
            }

            return builder.build();

        } catch (Exception e) {
            log.error("获取重置令牌信息失败: resetToken={}, error={}", resetToken, e.getMessage(), e);
            return null;
        }
    }

    /**
     * 验证重置令牌的有效性
     *
     * @param resetToken 重置令牌
     * @return 是否有效
     */
    public boolean validateResetToken(String resetToken) {
        try {
            String key = RESET_TOKEN_PREFIX + resetToken;
            return Boolean.TRUE.equals(redisTemplate.hasKey(key));
        } catch (Exception e) {
            log.error("验证重置令牌失败: resetToken={}, error={}", resetToken, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 撤销重置令牌
     *
     * @param resetToken 重置令牌
     */
    public void revokeResetToken(String resetToken) {
        try {
            String key = RESET_TOKEN_PREFIX + resetToken;
            redisTemplate.delete(key);
            log.info("重置令牌已撤销: resetToken={}", resetToken);
        } catch (Exception e) {
            log.error("撤销重置令牌失败: resetToken={}, error={}", resetToken, e.getMessage(), e);
        }
    }

    /**
     * 获取重置令牌有效期（秒）
     *
     * @return 有效期（秒）
     */
    public long getResetTokenExpireSeconds() {
        return RESET_TOKEN_EXPIRE_MINUTES * 60L;
    }
}