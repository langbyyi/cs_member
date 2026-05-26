package cloud.hilang.cs_member.enums;

/**
 * 用户角色枚举
 *
 * @author HiLang Cloud Team
 * @since 2025-01-14
 */
public enum UserRole {

    /**
     * 会员用户
     */
    MEMBER("MEMBER", "会员用户"),

    /**
     * 系统管理员
     */
    SYSTEM_ADMIN("SYSTEM_ADMIN", "系统管理员"),

    /**
     * 门店管理员
     */
    STORE_ADMIN("STORE_ADMIN", "门店管理员"),

    /**
     * 店员
     */
    CLERK("CLERK", "店员");

    private final String code;
    private final String description;

    UserRole(String code, String description) {
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
     * 根据代码获取角色
     */
    public static UserRole fromCode(String code) {
        for (UserRole role : UserRole.values()) {
            if (role.getCode().equals(code)) {
                return role;
            }
        }
        throw new IllegalArgumentException("未知的用户角色代码: " + code);
    }

    /**
     * 判断是否为管理员角色
     */
    public boolean isAdmin() {
        return this == SYSTEM_ADMIN || this == STORE_ADMIN;
    }

    /**
     * 判断是否为员工角色
     */
    public boolean isEmployee() {
        return this == CLERK || this == STORE_ADMIN || this == SYSTEM_ADMIN;
    }
}