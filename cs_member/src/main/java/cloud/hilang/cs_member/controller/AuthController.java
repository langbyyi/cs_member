package cloud.hilang.cs_member.controller;

import cloud.hilang.cs_member.common.ApiResponse;
import cloud.hilang.cs_member.dto.LoginRequest;
import cloud.hilang.cs_member.dto.LoginResponse;
import cloud.hilang.cs_member.dto.RegisterRequest;
import cloud.hilang.cs_member.dto.EmailVerificationRequest;
import cloud.hilang.cs_member.dto.EmailVerifyRequest;
import cloud.hilang.cs_member.dto.PhoneValidateRequest;
import cloud.hilang.cs_member.dto.ForgetPasswordRequest;
import cloud.hilang.cs_member.dto.ResetPasswordRequest;
import cloud.hilang.cs_member.dto.ResetPasswordTokenResponse;
import cloud.hilang.cs_member.service.AdminAuthService;
import cloud.hilang.cs_member.service.MemberAuthService;
import cloud.hilang.cs_member.service.MemberRegisterService;
import cloud.hilang.cs_member.service.EmailVerificationService;
import cloud.hilang.cs_member.service.ForgetPasswordService;
import cloud.hilang.cs_member.entity.SysUser;
import cloud.hilang.cs_member.entity.SysRole;
import cloud.hilang.cs_member.entity.Member;
import cloud.hilang.cs_member.util.MemberNoGenerator;
import cloud.hilang.cs_member.util.MemberJwtUtil;

import cloud.hilang.cs_member.mapper.SysUserMapper;
import cloud.hilang.cs_member.mapper.SysRoleMapper;
import cloud.hilang.cs_member.mapper.SysUserRoleMapper;
import cloud.hilang.cs_member.mapper.MemberMapper;
import cloud.hilang.cs_member.mapper.SystemConfigMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 认证控制器
 * 处理会员端和管理端的登录、登出、令牌刷新等认证相关操作
 *
 * @author HiLang Cloud Team
 * @since 2025-01-14
 */
@Slf4j
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "认证管理", description = "用户认证、注册、密码重置等接口")
public class AuthController {

    private final AdminAuthService adminAuthService;
    private final MemberAuthService memberAuthService;
    private final MemberRegisterService memberRegisterService;
    private final EmailVerificationService emailVerificationService;
    private final ForgetPasswordService forgetPasswordService;
    private final SysUserMapper sysUserMapper;
    private final SysRoleMapper sysRoleMapper;
    private final SysUserRoleMapper sysUserRoleMapper;
    private final MemberMapper memberMapper;
    private final SystemConfigMapper systemConfigMapper;

    private final BCryptPasswordEncoder passwordEncoder;
    private final MemberJwtUtil memberJwtUtil;


    /**
     * 会员注册
     */
    @PostMapping("/member/register")
    @Operation(summary = "会员注册", description = "使用手机号、邮箱和验证码注册新会员")
    public ResponseEntity<ApiResponse<String>> registerMember(
            @Valid @RequestBody RegisterRequest registerRequest) {

        try {
            // 验证邮箱不能为空
            if (registerRequest.getEmail() == null || registerRequest.getEmail().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("邮箱不能为空"));
            }

            if (registerRequest.getEmailCode() == null || registerRequest.getEmailCode().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("邮箱验证码不能为空"));
            }

            // 验证邮箱验证码的有效性
            boolean isEmailCodeValid = emailVerificationService.validateEmailCode(
                    registerRequest.getEmail(),
                    "member",
                    "register",
                    registerRequest.getEmailCode()
            );

