package cloud.hilang.cs_member.common;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * 统一API响应结果
 * 提供标准的API响应格式，包含状态码、消息、数据和时间戳
 *
 * @author HiLang Cloud Team
 * @since 2025-01-14
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "统一API响应")
public class ApiResponse<T> {

    /**
     * 响应状态码
     */
    @Schema(description = "响应状态码", example = "200")
    private Integer code;

    /**
     * 响应消息
     */
    @Schema(description = "响应消息", example = "操作成功")
    private String message;

    /**
     * 响应数据
     */
    @Schema(description = "响应数据")
    private T data;

    /**
     * 响应时间戳
     */
    @Schema(description = "响应时间戳")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;

    /**
     * 私有构造函数
     */
    private ApiResponse(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.timestamp = LocalDateTime.now();
    }

    /**
     * 成功响应（无数据）
     */
    public static <T> ApiResponse<T> success() {
        return new ApiResponse<>(200, "操作成功", null);
    }

    /**
     * 成功响应（自定义消息）
     */
    public static <T> ApiResponse<T> success(String message) {
        return new ApiResponse<>(200, message, null);
    }

    /**
     * 成功响应（带数据）
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(200, "操作成功", data);
    }

    /**
     * 成功响应（自定义消息和数据）
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(200, message, data);
    }

    /**
     * 失败响应（默认状态码500）
     */
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(500, message, null);
    }

    /**
     * 失败响应（自定义状态码）
     */
    public static <T> ApiResponse<T> error(Integer code, String message) {
        return new ApiResponse<>(code, message, null);
    }

    /**
     * 失败响应（自定义状态码和数据）
     */
    public static <T> ApiResponse<T> error(Integer code, String message, T data) {
        return new ApiResponse<>(code, message, data);
    }

    /**
     * 参数错误响应
     */
    public static <T> ApiResponse<T> badRequest(String message) {
        return new ApiResponse<>(400, message, null);
    }

    /**
     * 未授权响应
     */
    public static <T> ApiResponse<T> unauthorized(String message) {
        return new ApiResponse<>(401, message != null ? message : "未授权，请先登录", null);
    }

    /**
     * 禁止访问响应
     */
    public static <T> ApiResponse<T> forbidden(String message) {
        return new ApiResponse<>(403, message != null ? message : "权限不足，无法访问", null);
    }

    /**
     * 资源不存在响应
     */
    public static <T> ApiResponse<T> notFound(String message) {
        return new ApiResponse<>(404, message != null ? message : "资源不存在", null);
    }

    /**
     * 服务器错误响应
     */
    public static <T> ApiResponse<T> internalServerError(String message) {
        return new ApiResponse<>(500, message != null ? message : "服务器内部错误", null);
    }

    /**
     * 业务异常响应
     */
    public static <T> ApiResponse<T> businessError(String message) {
        return new ApiResponse<>(1001, message, null);
    }

    /**
     * 验证失败响应
     */
    public static <T> ApiResponse<T> validationError(String message) {
        return new ApiResponse<>(422, message, null);
    }

    /**
     * 判断响应是否成功
     */
    public boolean isSuccess() {
        return code != null && code == 200;
    }

    /**
     * 判断是否为客户端错误
     */
    public boolean isClientError() {
        return code != null && code >= 400 && code < 500;
    }

    /**
     * 判断是否为服务器错误
     */
    public boolean isServerError() {
        return code != null && code >= 500;
    }

    /**
     * 创建分页响应
     */
    public static <T> ApiResponse<PageResponse<T>> page(PageResponse<T> pageData) {
        return new ApiResponse<>(200, "查询成功", pageData);
    }

    /**
     * 创建分页响应（自定义消息）
     */
    public static <T> ApiResponse<PageResponse<T>> page(String message, PageResponse<T> pageData) {
        return new ApiResponse<>(200, message, pageData);
    }

    /**
     * 分页响应数据
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "分页响应数据")
    public static class PageResponse<T> {
        /**
         * 数据列表
         */
        @Schema(description = "数据列表")
        private T records;

        /**
         * 总记录数
         */
        @Schema(description = "总记录数", example = "100")
        private Long total;

        /**
         * 当前页码
         */
        @Schema(description = "当前页码", example = "1")
        private Long current;

        /**
         * 每页大小
         */
        @Schema(description = "每页大小", example = "10")
        private Long size;

        /**
         * 总页数
         */
        @Schema(description = "总页数", example = "10")
        private Long pages;
    }
}