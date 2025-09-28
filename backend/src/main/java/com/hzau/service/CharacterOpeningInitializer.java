package com.hzau.service;

import com.hzau.entity.AiCharacter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;

/**
 * @projectName: AI-roleplay
 * @package: com.hzau.service
 * @className: CharacterOpeningInitializer
 * @author: zhuyuchen
 * @description: TODO
 * @date: 2025/9/23 下午3:31
 */
@Slf4j
@Component
public class CharacterOpeningInitializer {

    @Autowired
    private CharacterService characterService;

    @Autowired
    private AiRoleplayService aiRoleplayService;

    /**
     * 应用启动时预生成所有角色的开场白
     */
    @Bean
    public ApplicationRunner initCharacterOpenings() {
        return args -> {
            log.info("开始预生成角色开场白...");

            try {
                // 获取所有激活的角色
                List<AiCharacter> activeCharacters = characterService.getAllActiveCharacters();
                log.info("找到 {} 个激活的角色", activeCharacters.size());

                if (activeCharacters.isEmpty()) {
                    log.warn("没有找到激活的角色，跳过开场白预生成");
                    return;
                }

                // 使用响应式编程批量预生成开场白
                Flux<String> voiceInitFlux = Flux.fromIterable(activeCharacters)
                        .delayElements(Duration.ofSeconds(3)) // 增加间隔到3秒，避免并发控制冲突
                        .flatMap(character -> {
                            log.info("预生成角色开场白: {} (ID: {})", character.getName(), character.getId());
                            Mono<String> openingMono = aiRoleplayService.getCharacterOpening(character.getId())
                                    .map(openingResponse -> openingResponse.getText()) // 从CharacterOpeningResponse中提取文本
                                    .timeout(Duration.ofSeconds(30)) // 添加超时控制
                                    .doOnSuccess(opening -> log.info("角色 {} 开场白预生成成功", character.getName()))
                                    .doOnError(error -> log.error("角色 {} 开场白预生成失败: {}", character.getName(), error.getMessage()))
                                    .onErrorResume(error -> {
                                        // 出错时返回空，继续处理下一个角色
                                        return Mono.empty();
                                    });
                            return openingMono;
                        }, 1); // 限制并发数为1，避免同时发起多个AI请求
                Mono<List<String>> finalResultMono = voiceInitFlux.collectList()
                        .doOnSuccess(results -> {
                            long successCount = results.stream().filter(result -> result != null && !result.isEmpty()).count();
                            log.info("角色开场白预生成完成，成功: {}/{}", successCount, activeCharacters.size());
                        })
                        .doOnError(error -> log.error("角色开场白预生成过程中发生错误", error));
                finalResultMono.subscribe();

            } catch (Exception e) {
                log.error("预生成角色开场白失败", e);
            }
        };
    }
}

