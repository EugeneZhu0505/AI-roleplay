package com.hzau.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.http.codec.multipart.DefaultPartHttpMessageReader;
import org.springframework.http.codec.multipart.MultipartHttpMessageReader;
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

    @Override
    public void configureHttpMessageCodecs(ServerCodecConfigurer configurer) {
        // 配置multipart文件上传支持
        DefaultPartHttpMessageReader partReader = new DefaultPartHttpMessageReader();
        partReader.setMaxParts(1024);
        partReader.setMaxDiskUsagePerPart(100 * 1024 * 1024L); // 100MB per part
        partReader.setMaxInMemorySize(1024 * 1024); // 1MB in memory
        
        MultipartHttpMessageReader multipartReader = new MultipartHttpMessageReader(partReader);

        configurer.defaultCodecs().multipartReader(multipartReader);
        configurer.defaultCodecs().maxInMemorySize(100 * 1024 * 1024); // 100MB
        
        // 启用multipart支持
        configurer.defaultCodecs().enableLoggingRequestDetails(true);
    }
}

