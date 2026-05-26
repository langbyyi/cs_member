package cloud.hilang.cs_member.service.impl;

import cloud.hilang.cs_member.entity.MemberGradeConfig;
import cloud.hilang.cs_member.mapper.MemberGradeConfigMapper;
import cloud.hilang.cs_member.mapper.MemberCardMapper;
import cloud.hilang.cs_member.service.MemberGradeConfigService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 会员等级配置服务实现类
 *
 * @author HiLang Cloud Team
 * @since 2025-01-24
 */
@Slf4j
@Service
public class MemberGradeConfigServiceImpl extends ServiceImpl<MemberGradeConfigMapper, MemberGradeConfig>
        implements MemberGradeConfigService {

    private final MemberCardMapper memberCardMapper;

    public MemberGradeConfigServiceImpl(MemberCardMapper memberCardMapper) {
        this.memberCardMapper = memberCardMapper;
    }

    @Override
    public MemberGradeConfig getGradeById(Long id) {
        return getOne(new LambdaQueryWrapper<MemberGradeConfig>()
                .eq(MemberGradeConfig::getId, id));
    }

    @Override
    public MemberGradeConfig getGradeByCode(String gradeCode) {
        return getOne(new LambdaQueryWrapper<MemberGradeConfig>()
                .eq(MemberGradeConfig::getGradeCode, gradeCode)
                .eq(MemberGradeConfig::getStatus, 1));
    }

    @Override
    public List<MemberGradeConfig> getGradesBySortOrder() {
        return list(new LambdaQueryWrapper<MemberGradeConfig>()
                .orderByAsc(MemberGradeConfig::getSortOrder));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createGrade(MemberGradeConfig grade) {
        if (grade == null) {
            return false;
        }

        // 数据验证
        validateGradeData(grade);

        // 检查等级编码是否已存在
        MemberGradeConfig existingGrade = getGradeByCode(grade.getGradeCode());
        if (existingGrade != null) {
            throw new IllegalArgumentException("等级编码已存在: " + grade.getGradeCode());
        }

        // 设置默认值
        if (grade.getSortOrder() == null) {
            // 获取最大排序值 + 1
            MemberGradeConfig lastGrade = getOne(new LambdaQueryWrapper<MemberGradeConfig>()
                    .orderByDesc(MemberGradeConfig::getSortOrder)
                    .last("LIMIT 1"));
            grade.setSortOrder(lastGrade != null ? lastGrade.getSortOrder() + 1 : 1);
        }

        if (grade.getStatus() == null) {
            grade.setStatus(1);
        }

        return save(grade);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateGrade(MemberGradeConfig grade) {
        if (grade == null || grade.getId() == null) {
            return false;
        }

        // 数据验证
        validateGradeData(grade);

        // 添加日志查看接收到的数据
        log.info("更新会员等级 - ID: {}, 等级编码: {}, 等级名称: {}", grade.getId(), grade.getGradeCode(), grade.getGradeName());
        log.info("更新会员等级 - 积分范围: {} - {}", grade.getMinPoints(), grade.getMaxPoints());
        log.info("更新会员等级 - 积分倍率: {}, 折扣率: {}", grade.getPointsMultiplier(), grade.getDiscountRate());
        log.info("更新会员等级 - 颜色: {}, 排序: {}, 状态: {}",
            grade.getColor(), grade.getSortOrder(), grade.getStatus());

        // 检查是否存在
        MemberGradeConfig existingGrade = getGradeById(grade.getId());
        if (existingGrade == null) {
            throw new IllegalArgumentException("等级配置不存在");
        }

        return updateById(grade);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteGrade(Long id) {
        MemberGradeConfig grade = getGradeById(id);
        if (grade == null) {
            return false;
        }

        // MemberCard已移除grade_id字段，无需检查会员使用情况

        // 直接删除（会员等级通过累计积分动态判断，无需检查会员使用情况）
        return removeById(id);
    }

    /**
     * 验证会员等级数据
     *
     * @param grade 等级配置
     */
    private void validateGradeData(MemberGradeConfig grade) {
        // ---------- 基础必填字段 ----------
        if (grade.getGradeCode() == null || grade.getGradeCode().trim().isEmpty()) {
            throw new IllegalArgumentException("等级编码不能为空");
        }
        if (grade.getGradeCode().length() > 20) {
            throw new IllegalArgumentException("等级编码长度不能超过 20");
        }
        if (grade.getGradeName() == null || grade.getGradeName().trim().isEmpty()) {
            throw new IllegalArgumentException("等级名称不能为空");
        }
        if (grade.getGradeName().length() > 50) {
            throw new IllegalArgumentException("等级名称长度不能超过 50");
        }
        // ---------- 可选字段约束 ----------
        if (grade.getColor() != null && grade.getColor().length() > 20) {
            throw new IllegalArgumentException("颜色字段长度不能超过 20");
        }
        if (grade.getSortOrder() != null && grade.getSortOrder() < 0) {
            throw new IllegalArgumentException("排序值必须为非负整数");
        }
        if (grade.getStatus() != null) {
            int status = grade.getStatus();
            if (status != 0 && status != 1 && status != 2) {
                throw new IllegalArgumentException("状态只能是 0（禁用）、1（正常）或 2（冻结）");
            }
        }
        // ---------- 积分倍率 ----------
        if (grade.getPointsMultiplier() != null) {
            if (grade.getPointsMultiplier().compareTo(new java.math.BigDecimal("0")) < 0 ||
                grade.getPointsMultiplier().compareTo(new java.math.BigDecimal("9.99")) > 0) {
                throw new IllegalArgumentException("积分倍率必须在 0.00 到 9.99 之间");
            }
        }
        // ---------- 折扣率 ----------
        if (grade.getDiscountRate() != null) {
            if (grade.getDiscountRate().compareTo(new java.math.BigDecimal("0")) < 0 ||
                grade.getDiscountRate().compareTo(new java.math.BigDecimal("999.99")) > 0) {
                throw new IllegalArgumentException("折扣率必须在 0.00 到 999.99 之间");
            }
        }
        // ---------- 积分范围 ----------
        if (grade.getMinPoints() != null && grade.getMinPoints() < 0) {
            throw new IllegalArgumentException("最低积分不能为负数");
        }
        if (grade.getMaxPoints() != null && grade.getMinPoints() != null) {
            if (grade.getMaxPoints() <= grade.getMinPoints()) {
                throw new IllegalArgumentException("最高积分必须大于最低积分");
            }
        }
    }
}