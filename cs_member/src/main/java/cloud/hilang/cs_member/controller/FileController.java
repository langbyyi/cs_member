package cloud.hilang.cs_member.controller;

import cloud.hilang.cs_member.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 文件上传控制器
 *
 * @author HiLang Cloud Team
 * @since 2025-11-25
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/common")
@Tag(name = "文件管理", description = "文件上传相关接口")
public class FileController {

    private static final String UPLOAD_DIR = "./uploads/";

    @Operation(summary = "上传文件", description = "通用文件上传接口")
    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<Map<String, String>>> uploadFile(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.ok(ApiResponse.error("上传文件不能为空"));
        }

        try {
            // 确保上传目录存在
            File uploadDir = new File(UPLOAD_DIR);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }

            // 生成文件名
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String filename = UUID.randomUUID().toString() + extension;

            // 保存文件
            File dest = new File(uploadDir.getAbsolutePath() + File.separator + filename);
            file.transferTo(dest);

            // 返回访问URL
            String url = "/uploads/" + filename;
            Map<String, String> result = new HashMap<>();
            result.put("url", url);
            result.put("filename", filename);

            log.info("文件上传成功: {}", dest.getAbsolutePath());
            return ResponseEntity.ok(ApiResponse.success(result));

        } catch (IOException e) {
            log.error("文件上传失败", e);
            return ResponseEntity.ok(ApiResponse.error("文件上传失败: " + e.getMessage()));
        }
    }
}
