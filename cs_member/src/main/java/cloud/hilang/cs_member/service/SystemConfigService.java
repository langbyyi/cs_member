package cloud.hilang.cs_member.service;

import cloud.hilang.cs_member.entity.SystemConfig;

import java.util.List;
import java.util.Map;

/**
 * 系统配置服务接口
 *
 * @author HiLang Cloud Team
 * @since 2025-01-24
 */
public interface SystemConfigService {

    /**
     * 获取所有配置
     *
     * @return 配置列表
     */
    List<SystemConfig> getAllConfigs();

    /**
     * 根据配置键获取配置
     *
     * @param configKey 配置键
     * @return 配置
     */
    SystemConfig getConfigByKey(String configKey);

    /**
     * 保存配置
     *
     * @param configKey 配置键
     * @param configValue 配置值
     * @return 是否成功
     */
    boolean saveConfig(String configKey, String configValue);

    /**
     * 批量保存配置
     *
     * @param configs 配置键值对
     * @return 是否成功
     */
    boolean batchSaveConfigs(Map<String, String> configs);

    /**
     * 获取安全配置
     *
     * @return 安全配置DTO
     */
    cloud.hilang.cs_member.dto.SecurityConfigDTO getSecurityConfig();
}