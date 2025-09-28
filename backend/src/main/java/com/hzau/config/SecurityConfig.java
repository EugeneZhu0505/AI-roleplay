package com.hzau.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.security.web.server.authorization.ServerAccessDeniedHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hzau.common.Result;
import com.hzau.common.constants.ErrorCode;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.nio.charset.StandardCharsets;

/**
 * @projectName: AI-roleplay
 * @package: com.hzau.config
 * @className: SecurityConfig
 * @author: zhuyuchen
 * @description: Spring Security WebFlux安全配置
 * @date: 2025/9/22 下午6:54
 */
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Value("${file.upload.base-url}")
    private String localhost;

    @Autowired
    private com.hzau.interceptor.RateLimitInterceptor rateLimitInterceptor;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter();
    }

    @Bean
    public SecurityWebFilterChain filterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeExchange(exchanges -> exchanges
                        // 公开路径，不需要认证
                        .pathMatchers("/api/auth/**").permitAll()
                        .pathMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**", "/webjars/**").permitAll()
                        .pathMatchers("/actuator/**").permitAll()
                        .pathMatchers("/error").permitAll()
                        .pathMatchers("/files/**").permitAll()
                        // 其他所有请求都需要认证
                        .anyExchange().authenticated()
                )
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(authenticationEntryPoint())
                        .accessDeniedHandler(accessDeniedHandler())
                )
                .addFilterBefore(jwtAuthenticationFilter(), SecurityWebFiltersOrder.AUTHENTICATION)
                //.addFilterAfter(rateLimitInterceptor, SecurityWebFiltersOrder.AUTHENTICATION)
                .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // 修复跨域配置：当setAllowCredentials为true时，不能使用通配符
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000", "http://127.0.0.1:3000", localhost + ":3000"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L); // 预检请求缓存时间
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public ServerAuthenticationEntryPoint authenticationEntryPoint() {
        return (exchange, ex) -> {
            ServerHttpResponse response = exchange.getResponse();
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            response.getHeaders().add("Content-Type", MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8");
            
            // 添加CORS头
            String origin = exchange.getRequest().getHeaders().getFirst("Origin");
            if (origin != null) {
                response.getHeaders().add("Access-Control-Allow-Origin", origin);
            }
            response.getHeaders().add("Access-Control-Allow-Credentials", "true");
            response.getHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
            response.getHeaders().add("Access-Control-Allow-Headers", "*");

            Map<String, Object> result = new HashMap<>();
            result.put("code", 401);
            result.put("message", "未认证，请先登录");
            result.put("data", null);
            
            try {
                ObjectMapper mapper = new ObjectMapper();
                String json = mapper.writeValueAsString(result);
                byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
                
                return response.writeWith(
                    Mono.just(response.bufferFactory().wrap(bytes))
                );
            } catch (Exception e) {
                return Mono.error(e);
            }
        };
    }

    @Bean
    public ServerAccessDeniedHandler accessDeniedHandler() {
        return (exchange, denied) -> {
            ServerHttpResponse response = exchange.getResponse();
            response.setStatusCode(HttpStatus.FORBIDDEN);
            response.getHeaders().add("Content-Type", MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8");
            
            // 添加CORS头
            String origin = exchange.getRequest().getHeaders().getFirst("Origin");
            if (origin != null) {
                response.getHeaders().add("Access-Control-Allow-Origin", origin);
            }
            response.getHeaders().add("Access-Control-Allow-Credentials", "true");
            response.getHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
            response.getHeaders().add("Access-Control-Allow-Headers", "*");

            Map<String, Object> result = new HashMap<>();
            result.put("code", 403);
            result.put("message", "权限不足");
            result.put("data", null);
            
            try {
                ObjectMapper mapper = new ObjectMapper();
                String json = mapper.writeValueAsString(result);
                byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
                
                return response.writeWith(
                    Mono.just(response.bufferFactory().wrap(bytes))
                );
            } catch (Exception e) {
                return Mono.error(e);
            }
        };
    }
}
