package com.boot.jwt.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

//http://localhost:8080/swagger-ui/
//swagger-ui 사용할 때 경로 맨 마지막에 '/' 작성해주어야 함.
@Configuration //해당 클래스가 스프링빈(Bean)에 대한 설정을 하는 클래스임을 명시
@EnableWebMvc
public class CustomServletConfig implements WebMvcConfigurer {


    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry
                .addResourceHandler("/files/**")    // '/files/'로 시작하는 경로는 스프링 MVC에서 일반 파일 경로로 처리하도록 지정
                .addResourceLocations("classpath:/static/");    // 도메인에 /files/sample.html 하면 화면이 나온다.
    }
}
