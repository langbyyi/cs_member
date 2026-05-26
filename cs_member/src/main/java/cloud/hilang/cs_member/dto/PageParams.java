package cloud.hilang.cs_member.dto;

import lombok.Data;

/**
 * 分页参数基类
 *
 * @author HiLang Cloud Team
 * @since 2025-01-19
 */
@Data
public class PageParams {

    /**
     * 页码，从1开始
     */
    private Integer pageNum = 1;

    /**
     * 每页大小
     */
    private Integer pageSize = 20;

    /**
     * 排序字段
     */
    private String sortField;

    /**
     * 排序方向：ASC/DESC
     */
    private String sortOrder = "DESC";
}