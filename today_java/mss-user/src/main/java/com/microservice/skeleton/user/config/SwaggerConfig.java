package com.microservice.skeleton.user.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
public class SwaggerConfig {

    @Value("${swagger.enable}")
    private Boolean enable;
    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .enable(enable)
                .apiInfo(apiInfo())
                .select()
                //.apis(RequestHandlerSelectors.withMethodAnnotation(ApiOperation.class))
                .apis(RequestHandlerSelectors.basePackage("com/microservice/skeleton/user/controller")) // 替换为你的controller包路径
                .paths(PathSelectors.any())
                .build();
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("用户模块") // API文档标题
                .description("用户模块接口文档") // API文档描述
                .version("1.0") // API版本
                //.contact(new Contact("Your Name", "http://www.yourcompany.com/contact", "your.email@example.com")) // 维护者信息
                //.termsOfServiceUrl("http://www.yourcompany.com/terms") // 服务条款URL
                .license("Apache 2.0") // 许可证
                .licenseUrl("http://www.apache.org/licenses/LICENSE-2.0.html") // 许可证URL
                .build();
    }

}
