package com.hzau.service;

import com.hzau.entity.Character;
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
                List<Character> activeCharacters = characterService.getAllActiveCharacters();
                log.info("找到 {} 个激活的角色", activeCharacters.size());

                if (activeCharacters.isEmpty()) {
                    log.warn("没有找到激活的角色，跳过开场白预生成");
                    return;
                }

                // 使用响应式编程批量预生成开场白
                Flux.fromIterable(activeCharacters)
                        .delayElements(Duration.ofSeconds(2)) // 每个请求间隔2秒，避免API限流
                        .flatMap(character -> {
                            log.info("预生成角色开场白: {} (ID: {})", character.getName(), character.getId());
                            return aiRoleplayService.getCharacterOpening(character.getId())
                                    .doOnSuccess(opening -> log.info("角色 {} 开场白预生成成功", character.getName()))
                                    .doOnError(error -> log.error("角色 {} 开场白预生成失败: {}", character.getName(), error.getMessage()))
                                    .onErrorResume(error -> {
                                        // 出错时返回空，继续处理下一个角色
                                        return Mono.empty();
                                    });
                        })
                        .collectList()
                        .doOnSuccess(results -> {
                            long successCount = results.stream().filter(result -> result != null && !result.isEmpty()).count();
                            log.info("角色开场白预生成完成，成功: {}/{}", successCount, activeCharacters.size());
                        })
                        .doOnError(error -> log.error("角色开场白预生成过程中发生错误", error))
                        .subscribe();

            } catch (Exception e) {
                log.error("预生成角色开场白失败", e);
            }
        };
    }
}

