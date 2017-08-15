package com.zg.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
public class Swagger {

    @Value("${swagger.enable}")
    private Boolean swaggerEnable;
    @Value("${swagger.title}")
    private String title;
    @Value("${swagger.version}")
    private String version;
    @Value("${swagger.auth}")
    private String auth;
    @Value("${swagger.url}")
    private String url;
    @Value("${swagger.email}")
    private String email;

    @Bean
    public Docket createRestApi() {
        Docket docket = null;
        if (swaggerEnable) {
            docket = new Docket(DocumentationType.SWAGGER_2)
                    .apiInfo(new ApiInfoBuilder().title(title).version(version).contact(new Contact(auth, url, email)).build())
                    .select()
                    .apis(RequestHandlerSelectors.basePackage("com.zg"))
                    .paths(PathSelectors.any())
                    .build();
        } else {
            docket = new Docket(DocumentationType.SWAGGER_2).enable(false);
        }
        return docket;
    }

}
