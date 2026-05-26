package cloud.hilang.cs_member.controller;

import cloud.hilang.cs_member.common.ApiResponse;
import cloud.hilang.cs_member.dto.EmailVerificationRequest;
import cloud.hilang.cs_member.dto.EmailVerifyRequest;
import cloud.hilang.cs_member.service.EmailVerificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 邮箱验证控制器
 *
 * @author HiLang Cloud Team
 * @since 2025-01-17
 */
@Slf4j
@RestController
@RequestMapping({"/api/v1/member/email-verification", "/api/v1/admin/email-verification"})
@RequiredArgsConstructor
@Tag(name = "邮箱验证管理", description = "邮箱验证码发送与验证相关接口")
public class EmailVerificationController {

    private final EmailVerificationService emailVerificationService;

    @PostMapping("/send-code")
    @Operation(summary = "发送邮箱验证码", description = "向指定邮箱发送验证码，支持注册、重置密码、绑定邮箱等场景")
    public ApiResponse<Boolean> sendEmailCode(
            @Valid @RequestBody EmailVerificationRequest request,
            HttpServletRequest httpRequest) {

        try {
            // 获取客户端信息
            String ipAddress = getClientIpAddress(httpRequest);
            String userAgent = httpRequest.getHeader("User-Agent");

            log.info("发送邮箱验证码请求：邮箱={}，用户类型={}，验证类型={}，IP={}",
                    request.getEmail(), request.getUserType(), request.getVerificationType(), ipAddress);

            boolean success = emailVerificationService.sendEmailCode(request, ipAddress, userAgent);

            if (success) {
                return ApiResponse.success("验证码发送成功，请查收邮件", true);
            } else {
                return ApiResponse.error("发送频率过于频繁，请60秒后再试");
            }
        } catch (Exception e) {
            log.error("发送邮箱验证码失败", e);
            return ApiResponse.error("发送频率过于频繁，请60秒后再试");
        }
    }

    @PostMapping("/verify-code")
    @Operation(summary = "验证邮箱验证码", description = "验证邮箱验证码的有效性")
    public ApiResponse<Boolean> verifyEmailCode(
            @Valid @RequestBody EmailVerifyRequest request) {

        try {
            log.info("验证邮箱验证码请求：邮箱={}，用户类型={}，验证类型={}",
                    request.getEmail(), request.getUserType(), request.getVerificationType());

            boolean success = emailVerificationService.verifyEmailCode(request);

            if (success) {
                return ApiResponse.success("验证码验证成功", true);
            } else {
                return ApiResponse.error("验证码无效或已过期");
            }
        } catch (Exception e) {
            log.error("验证邮箱验证码失败", e);
            return ApiResponse.error("验证失败，请稍后重试");
        }
    }

  
    /**
     * 获取客户端IP地址
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String ipAddress = request.getHeader("X-Forwarded-For");

        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("Proxy-Client-IP");
        }

        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("WL-Proxy-Client-IP");
        }

        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("HTTP_CLIENT_IP");
        }

        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("HTTP_X_FORWARDED_FOR");
        }

        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddr();
        }

        // 如果是多个代理服务器，第一个IP为客户端真实IP
        if (ipAddress != null && ipAddress.contains(",")) {
            ipAddress = ipAddress.split(",")[0].trim();
        }

        return ipAddress;
    }
}