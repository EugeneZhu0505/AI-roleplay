package com.hzau.controller;

import com.hzau.common.Result;
import com.hzau.entity.Character;
import com.hzau.service.CharacterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @projectName: AI-roleplay
 * @package: com.hzau.controller
 * @className: CharacterController
 * @author: zhuyuchen
 * @description: TODO
 * @date: 2025/9/23 下午3:38
 */
@RestController
@RequestMapping("/api/characters")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "角色管理", description = "AI角色相关接口")
public class CharacterController {

    private final CharacterService characterService;

    /**
     * 获取所有激活的角色列表
     */
    @GetMapping
    @Operation(summary = "获取角色列表", description = "获取所有激活的AI角色列表")
    public Result<List<Character>> getActiveCharacters() {
        try {
            List<Character> characters = characterService.getActiveCharacters();
            return Result.success(characters);
        } catch (Exception e) {
            log.error("获取角色列表失败", e);
            return Result.fail(500, "获取角色列表失败");
        }
    }

    /**
     * 根据ID获取角色详情
     */
    @GetMapping("/{characterId}")
    @Operation(summary = "获取角色详情", description = "根据ID获取指定角色的详细信息")
    public Result<Character> getCharacterById(
            @Parameter(description = "角色ID", required = true)
            @PathVariable Long characterId) {
        try {
            Character character = characterService.getCharacterById(characterId);
            if (character == null) {
                return Result.fail(404, "角色不存在");
            }
            return Result.success(character);
        } catch (Exception e) {
            log.error("获取角色详情失败, characterId: {}", characterId, e);
            return Result.fail(500, "获取角色详情失败");
        }
    }

    /**
     * 根据名称搜索角色
     */
    @GetMapping("/search")
    @Operation(summary = "搜索角色", description = "根据名称关键词搜索角色")
    public Result<List<Character>> searchCharactersByName(
            @Parameter(description = "角色名称关键词", required = true)
            @RequestParam String name) {
        try {
            List<Character> characters = characterService.searchCharactersByName(name);
            return Result.success(characters);
        } catch (Exception e) {
            log.error("搜索角色失败, name: {}", name, e);
            return Result.fail(500, "搜索角色失败");
        }
    }

    /**
     * 根据标签搜索角色
     */
    @GetMapping("/search/tag")
    @Operation(summary = "按标签搜索角色", description = "根据标签关键词搜索角色")
    public Result<List<Character>> searchCharactersByTag(
            @Parameter(description = "标签关键词", required = true)
            @RequestParam String tag) {
        try {
            List<Character> characters = characterService.searchCharactersByTag(tag);
            return Result.success(characters);
        } catch (Exception e) {
            log.error("按标签搜索角色失败, tag: {}", tag, e);
            return Result.fail(500, "按标签搜索角色失败");
        }
    }
}
