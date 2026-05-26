package cloud.hilang.cs_member.service.impl;

import cloud.hilang.cs_member.entity.SystemConfig;
import cloud.hilang.cs_member.mapper.SystemConfigMapper;
import cloud.hilang.cs_member.service.SystemConfigService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cloud.hilang.cs_member.dto.SecurityConfigDTO;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * 系统配置服务实现类
 *
 * @author HiLang Cloud Team
 * @since 2025-01-24
 */
@Service
public class SystemConfigServiceImpl extends ServiceImpl<SystemConfigMapper, SystemConfig> implements SystemConfigService {

    private volatile SecurityConfigDTO cachedSecurityConfig;
    private volatile long lastCacheTime = 0;
    private static final long CACHE_DURATION = 5 * 60 * 1000; // 5 minutes

    @Override
    public List<SystemConfig> getAllConfigs() {
        return list(new LambdaQueryWrapper<SystemConfig>()
                .orderByAsc(SystemConfig::getConfigType)
                .orderByAsc(SystemConfig::getSortOrder));
    }

    @Override
    public SystemConfig getConfigByKey(String configKey) {
        return getOne(new LambdaQueryWrapper<SystemConfig>()
                .eq(SystemConfig::getConfigKey, configKey));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveConfig(String configKey, String configValue) {
        SystemConfig config = getConfigByKey(configKey);
        if (config != null) {
            config.setConfigValue(configValue);
            boolean result = updateById(config);
            if (result && "security".equals(config.getConfigType())) {
                clearSecurityCache();
            }
            return result;
        } else {
            config = new SystemConfig();
            config.setConfigKey(configKey);
            config.setConfigValue(configValue);
            config.setConfigType("system");
            config.setDataType("string");
            return save(config);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchSaveConfigs(Map<String, String> configs) {
        if (configs == null || configs.isEmpty()) {
            return true;
        }

        List<SystemConfig> updateConfigs = new ArrayList<>();
        List<SystemConfig> newConfigs = new ArrayList<>();

        configs.forEach((key, value) -> {
            SystemConfig existConfig = getConfigByKey(key);
            if (existConfig != null) {
                existConfig.setConfigValue(value);
                updateConfigs.add(existConfig);
            } else {
                SystemConfig newConfig = new SystemConfig();
                newConfig.setConfigKey(key);
                newConfig.setConfigValue(value);
                newConfig.setConfigType("system");
                newConfig.setDataType("string");
                newConfigs.add(newConfig);
            }
        });

        boolean result = true;
        boolean hasSecurityConfig = false;

        if (!updateConfigs.isEmpty()) {
            result = updateBatchById(updateConfigs);
            for (SystemConfig config : updateConfigs) {
                if ("security".equals(config.getConfigType())) {
                    hasSecurityConfig = true;
                    break;
                }
            }
        }
        if (!newConfigs.isEmpty()) {
            result = result && saveBatch(newConfigs);
            // New configs are hardcoded to "system" type in this method, so no need to check for security type
        }

        if (hasSecurityConfig) {
            clearSecurityCache();
        }

        return result;
    }

    @Override
    public SecurityConfigDTO getSecurityConfig() {
        if (cachedSecurityConfig != null && (System.currentTimeMillis() - lastCacheTime < CACHE_DURATION)) {
            return cachedSecurityConfig;
        }

        synchronized (this) {
            if (cachedSecurityConfig != null && (System.currentTimeMillis() - lastCacheTime < CACHE_DURATION)) {
                return cachedSecurityConfig;
            }

            List<SystemConfig> securityConfigs = list(new LambdaQueryWrapper<SystemConfig>()
                    .eq(SystemConfig::getConfigType, "security"));

            Map<String, String> configMap = new HashMap<>();
            for (SystemConfig config : securityConfigs) {
                configMap.put(config.getConfigKey(), config.getConfigValue());
            }

            SecurityConfigDTO configDTO = SecurityConfigDTO.builder()
                    .passwordMinLength(parseInteger(configMap.get("security.password_min_length"), 10))
                    .loginMaxAttempts(parseInteger(configMap.get("security.login_max_attempts"), 5))
                    .loginLockoutDuration(parseInteger(configMap.get("security.login_lockout_duration"), 30))
                    .passwordComplexity(parseBoolean(configMap.get("security.password_complexity"), false))
                    .sessionTimeout(parseInteger(configMap.get("security.session_timeout"), 30))
                    .tokenExpireHours(parseInteger(configMap.get("security.token_expire_hours"), 2))
                    .refreshTokenExpireDays(parseInteger(configMap.get("security.refresh_token_expire_days"), 7))
                    .build();

            cachedSecurityConfig = configDTO;
            lastCacheTime = System.currentTimeMillis();
            return configDTO;
        }
    }

    private void clearSecurityCache() {
        synchronized (this) {
            cachedSecurityConfig = null;
        }
    }

    private Integer parseInteger(String value, Integer defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private Boolean parseBoolean(String value, Boolean defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        return Boolean.parseBoolean(value);
    }
}