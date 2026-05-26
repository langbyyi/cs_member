package cloud.hilang.cs_member.service;

import cloud.hilang.cs_member.entity.MemberGradeConfig;

import java.util.List;
import java.util.Map;

/**
 * 会员等级配置服务接口
 *
 * @author HiLang Cloud Team
 * @since 2025-01-24
 */
public interface MemberGradeConfigService {

    /**
     * 根据ID获取会员等级配置
     *
     * @param id 等级ID
     * @return 等级配置
     */
    MemberGradeConfig getGradeById(Long id);

    /**
     * 根据编码获取会员等级配置
     *
     * @param gradeCode 等级编码
     * @return 等级配置
     */
    MemberGradeConfig getGradeByCode(String gradeCode);

    /**
     * 按排序获取会员等级列表
     *
     * @return 等级配置列表
     */
    List<MemberGradeConfig> getGradesBySortOrder();

    /**
     * 创建会员等级配置
     *
     * @param grade 等级配置
     * @return 是否成功
     */
    boolean createGrade(MemberGradeConfig grade);

    /**
     * 更新会员等级配置
     *
     * @param grade 等级配置
     * @return 是否成功
     */
    boolean updateGrade(MemberGradeConfig grade);

    /**
     * 删除会员等级配置
     *
     * @param id 等级ID
     * @return 是否成功
     */
    boolean deleteGrade(Long id);
}