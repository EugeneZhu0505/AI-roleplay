package com.hzau.controller;

import com.hzau.common.Result;
import com.hzau.dto.CharacterCreateReq;
import com.hzau.entity.AiCharacter;
import com.hzau.service.CharacterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
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
    public Result<List<AiCharacter>> getActiveCharacters() {
        try {
            List<AiCharacter> characters = characterService.getActiveCharacters();
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
    public Result<AiCharacter> getCharacterById(
            @Parameter(description = "角色ID", required = true)
            @PathVariable Long characterId) {
        try {
            AiCharacter character = characterService.getCharacterById(characterId);
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
    public Result<List<AiCharacter>> searchCharactersByName(
            @Parameter(description = "角色名称关键词", required = true)
            @RequestParam String name) {
        try {
            List<AiCharacter> characters = characterService.searchCharactersByName(name);
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
    public Result<List<AiCharacter>> searchCharactersByTag(
            @Parameter(description = "标签关键词", required = true)
            @RequestParam String tag) {
        try {
            List<AiCharacter> characters = characterService.searchCharactersByTag(tag);
            return Result.success(characters);
        } catch (Exception e) {
            log.error("按标签搜索角色失败, tag: {}", tag, e);
            return Result.fail(500, "按标签搜索角色失败");
        }
    }

    /**
     * 创建新的AI角色
     */
    @PostMapping
    @Operation(summary = "创建AI角色", description = "创建一个新的AI角色")
    public Result<AiCharacter> createCharacter(
            @Parameter(description = "角色创建请求", required = true)
            @Valid @RequestBody CharacterCreateReq request) {
        try {
            // 将DTO转换为实体对象
            AiCharacter character = new AiCharacter();
            BeanUtils.copyProperties(request, character);

            // 调用服务层创建角色
            AiCharacter createdCharacter = characterService.createCharacter(character);
            return Result.success(createdCharacter);
        } catch (RuntimeException e) {
            log.error("创建角色失败: {}", e.getMessage());
            return Result.fail(400, e.getMessage());
        } catch (Exception e) {
            log.error("创建角色失败", e);
            return Result.fail(500, "创建角色失败");
        }
    }

    /**
     * 根据分类获取角色列表
     */
    @GetMapping("/category/{category}")
    @Operation(summary = "按分类获取角色", description = "根据分类获取角色列表 (0-动漫, 1-影视, 2-历史, 3-科普)")
    public Result<List<AiCharacter>> getCharactersByCategory(
            @Parameter(description = "角色分类", required = true)
            @PathVariable Integer category) {
        try {
            // 验证分类参数
            if (category < 0 || category > 3) {
                return Result.fail(400, "分类参数无效，必须是0-3之间的数字");
            }
            
            List<AiCharacter> characters = characterService.getCharactersByCategory(category);
            return Result.success(characters);
        } catch (Exception e) {
            log.error("按分类获取角色失败, category: {}", category, e);
            return Result.fail(500, "按分类获取角色失败");
        }
    }
}