            if (!isEmailCodeValid) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("邮箱验证码无效或已过期"));
            }

            // 验证手机号唯一性
            if (!memberRegisterService.isPhoneUnique(registerRequest.getPhone())) {
                return ResponseEntity.ok()
                        .body(ApiResponse.error("手机号已注册"));
            }

            // 验证邮箱唯一性
            if (!memberRegisterService.isEmailUnique(registerRequest.getEmail())) {
                return ResponseEntity.ok()
                        .body(ApiResponse.error("邮箱已被注册"));
            }

            // 获取注册积分配置
            Integer registerPoints = systemConfigMapper.selectIntValueByKey(
                "member.register_default_points", 0);

            // 创建会员基础信息
            Member member = new Member();
            member.setName(registerRequest.getName());
            member.setPhone(registerRequest.getPhone());
            member.setEmail(registerRequest.getEmail());
            member.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
            member.setGender(registerRequest.getGender() != null ? registerRequest.getGender() : 1);
            member.setMemberNo(MemberNoGenerator.generateMemberNo());
            member.setTotalConsumption(java.math.BigDecimal.ZERO);
            member.setTotalPoints(registerPoints != null && registerPoints > 0 ? registerPoints : 0);
            member.setCurrentPoints(registerPoints != null && registerPoints > 0 ? registerPoints : 0);
            member.setBalance(java.math.BigDecimal.ZERO);
            member.setRegisterStoreId(null); // 默认注册门店ID为null
            member.setStatus(1); // 正常状态
            member.setCreatedTime(LocalDateTime.now());
            member.setUpdatedTime(LocalDateTime.now());

            // 使用注册服务进行完整的会员初始化（包括会员卡、积分记录等）
            Long memberId = memberRegisterService.registerMemberWithInit(
                member, registerPoints, null); // 注册门店ID默认为null

            // 注册成功后,将邮箱验证码标记为已验证（消耗掉验证码）
            try {
                EmailVerifyRequest verifyRequest = new EmailVerifyRequest();
                verifyRequest.setUserType("member");
                verifyRequest.setEmail(registerRequest.getEmail());
                verifyRequest.setVerificationType("register");
                verifyRequest.setVerificationCode(registerRequest.getEmailCode());

                boolean isCodeConsumed = emailVerificationService.verifyEmailCode(verifyRequest);
                if (isCodeConsumed) {
                    log.info("邮箱验证码消耗成功: email={}", registerRequest.getEmail());
                } else {
                    log.warn("邮箱验证码消耗失败，但注册成功: email={}", registerRequest.getEmail());
                }
            } catch (Exception e) {
                log.error("消耗邮箱验证码失败: email={}, error={}", registerRequest.getEmail(), e.getMessage());
                // 不影响注册流程，只记录错误
            }

            log.info("会员注册成功: phone={}, name={}, memberId={}, registerPoints={}",
                    registerRequest.getPhone(), registerRequest.getName(), memberId, registerPoints);

            String successMessage = "会员账户创建成功" +
                (registerPoints != null && registerPoints > 0 ?
                    "，获得注册积分 " + registerPoints + " 分" : "");

            return ResponseEntity.ok(ApiResponse.success("注册成功", successMessage));

        } catch (Exception e) {
            log.error("会员注册失败: phone={}, error={}", registerRequest.getPhone(), e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("注册失败: " + e.getMessage()));
        }
    }

    /**
     * 根据角色编码获取角色信息
     */
    private SysRole getRoleByCode(String roleCode) {
        return sysRoleMapper.selectByRoleCode(roleCode);
    }

    /**
     * 插入用户角色关联
     */
    private void insertUserRole(Long userId, Long roleId) {
        sysUserRoleMapper.insertUserRole(userId, roleId);
    }

    /**
     * 会员校验手机号格式
     */
    @PostMapping("/member/forgot-password/validate-phone")
    @Operation(summary = "会员校验手机号格式", description = "校验会员手机号格式是否正确，无论是否存在都通过")
    public ResponseEntity<ApiResponse<Boolean>> validateMemberPhoneFormat(
            @Valid @RequestBody PhoneValidateRequest request) {

        try {
            // 自动设置为会员类型
            request.setUserType("member");
            boolean isValid = forgetPasswordService.validatePhoneFormat(request);

            if (isValid) {
                log.info("会员手机号格式校验通过: phone={}", request.getPhone());
                return ResponseEntity.ok(ApiResponse.success("手机号格式正确", true));
            } else {
                log.warn("会员手机号格式校验失败: phone={}", request.getPhone());
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("手机号格式不正确"));
            }
        } catch (Exception e) {
            log.error("会员校验手机号格式异常: phone={}, error={}",
                    request.getPhone(), e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("校验失败: " + e.getMessage()));
        }
    }

    /**
     * 管理员校验手机号格式
     */
    @PostMapping("/admin/forgot-password/validate-phone")
    @Operation(summary = "管理员校验手机号格式", description = "校验管理员手机号格式是否正确，无论是否存在都通过")
    public ResponseEntity<ApiResponse<Boolean>> validateAdminPhoneFormat(
            @Valid @RequestBody PhoneValidateRequest request) {

        try {
            // 自动设置为管理员类型
            request.setUserType("admin");
            boolean isValid = forgetPasswordService.validatePhoneFormat(request);

            if (isValid) {
                log.info("管理员手机号格式校验通过: phone={}", request.getPhone());
                return ResponseEntity.ok(ApiResponse.success("手机号格式正确", true));
            } else {
                log.warn("管理员手机号格式校验失败: phone={}", request.getPhone());
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("手机号格式不正确"));
            }
        } catch (Exception e) {
            log.error("管理员校验手机号格式异常: phone={}, error={}",
                    request.getPhone(), e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("校验失败: " + e.getMessage()));
        }
    }

    /**
     * 会员发送重置密码验证码
     */
    @PostMapping("/member/forgot-password/send-code")
    @Operation(summary = "会员发送重置密码验证码", description = "向会员邮箱发送重置密码验证码")
    public ResponseEntity<ApiResponse<String>> sendMemberResetCode(
            @Valid @RequestBody ForgetPasswordRequest request,
            HttpServletRequest httpRequest) {

        try {
            // 自动设置为会员类型
            request.setUserType("member");
            String clientIp = getClientIp(httpRequest);
            String userAgent = httpRequest.getHeader("User-Agent");

            boolean sent = forgetPasswordService.sendResetCode(request, clientIp, userAgent);

            if (sent) {
                log.info("会员重置密码验证码发送成功: phone={}, email={}, ip={}",
                        request.getPhone(), request.getEmail(), clientIp);
                return ResponseEntity.ok(ApiResponse.success("验证码发送成功", "验证码已发送到您的邮箱"));
            } else {
                log.warn("会员重置密码验证码发送失败: phone={}, email={}, ip={}",
                        request.getPhone(), request.getEmail(), clientIp);
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("验证码发送失败，请稍后重试"));
            }
        } catch (Exception e) {
            log.error("会员发送重置密码验证码异常: phone={}, email={}, error={}",
                    request.getPhone(), request.getEmail(), e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("发送失败: " + e.getMessage()));
        }
    }

    /**
     * 管理员发送重置密码验证码
     */
    @PostMapping("/admin/forgot-password/send-code")
    @Operation(summary = "管理员发送重置密码验证码", description = "向管理员邮箱发送重置密码验证码")
    public ResponseEntity<ApiResponse<String>> sendAdminResetCode(
            @Valid @RequestBody ForgetPasswordRequest request,
            HttpServletRequest httpRequest) {

        try {
            // 自动设置为管理员类型
            request.setUserType("admin");
            String clientIp = getClientIp(httpRequest);
            String userAgent = httpRequest.getHeader("User-Agent");

            boolean sent = forgetPasswordService.sendResetCode(request, clientIp, userAgent);

            if (sent) {
                log.info("管理员重置密码验证码发送成功: phone={}, email={}, ip={}",
                        request.getPhone(), request.getEmail(), clientIp);
                return ResponseEntity.ok(ApiResponse.success("验证码发送成功", "验证码已发送到您的邮箱"));
            } else {
                log.warn("管理员重置密码验证码发送失败: phone={}, email={}, ip={}",
                        request.getPhone(), request.getEmail(), clientIp);
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("验证码发送失败，请稍后重试"));
            }
        } catch (Exception e) {
            log.error("管理员发送重置密码验证码异常: phone={}, email={}, error={}",
                    request.getPhone(), request.getEmail(), e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("发送失败: " + e.getMessage()));
        }
    }

    /**
     * 会员验证验证码并生成重置令牌
     */
    @PostMapping("/member/forgot-password/verify-code")
    @Operation(summary = "会员验证验证码并生成重置令牌", description = "验证会员邮箱验证码和手机号邮箱匹配，生成重置密码令牌")
    public ResponseEntity<ApiResponse<ResetPasswordTokenResponse>> verifyMemberCodeAndGenerateToken(
            @Valid @RequestBody ForgetPasswordRequest request) {

        try {
            // 自动设置为会员类型
            request.setUserType("member");
            ResetPasswordTokenResponse response = forgetPasswordService.verifyCodeAndGenerateToken(request);

            log.info("会员重置令牌生成成功: phone={}, email={}",
                    request.getPhone(), request.getEmail());

            return ResponseEntity.ok(ApiResponse.success("验证成功，请进行密码重置", response));
        } catch (Exception e) {
            log.error("会员验证验证码并生成重置令牌失败: phone={}, email={}, error={}",
                    request.getPhone(), request.getEmail(), e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("验证失败: " + e.getMessage()));
        }
    }

    /**
     * 管理员验证验证码并生成重置令牌
     */
    @PostMapping("/admin/forgot-password/verify-code")
    @Operation(summary = "管理员验证验证码并生成重置令牌", description = "验证管理员邮箱验证码和手机号邮箱匹配，生成重置密码令牌")
    public ResponseEntity<ApiResponse<ResetPasswordTokenResponse>> verifyAdminCodeAndGenerateToken(
            @Valid @RequestBody ForgetPasswordRequest request) {

        try {
            // 自动设置为管理员类型
            request.setUserType("admin");
            ResetPasswordTokenResponse response = forgetPasswordService.verifyCodeAndGenerateToken(request);

            log.info("管理员重置令牌生成成功: phone={}, email={}",
                    request.getPhone(), request.getEmail());

            return ResponseEntity.ok(ApiResponse.success("验证成功，请进行密码重置", response));
        } catch (Exception e) {
            log.error("管理员验证验证码并生成重置令牌失败: phone={}, email={}, error={}",
                    request.getPhone(), request.getEmail(), e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("验证失败: " + e.getMessage()));
        }
    }

    /**
     * 重置密码
     */
    @PostMapping({"/member/forgot-password/reset", "/admin/forgot-password/reset"})
    @Operation(summary = "重置密码", description = "使用重置令牌和密码进行密码重置")
    public ResponseEntity<ApiResponse<String>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {

        try {
            boolean success = forgetPasswordService.resetPassword(request);

            if (success) {
                log.info("密码重置成功: resetToken={}", request.getResetToken());
                return ResponseEntity.ok(ApiResponse.success("密码重置成功", "您的密码已成功重置"));
            } else {
                log.warn("密码重置失败: resetToken={}", request.getResetToken());
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("密码重置失败"));
            }
        } catch (Exception e) {
            log.error("重置密码异常: resetToken={}, error={}", request.getResetToken(), e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("重置失败: " + e.getMessage()));
        }
    }

    /**
     * 验证重置令牌
     */
    @PostMapping({"/member/forgot-password/validate-token", "/admin/forgot-password/validate-token"})
    @Operation(summary = "验证重置令牌", description = "验证重置密码令牌的有效性")
    public ResponseEntity<ApiResponse<Boolean>> validateResetToken(
            @Parameter(description = "重置密码令牌", required = true)
            @RequestParam("resetToken") String resetToken) {

        try {
            boolean isValid = forgetPasswordService.validateResetToken(resetToken);

            return ResponseEntity.ok(ApiResponse.success("令牌验证完成", isValid));
        } catch (Exception e) {
            log.error("验证重置令牌失败: resetToken={}, error={}", resetToken, e.getMessage());
            return ResponseEntity.ok(ApiResponse.success("令牌验证完成", false));
        }
    }

    /**
     * 会员登录
     */
    @PostMapping("/member/login")
    @Operation(summary = "会员登录", description = "会员使用手机号和密码登录")
    public ResponseEntity<ApiResponse<LoginResponse>> memberLogin(
            @Valid @RequestBody LoginRequest loginRequest,
            HttpServletRequest request) {
        
        try {
            // 记录登录IP
            String clientIp = cloud.hilang.cs_member.util.IpUtil.getClientIp(request);
            log.info("会员尝试登录: phone={}, ip={}", loginRequest.getPhone(), clientIp);
            
            LoginResponse response = memberAuthService.login(loginRequest);
            
            // 补充登录信息
            response.setLoginIp(clientIp);
            response.setLoginTime(LocalDateTime.now());
            
            return ResponseEntity.ok(ApiResponse.success("登录成功", response));
        } catch (Exception e) {
            log.error("会员登录失败: phone={}, error={}", loginRequest.getPhone(), e.getMessage());
            return ResponseEntity.ok(ApiResponse.error(401, e.getMessage()));
        }
    }

    /**
     * 管理员登录
     */
    @PostMapping("/admin/login")
    @Operation(summary = "管理员登录", description = "管理员使用手机号和密码登录")
    public ResponseEntity<ApiResponse<LoginResponse>> adminLogin(
            @Valid @RequestBody LoginRequest loginRequest,
            HttpServletRequest request) {
        
        try {
            // 记录登录IP
            String clientIp = cloud.hilang.cs_member.util.IpUtil.getClientIp(request);
            log.info("管理员尝试登录: phone={}, ip={}", loginRequest.getPhone(), clientIp);
            
            LoginResponse response = adminAuthService.login(loginRequest);
            
            // 补充登录信息
            response.setLoginIp(clientIp);
            response.setLoginTime(LocalDateTime.now());
            
            return ResponseEntity.ok(ApiResponse.success("登录成功", response));
        } catch (Exception e) {
            log.error("管理员登录失败: phone={}, error={}", loginRequest.getPhone(), e.getMessage());
            return ResponseEntity.ok(ApiResponse.error(401, e.getMessage()));
        }
    }

    /**
     * 会员登出
     */
    @PostMapping("/member/logout")
    @Operation(summary = "会员登出", description = "会员退出登录，失效当前令牌")
    public ResponseEntity<ApiResponse<Void>> memberLogout(
            @RequestHeader("Authorization") String token) {
        try {
            memberAuthService.logout(token);
            return ResponseEntity.ok(ApiResponse.success("退出登录成功"));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error("退出登录失败"));
        }
    }

    /**
     * 管理员登出
     */
    @PostMapping("/admin/logout")
    @Operation(summary = "管理员登出", description = "管理员退出登录，失效当前令牌")
    public ResponseEntity<ApiResponse<Void>> adminLogout(
            @RequestHeader("Authorization") String token) {
        try {
            adminAuthService.logout(token);
            return ResponseEntity.ok(ApiResponse.success("退出登录成功"));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error("退出登录失败"));
        }
    }

    /**
     * 会员令牌刷新
     */
    @PostMapping("/member/refresh")
    @Operation(summary = "会员令牌刷新", description = "使用刷新令牌获取新的访问令牌")
    public ResponseEntity<ApiResponse<LoginResponse>> memberRefresh(
            @RequestParam("refreshToken") String refreshToken) {
        try {
            LoginResponse response = memberAuthService.refreshToken(refreshToken);
            return ResponseEntity.ok(ApiResponse.success("刷新成功", response));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error(401, "刷新令牌失败: " + e.getMessage()));
        }
    }

    /**
     * 管理员令牌刷新
     */
    @PostMapping("/admin/refresh")
    @Operation(summary = "管理员令牌刷新", description = "使用刷新令牌获取新的访问令牌")
    public ResponseEntity<ApiResponse<LoginResponse>> adminRefresh(
            @RequestParam("refreshToken") String refreshToken) {
        try {
            LoginResponse response = adminAuthService.refreshToken(refreshToken);
            return ResponseEntity.ok(ApiResponse.success("刷新成功", response));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error(401, "刷新令牌失败: " + e.getMessage()));
        }
    }

    /**
     * 获取客户端IP地址
     */
    private String getClientIp(HttpServletRequest request) {
        return cloud.hilang.cs_member.util.IpUtil.getClientIp(request);
    }

    @PostMapping("/member/check-phone-email-binding")
    @Operation(summary = "检查手机号邮箱绑定", description = "检查手机号是否已绑定邮箱")
    public ResponseEntity<ApiResponse<Boolean>> checkPhoneEmailBinding(@RequestParam String phone) {
        Member member = memberMapper.selectByPhone(phone);
        boolean bound = member != null && member.getEmail() != null && !member.getEmail().isEmpty();
        return ResponseEntity.ok(ApiResponse.success(bound));
    }

    @PostMapping("/member/send-email-binding-code")
    @Operation(summary = "发送邮箱绑定验证码", description = "发送邮箱绑定验证码到指定邮箱")
    public ResponseEntity<ApiResponse<String>> sendEmailBindingCode(
            @RequestParam String phone, @RequestParam String email) {
        Member member = memberMapper.selectByPhone(phone);
        if (member == null) return ResponseEntity.ok(ApiResponse.error("手机号未注册"));
        try {
            EmailVerificationRequest req = new EmailVerificationRequest();
            req.setEmail(email);
            req.setUserType("member");
            req.setVerificationType("bind");
            emailVerificationService.sendEmailCode(req, null, null);
            return ResponseEntity.ok(ApiResponse.success("验证码已发送"));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error("发送失败: " + e.getMessage()));
        }
    }

    @PostMapping("/member/verify-email-binding-code")
    @Operation(summary = "验证邮箱绑定验证码", description = "验证邮箱绑定验证码并绑定邮箱")
    public ResponseEntity<ApiResponse<String>> verifyEmailBindingCode(
            @RequestParam String phone, @RequestParam String email, @RequestParam String verifyCode) {
        Member member = memberMapper.selectByPhone(phone);
        if (member == null) return ResponseEntity.ok(ApiResponse.error("手机号未注册"));
        try {
            EmailVerifyRequest req = new EmailVerifyRequest();
            req.setEmail(email);
            req.setUserType("member");
            req.setVerificationType("bind");
            req.setVerificationCode(verifyCode);
            boolean verified = emailVerificationService.verifyEmailCode(req);
            if (verified) {
                member.setEmail(email);
                memberMapper.updateById(member);
                return ResponseEntity.ok(ApiResponse.success("邮箱绑定成功"));
            }
            return ResponseEntity.ok(ApiResponse.error("验证码错误"));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error("验证失败: " + e.getMessage()));
        }
    }
}