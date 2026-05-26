package cloud.hilang.cs_member.enums;

/**
 * 系统权限枚举
 *
 * @author HiLang Cloud Team
 * @since 2025-01-14
 */
public enum Permission {

    // ==================== 系统管理权限 ====================

    /**
     * 用户管理
     */
    USER_VIEW("user:view", "查看用户"),
    USER_CREATE("user:create", "创建用户"),
    USER_UPDATE("user:update", "更新用户"),
    USER_DELETE("user:delete", "删除用户"),

    /**
     * 角色管理
     */
    ROLE_VIEW("role:view", "查看角色"),
    ROLE_CREATE("role:create", "创建角色"),
    ROLE_UPDATE("role:update", "更新角色"),
    ROLE_DELETE("role:delete", "删除角色"),

    /**
     * 权限管理
     */
    PERMISSION_VIEW("permission:view", "查看权限"),
    PERMISSION_CREATE("permission:create", "创建权限"),
    PERMISSION_UPDATE("permission:update", "更新权限"),
    PERMISSION_DELETE("permission:delete", "删除权限"),

    // ==================== 会员管理权限 ====================

    /**
     * 会员管理
     */
    MEMBER_VIEW("member:view", "查看会员"),
    MEMBER_CREATE("member:create", "创建会员"),
    MEMBER_UPDATE("member:update", "更新会员"),
    MEMBER_DELETE("member:delete", "删除会员"),
    MEMBER_IMPORT("member:import", "导入会员"),
    MEMBER_EXPORT("member:export", "导出会员"),

    /**
     * 会员等级管理
     */
    MEMBER_LEVEL_VIEW("member_level:view", "查看会员等级"),
    MEMBER_LEVEL_CREATE("member_level:create", "创建会员等级"),
    MEMBER_LEVEL_UPDATE("member_level:update", "更新会员等级"),
    MEMBER_LEVEL_DELETE("member_level:delete", "删除会员等级"),

    // ==================== 业务管理权限 ====================

    /**
     * 消费记录管理
     */
    CONSUMPTION_VIEW("consumption:view", "查看消费记录"),
    CONSUMPTION_CREATE("consumption:create", "创建消费记录"),
    CONSUMPTION_UPDATE("consumption:update", "更新消费记录"),
    CONSUMPTION_DELETE("consumption:delete", "删除消费记录"),
    CONSUMPTION_EXPORT("consumption:export", "导出消费记录"),

    /**
     * 积分管理
     */
    POINTS_VIEW("points:view", "查看积分"),
    POINTS_ADJUST("points:adjust", "调整积分"),
    POINTS_RULE_VIEW("points_rule:view", "查看积分规则"),
    POINTS_RULE_UPDATE("points_rule:update", "更新积分规则"),

    /**
     * 优惠券管理
     */
    COUPON_VIEW("coupon:view", "查看优惠券"),
    COUPON_CREATE("coupon:create", "创建优惠券"),
    COUPON_UPDATE("coupon:update", "更新优惠券"),
    COUPON_DELETE("coupon:delete", "删除优惠券"),
    COUPON_DISTRIBUTE("coupon:distribute", "分发优惠券"),

    // ==================== 门店管理权限 ====================

    /**
     * 门店管理
     */
    STORE_VIEW("store:view", "查看门店"),
    STORE_CREATE("store:create", "创建门店"),
    STORE_UPDATE("store:update", "更新门店"),
    STORE_DELETE("store:delete", "删除门店"),

    // ==================== 统计报表权限 ====================

    /**
     * 统计报表
     */
    DASHBOARD_VIEW("dashboard:view", "查看仪表盘"),
    REPORT_VIEW("report:view", "查看报表"),
    REPORT_EXPORT("report:export", "导出报表"),
    ANALYSIS_VIEW("analysis:view", "查看数据分析"),

    // ==================== 系统配置权限 ====================

    /**
     * 系统配置
     */
    CONFIG_VIEW("config:view", "查看系统配置"),
    CONFIG_UPDATE("config:update", "更新系统配置"),

    /**
     * 操作日志
     */
    LOG_VIEW("log:view", "查看操作日志"),
    LOG_EXPORT("log:export", "导出操作日志");

    private final String code;
    private final String description;

    Permission(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 根据代码获取权限
     */
    public static Permission fromCode(String code) {
        for (Permission permission : Permission.values()) {
            if (permission.getCode().equals(code)) {
                return permission;
            }
        }
        throw new IllegalArgumentException("未知的权限代码: " + code);
    }
}