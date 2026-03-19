package com.MedLab.dicom_converter_web.config;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${dicom.storage.location}")
    private String storageLocation;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // 모든 경로에 대해
                //.allowedOriginPatterns("*") //  모든 도메인 (출처) 허용
                //.allowedOrigins("http://localhost:3000") // 프론트엔드 서버 주소 (React 디폴트값)
                .allowedOrigins("http://localhost:8080","http://127.0.0.1:5500", "http://localhost:5500","http://localhost:63343")
                .allowedMethods("GET","POST","PUT","DELETE","OPTIONS") // 허용할 HTTP 메서드
                .allowedHeaders("*") // 모든 헤더 허용
                .allowCredentials(true); // 쿠키나 인증정보 허용
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/images/**")
                .addResourceLocations("file:" + storageLocation + "/");
    }


}

//주의 : allowedOrigins("*")와  allowCredentials(true)는 동시에 쓸 수 없음. 반드시 allowedOriginPatterns("*")를 사용할 것.
// allowCredentials(true) : 요청에 사용자의 인증 정보(쿠키, 세션 등...)을 포함해서 보내는 것을 허락한다.
// alloedOrigins("*") 모든 웹사이트(악성 포함에서 나의 서버로 요청을 보내도 다 받음.
