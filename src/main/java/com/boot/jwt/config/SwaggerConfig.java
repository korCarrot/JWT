package com.boot.jwt.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.ApiKey;
import springfox.documentation.service.AuthorizationScope;
import springfox.documentation.service.SecurityReference;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;

import java.util.List;

@Configuration
public class SwaggerConfig {

//  Docket 객체를 통해 Swagger 문서의 구성을 제어하고 API를 문서화
/*
useDefaultResponseMessages(false) :  기본 응답 메시지를 사용하지 않도록 설정합니다. 즉, 사용자가 직접 응답 메시지를 정의할 수 있습니다
select() : API 문서에 표시할 요청 핸들러를 선택
apis(RequestHandlerSelectors.basePackage("com.boot.springbootstudy")) : 지정된 패키지 내의 요청 핸들러만을 선택하도록 설정
paths(PathSelectors.any()) : 모든 경로를 포함하도록 설정합니다. 이는 API 문서에 프로젝트 내의 모든 엔드포인트를 포함
apiInfo(apiInfo()) : API 문서의 정보를 설정하는 메서드
 */
    @Bean
    public Docket api(){
        return new Docket(DocumentationType.OAS_30)
                .useDefaultResponseMessages(false)
                .select()
                .apis(RequestHandlerSelectors.withClassAnnotation(RestController.class))

                .paths(PathSelectors.any())
                .build()
                .securitySchemes(List.of(apiKey()))
                .securityContexts(List.of(securityContext()))
                .apiInfo(apiInfo());
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("Boot API 01 Project Swagger")
                .build();
    }

    //Authorization: API 키의 이름을 지정합니다. 이 이름은 Swagger UI에서 API 키 입력란의 이름으로 사용됩니다.
    //Bearer Token: API 키의 값의 예를 나타냅니다. 실제로는 사용자 인증을 위한 토큰이 여기에 들어갑니다.
    //header: API 키가 포함될 HTTP 헤더를 지정합니다. 일반적으로 Authorization 헤더를 사용합니다.
    private ApiKey apiKey() {
        return new ApiKey("Authorization", "Bearer Token", "header");
    }



    private SecurityContext securityContext() {
        return SecurityContext.builder().securityReferences(defaultAuth())  //securityReferences(defaultAuth()) : 보안 참조를 설정합니다.
                .operationSelector(selector -> selector.requestMappingPattern().startsWith("/api/")).build();   //API 경로가 /api/로 시작하는 엔드포인트에만 보안 설정을 적용
    }

    private List<SecurityReference> defaultAuth() { // 인증 범위의 이름을 지정, 인증 범위의 설명을 지정(여기서는 global access -> 전역 접근 권한)
        AuthorizationScope authorizationScope = new AuthorizationScope("global", "global access");
        //보안 참조의 이름을 지정(apiKey() 메서드에서 지정한 API 키 이름과 일치) ,  인증 범위 배열을 지정합니다. 여기서는 하나의 전역 인증 범위만 지정
        return List.of(new SecurityReference("Authorization", new AuthorizationScope[] {authorizationScope}));
    }

}

