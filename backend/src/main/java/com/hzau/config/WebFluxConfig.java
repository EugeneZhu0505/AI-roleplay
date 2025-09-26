package com.hzau.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.config.EnableWebFlux;
import org.springframework.web.reactive.config.ResourceHandlerRegistry;
import org.springframework.web.reactive.config.WebFluxConfigurer;

/**
 * @projectName: AI-roleplay
 * @package: com.hzau.config
 * @className: WebFluxConfig
 * @author: zhuyuchen
 * @description: WebFlux配置 - 替代原来的WebMvcConfig
 * @date: 2025/9/23 下午3:21
 */
@Configuration
@EnableWebFlux
public class WebFluxConfig implements WebFluxConfigurer {

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

