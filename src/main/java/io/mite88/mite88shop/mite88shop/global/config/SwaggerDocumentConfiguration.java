package io.mite88.mite88shop.mite88shop.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * packageName    : io.mite88.mite88shop.bolg.global.config
 * fileName       : SwaggerDocumentConfiguration
 * author         : Admin
 * date           : 26. 6. 11.
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 26. 6. 11.        Admin       최초 생성
 */
@Configuration
public class SwaggerDocumentConfiguration {

    @Bean
    public OpenAPI openAPI(){
        //인증방식 설정
        SecurityScheme  securityScheme= new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT");

        SecurityRequirement securityRequirement =  new SecurityRequirement().addList("bearerAuth");


        OpenAPI openAPI = new OpenAPI();
        openAPI.components(
                new Components().addSecuritySchemes("bearerAuth", securityScheme)
        ).addSecurityItem(securityRequirement);

        return openAPI;
    }
}
