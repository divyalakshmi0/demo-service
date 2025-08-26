package com.example.demo_service.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/v1.0/**")
                .allowedOrigins(
                        "https://applogics.vercel.app",
                        "https://b86389ed-4c01-4b96-b966-7eb99b586dd6-00-3qsfgfmccjso.sisko.replit.dev"
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}


