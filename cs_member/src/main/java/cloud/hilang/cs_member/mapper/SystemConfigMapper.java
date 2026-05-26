package cloud.hilang.cs_member.mapper;

import cloud.hilang.cs_member.entity.SystemConfig;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 系统配置Mapper接口
 *
 * @author HiLang Cloud Team
 * @since 2025-01-17
 */
@Mapper
public interface SystemConfigMapper extends BaseMapper<SystemConfig> {

    /**
     * 根据配置键获取配置值
     *
     * @param configKey 配置键
     * @return 配置值
     */
    String selectConfigValueByKey(@Param("configKey") String configKey);

    /**
     * 根据配置键获取整型配置值
     *
     * @param configKey 配置键
     * @param defaultValue 默认值
     * @return 配置值
     */
    Integer selectIntValueByKey(@Param("configKey") String configKey, @Param("defaultValue") Integer defaultValue);
}