package com.hzau.config;

import com.hzau.interceptor.RateLimitInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @projectName: AI-roleplay
 * @package: com.hzau.config
 * @className: WebMvcConfig
 * @author: zhuyuchen
 * @description: Web MVC配置
 * @date: 2025/9/23 下午3:21
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private RateLimitInterceptor rateLimitInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册限流拦截器
        registry.addInterceptor(rateLimitInterceptor)
                .addPathPatterns("/api/conversations/**", "/ai/chat/**") // 只对AI相关接口进行限流
                .excludePathPatterns("/api/characters/**"); // 排除角色管理接口，不需要限流
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 配置Swagger UI静态资源处理
        registry.addResourceHandler("/swagger-ui/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/swagger-ui/")
                .resourceChain(false);
        
        // 配置其他静态资源
        registry.addResourceHandler("/webjars/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/")
                .resourceChain(false);
        
        // 配置文件访问静态资源处理器
        registry.addResourceHandler("/files/**")
                .addResourceLocations("file:uploads/", "file:data/")
                .resourceChain(false);
    }
}

