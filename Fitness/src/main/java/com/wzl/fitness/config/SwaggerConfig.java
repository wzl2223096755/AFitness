package com.wzl.fitness.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Swagger API文档配置
 * 
 * 提供完整的OpenAPI 3.0文档配置，包括：
 * - API基本信息
 * - 安全认证配置
 * - 服务器配置
 * - 标签分组
 * - 通用响应示例
 * - 模块化API分组
 * 
 * @see Requirements 7.4 - 自动生成模块化的OpenAPI文档
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(createApiInfo())
                .externalDocs(createExternalDocs())
                .servers(createServers())
                .tags(createTags())
                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
                .components(createComponents());
    }
    
    // ==================== 模块化API分组配置 ====================
    
    /**
     * 全部API分组
     * 包含所有模块的API接口
     */
    @Bean
    public GroupedOpenApi allApi() {
        return GroupedOpenApi.builder()
                .group("all-apis")
                .displayName("全部API")
                .pathsToMatch("/api/**")
                .build();
    }
    
    /**
     * 用户模块API分组
     * 包含用户认证、用户管理、用户资料等接口
     */
    @Bean
    public GroupedOpenApi userModuleApi() {
        return GroupedOpenApi.builder()
                .group("user")
                .displayName("用户模块")
                .pathsToMatch("/api/v1/auth/**", "/api/v1/user/**", "/api/v1/users/**")
                .addOpenApiCustomizer(openApi -> openApi.info(
                        new Info()
                                .title("用户模块 API")
                                .description("用户认证、用户管理、用户资料等接口")
                                .version("1.0.0")
                ))
                .build();
    }
    
    /**
     * 训练模块API分组
     * 包含训练记录、训练计划、训练分析等接口
     */
    @Bean
    public GroupedOpenApi trainingModuleApi() {
        return GroupedOpenApi.builder()
                .group("training")
                .displayName("训练模块")
                .pathsToMatch("/api/v1/training/**", "/api/v1/training-plans/**")
                .addOpenApiCustomizer(openApi -> openApi.info(
                        new Info()
                                .title("训练模块 API")
                                .description("训练记录、训练计划、训练分析等接口")
                                .version("1.0.0")
                ))
                .build();
    }
    
    /**
     * 营养模块API分组
     * 包含营养记录、营养目标、营养建议等接口
     */
    @Bean
    public GroupedOpenApi nutritionModuleApi() {
        return GroupedOpenApi.builder()
                .group("nutrition")
                .displayName("营养模块")
                .pathsToMatch("/api/v1/nutrition/**")
                .addOpenApiCustomizer(openApi -> openApi.info(
                        new Info()
                                .title("营养模块 API")
                                .description("营养记录、营养目标、营养建议等接口")
                                .version("1.0.0")
                ))
                .build();
    }
    
    /**
     * 恢复评估模块API分组
     * 包含恢复状态评估、训练建议等接口
     */
    @Bean
    public GroupedOpenApi recoveryModuleApi() {
        return GroupedOpenApi.builder()
                .group("recovery")
                .displayName("恢复评估模块")
                .pathsToMatch("/api/v1/recovery/**", "/api/v1/load-recovery/**")
                .addOpenApiCustomizer(openApi -> openApi.info(
                        new Info()
                                .title("恢复评估模块 API")
                                .description("恢复状态评估、训练建议等接口")
                                .version("1.0.0")
                ))
                .build();
    }
    
    /**
     * 管理模块API分组
     * 包含系统监控、数据导出、缓存管理等接口
     */
    @Bean
    public GroupedOpenApi adminModuleApi() {
        return GroupedOpenApi.builder()
                .group("admin")
                .displayName("管理模块")
                .pathsToMatch("/api/v1/admin/**")
                .addOpenApiCustomizer(openApi -> openApi.info(
                        new Info()
                                .title("管理模块 API")
                                .description("系统监控、数据导出、缓存管理等管理员接口")
                                .version("1.0.0")
                ))
                .build();
    }
    
    /**
     * 仪表盘模块API分组
     * 包含数据概览、统计数据等接口
     */
    @Bean
    public GroupedOpenApi dashboardModuleApi() {
        return GroupedOpenApi.builder()
                .group("dashboard")
                .displayName("仪表盘模块")
                .pathsToMatch("/api/v1/dashboard/**")
                .addOpenApiCustomizer(openApi -> openApi.info(
                        new Info()
                                .title("仪表盘模块 API")
                                .description("数据概览、统计数据等接口")
                                .version("1.0.0")
                ))
                .build();
    }
    
    /**
     * 系统模块API分组
     * 包含健康检查、模块配置等系统接口
     */
    @Bean
    public GroupedOpenApi systemModuleApi() {
        return GroupedOpenApi.builder()
                .group("system")
                .displayName("系统模块")
                .pathsToMatch("/api/v1/health/**", "/api/v1/modules/**", "/actuator/**")
                .addOpenApiCustomizer(openApi -> openApi.info(
                        new Info()
                                .title("系统模块 API")
                                .description("健康检查、模块配置等系统接口")
                                .version("1.0.0")
                ))
                .build();
    }

    /**
     * 创建API基本信息
     */
    private Info createApiInfo() {
        return new Info()
                .title("AFitness 健身管理系统 API")
                .description(createApiDescription())
                .version("1.0.0")
                .contact(new Contact()
                        .name("AFitness Team")
                        .email("support@afitness.com")
                        .url("https://afitness.com"))
                .license(new License()
                        .name("MIT License")
                        .url("https://opensource.org/licenses/MIT"));
    }

    /**
     * 创建API描述文档
     */
    private String createApiDescription() {
        return """
                # AFitness 健身管理系统 API 文档
                
                ## 概述
                AFitness是一个专注于力量训练的智能化负荷计算与恢复监控平台，提供完整的健身数据管理功能。
                系统采用模块化架构设计，各功能模块独立且可配置。
                
                ## 模块化架构
                系统按业务领域划分为以下模块：
                
                | 模块 | 路由前缀 | 说明 |
                |------|----------|------|
                | 用户模块 | /api/v1/user, /api/v1/auth | 用户认证、用户管理、用户资料 |
                | 训练模块 | /api/v1/training | 训练记录、训练计划、训练分析 |
                | 营养模块 | /api/v1/nutrition | 营养记录、营养目标、营养建议 |
                | 恢复评估模块 | /api/v1/recovery | 恢复状态评估、训练建议 |
                | 管理模块 | /api/v1/admin | 系统监控、数据导出、缓存管理 |
                | 仪表盘模块 | /api/v1/dashboard | 数据概览、统计数据 |
                
                ## 主要功能
                - **用户认证**: 注册、登录、令牌刷新
                - **训练管理**: 训练记录CRUD、训练分析
                - **负荷计算**: 1RM计算、训练量计算、恢复评估
                - **营养追踪**: 营养记录、营养目标、营养建议
                - **仪表盘**: 数据概览、趋势分析
                - **系统管理**: 系统监控、数据导出、缓存管理
                
                ## 认证方式
                本API使用JWT Bearer Token认证。获取令牌后，在请求头中添加：
                ```
                Authorization: Bearer <your_token>
                ```
                
                ## 响应格式
                所有API响应遵循统一格式：
                ```json
                {
                  "code": 200,
                  "message": "操作成功",
                  "data": { ... },
                  "timestamp": "2024-01-01 12:00:00",
                  "success": true
                }
                ```
                
                ## 错误码说明
                | 错误码 | 说明 |
                |--------|------|
                | 200 | 操作成功 |
                | 400 | 参数错误 |
                | 401 | 未授权（未登录或Token失效） |
                | 403 | 权限不足 |
                | 404 | 资源不存在 |
                | 500 | 服务器内部错误 |
                | 1001 | 用户不存在 |
                | 1002 | 用户名已存在 |
                | 1003 | 邮箱已存在 |
                | 1004 | 登录失败 |
                | 1005 | 用户名或密码错误 |
                
                ## 模块配置
                各模块可通过配置文件启用/禁用：
                ```properties
                fitness.modules.user.enabled=true
                fitness.modules.training.enabled=true
                fitness.modules.nutrition.enabled=true
                fitness.modules.recovery.enabled=true
                fitness.modules.admin.enabled=true
                ```
                """;
    }

    /**
     * 创建外部文档链接
     */
    private ExternalDocumentation createExternalDocs() {
        return new ExternalDocumentation()
                .description("AFitness 完整文档")
                .url("https://github.com/afitness/docs");
    }

    /**
     * 创建服务器配置
     */
    private List<Server> createServers() {
        return Arrays.asList(
                new Server()
                        .url("http://localhost:8080")
                        .description("开发环境"),
                new Server()
                        .url("https://api.afitness.com")
                        .description("生产环境")
        );
    }

    /**
     * 创建API标签分组
     * 按模块组织API标签，便于文档导航
     */
    private List<Tag> createTags() {
        return Arrays.asList(
                // 用户模块标签
                new Tag().name("认证管理").description("用户认证相关接口，包括注册、登录、令牌刷新等"),
                new Tag().name("用户管理").description("用户信息管理，包括用户资料、用户设置等"),
                new Tag().name("用户管理（管理员）").description("管理员用户管理接口，包括用户列表、添加、删除等"),
                
                // 训练模块标签
                new Tag().name("训练管理").description("训练记录和恢复指标管理，包括训练数据CRUD、训练分析等"),
                new Tag().name("训练计划").description("训练计划管理，包括计划创建、计划执行等"),
                
                // 营养模块标签
                new Tag().name("营养记录管理").description("营养记录相关接口，包括营养记录CRUD、营养统计、营养建议等"),
                
                // 恢复评估模块标签
                new Tag().name("负荷恢复").description("负荷计算和恢复评估，包括1RM计算、训练负荷、恢复状态评估等"),
                new Tag().name("恢复评估模块").description("恢复状态评估和训练建议API"),
                
                // 仪表盘模块标签
                new Tag().name("仪表盘管理").description("仪表盘数据相关接口，包括指标概览、统计数据、分析数据等"),
                
                // 管理模块标签
                new Tag().name("系统监控").description("系统监控和指标接口，包括系统信息、JVM指标、数据库统计等"),
                new Tag().name("数据导出").description("数据导出接口，支持用户数据、训练记录、营养记录的Excel导出"),
                new Tag().name("缓存管理").description("缓存统计和管理接口，包括缓存监控、缓存清除等"),
                
                // 系统模块标签
                new Tag().name("健康检查").description("系统健康检查和监控接口"),
                new Tag().name("模块配置").description("模块配置管理接口，包括模块状态查询、模块启用/禁用等")
        );
    }

    /**
     * 创建组件配置
     */
    private Components createComponents() {
        return new Components()
                .addSecuritySchemes("Bearer Authentication", createAPIKeyScheme())
                .addSchemas("ApiResponse", createApiResponseSchema())
                .addSchemas("ErrorResponse", createErrorResponseSchema())
                .addResponses("UnauthorizedError", createUnauthorizedResponse())
                .addResponses("ForbiddenError", createForbiddenResponse())
                .addResponses("NotFoundError", createNotFoundResponse())
                .addResponses("ServerError", createServerErrorResponse());
    }

    /**
     * 创建JWT安全方案
     */
    private SecurityScheme createAPIKeyScheme() {
        return new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .bearerFormat("JWT")
                .scheme("bearer")
                .description("""
                        JWT Bearer Token认证
                        
                        获取方式：
                        1. 调用 POST /api/v1/auth/login 接口获取accessToken
                        2. 在此处输入获取到的accessToken（不包含Bearer前缀）
                        
                        示例Token格式：
                        eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhZG1pbiIsImlhdCI6MTcwNDEyMzQ1NiwiZXhwIjoxNzA0MjA5ODU2fQ...
                        """);
    }

    /**
     * 创建统一响应Schema
     */
    @SuppressWarnings("rawtypes")
    private Schema createApiResponseSchema() {
        Map<String, Object> example = new HashMap<>();
        example.put("code", 200);
        example.put("message", "操作成功");
        example.put("data", null);
        example.put("timestamp", "2024-01-01 12:00:00");
        example.put("success", true);

        return new Schema<>()
                .type("object")
                .description("统一API响应格式")
                .example(example)
                .addProperty("code", new Schema<>().type("integer").description("响应码").example(200))
                .addProperty("message", new Schema<>().type("string").description("响应消息").example("操作成功"))
                .addProperty("data", new Schema<>().type("object").description("响应数据"))
                .addProperty("timestamp", new Schema<>().type("string").description("响应时间").example("2024-01-01 12:00:00"))
                .addProperty("success", new Schema<>().type("boolean").description("是否成功").example(true));
    }

    /**
     * 创建错误响应Schema
     */
    @SuppressWarnings("rawtypes")
    private Schema createErrorResponseSchema() {
        Map<String, Object> example = new HashMap<>();
        example.put("code", 400);
        example.put("message", "参数错误");
        example.put("data", null);
        example.put("timestamp", "2024-01-01 12:00:00");
        example.put("success", false);

        return new Schema<>()
                .type("object")
                .description("错误响应格式")
                .example(example);
    }

    /**
     * 创建错误响应示例Map（支持null值）
     */
    private Map<String, Object> createErrorExample(int code, String message) {
        Map<String, Object> example = new HashMap<>();
        example.put("code", code);
        example.put("message", message);
        example.put("data", null);
        example.put("timestamp", "2024-01-01 12:00:00");
        example.put("success", false);
        return example;
    }

    /**
     * 创建401未授权响应
     */
    private ApiResponse createUnauthorizedResponse() {
        return new ApiResponse()
                .description("未授权 - Token无效或已过期")
                .content(new Content()
                        .addMediaType("application/json", new MediaType()
                                .example(createErrorExample(401, "未授权，请先登录"))));
    }

    /**
     * 创建403权限不足响应
     */
    private ApiResponse createForbiddenResponse() {
        return new ApiResponse()
                .description("权限不足 - 无权访问该资源")
                .content(new Content()
                        .addMediaType("application/json", new MediaType()
                                .example(createErrorExample(403, "权限不足"))));
    }

    /**
     * 创建404资源不存在响应
     */
    private ApiResponse createNotFoundResponse() {
        return new ApiResponse()
                .description("资源不存在")
                .content(new Content()
                        .addMediaType("application/json", new MediaType()
                                .example(createErrorExample(404, "资源不存在"))));
    }

    /**
     * 创建500服务器错误响应
     */
    private ApiResponse createServerErrorResponse() {
        return new ApiResponse()
                .description("服务器内部错误")
                .content(new Content()
                        .addMediaType("application/json", new MediaType()
                                .example(createErrorExample(500, "服务器内部错误"))));
    }
}