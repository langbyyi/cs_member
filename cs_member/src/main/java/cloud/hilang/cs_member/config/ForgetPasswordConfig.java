package cloud.hilang.cs_member.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * 忘记密码功能配置类
 *
 * @author HiLang Cloud Team
 * @since 2025-01-18
 */
@Configuration
public class ForgetPasswordConfig {

    /**
     * 配置StringRedisTemplate Bean（如果系统中还没有的话）
     * 确保Redis操作正常工作
     */
    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
        StringRedisTemplate template = new StringRedisTemplate();
        template.setConnectionFactory(redisConnectionFactory);
        return template;
    }
}