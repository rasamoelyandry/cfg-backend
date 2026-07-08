package com.cfg.config;

import com.cfg.common.security.TenantAccessInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new TenantAccessInterceptor())
                .addPathPatterns("/api/v1/restaurants/{restaurantId}/**");
    }
}
