package cloud.hilang.cs_member.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Swagger API文档配置类
 * 配置OpenAPI 3.0规范的自定义API文档
 *
 * @author HiLang Cloud Team
 * @since 2025-01-15
 */
@Configuration
public class SwaggerConfig {

    @Value("${server.port:8080}")
    private String serverPort;

    /**
     * 配置OpenAPI基本信息
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(apiInfo())
                .servers(List.of(
                        new Server()
                                .url("http://localhost:" + serverPort)
                                .description("开发环境服务器"),
                        new Server()
                                .url("https://api.cs-member.com")
                                .description("生产环境服务器")
                ))
                .components(new Components()
                        .addSecuritySchemes("Bearer Authentication", createAPIKeyScheme()))
                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"));
    }

    /**
     * API文档基本信息
     */
    private Info apiInfo() {
        return new Info()
                .title("便利店会员管理系统 API")
                .description("基于Spring Boot 3.x + MyBatis + PageHelper构建的便利店会员管理系统API接口文档")
                .version("1.0.0")
                .contact(new Contact()
                        .name("HiLang Cloud Team")
                        .email("support@hilang.cloud")
                        .url("https://www.hilang.cloud"))
                .license(new License()
                        .name("MIT License")
                        .url("https://opensource.org/licenses/MIT"));
    }

    /**
     * 配置JWT认证方案
     */
    private SecurityScheme createAPIKeyScheme() {
        return new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .bearerFormat("JWT")
                .scheme("bearer")
                .description("请在此处输入JWT Token，格式：Bearer <token>");
    }
}