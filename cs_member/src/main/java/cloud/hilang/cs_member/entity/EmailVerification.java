package cloud.hilang.cs_member.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 邮箱验证实体类
 *
 * @author HiLang Cloud Team
 * @since 2025-01-17
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("email_verification")
public class EmailVerification {

    /**
     * 验证ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户类型：member-会员 admin-管理员
     */
    @TableField("user_type")
    private String userType;

    
    /**
     * 邮箱地址
     */
    @TableField("email")
    private String email;

    /**
     * 验证码
     */
    @TableField("verification_code")
    private String verificationCode;

    /**
     * 验证类型：register-注册 reset-重置密码 bind-绑定邮箱
     */
    @TableField("verification_type")
    private String verificationType;

    /**
     * 是否已验证：0-未验证 1-已验证
     */
    @TableField("is_verified")
    private Boolean isVerified;

    /**
     * 发送时间
     */
    @TableField("send_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime sendTime;

    /**
     * 过期时间
     */
    @TableField("expire_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expireTime;

    /**
     * 验证时间
     */
    @TableField("verified_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime verifiedTime;

    /**
     * 请求IP
     */
    @TableField("ip_address")
    private String ipAddress;

    /**
     * 用户代理信息
     */
    @TableField("user_agent")
    private String userAgent;

    /**
     * 创建时间
     */
    @TableField(value = "created_time", fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    @TableField(value = "updated_time", fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedTime;

    /**
     * 验证类型枚举
     */
    public enum VerificationType {
        REGISTER("register", "注册"),
        RESET("reset", "重置密码"),
        BIND("bind", "绑定邮箱");

        private final String code;
        private final String description;

        VerificationType(String code, String description) {
            this.code = code;
            this.description = description;
        }

        public String getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }

        public static VerificationType getByCode(String code) {
            for (VerificationType type : values()) {
                if (type.getCode().equals(code)) {
                    return type;
                }
            }
            return null;
        }
    }

    /**
     * 用户类型枚举
     */
    public enum UserType {
        MEMBER("member", "会员"),
        ADMIN("admin", "管理员");

        private final String code;
        private final String description;

        UserType(String code, String description) {
            this.code = code;
            this.description = description;
        }

        public String getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }

        public static UserType getByCode(String code) {
            for (UserType type : values()) {
                if (type.getCode().equals(code)) {
                    return type;
                }
            }
            return null;
        }
    }
}