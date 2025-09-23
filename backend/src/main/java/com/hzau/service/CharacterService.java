package com.hzau.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hzau.entity.Character;
import com.hzau.mapper.CharacterMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @projectName: AI-roleplay
 * @package: com.hzau.service
 * @className: CharacterService
 * @author: zhuyuchen
 * @description: TODO
 * @date: 2025/9/23 下午3:00
 */
@Slf4j
@Service
public class CharacterService extends ServiceImpl<CharacterMapper, Character> {

    /**
     * 获取所有激活的角色列表
     * @return 角色列表
     */
    public List<Character> getActiveCharacters() {
        log.info("获取所有激活的角色列表");
        QueryWrapper<Character> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_active", true)
                .orderBy(true, true, "created_at");
        return this.list(queryWrapper);
    }

    /**
     * 根据ID获取角色详情
     * @param characterId 角色ID
     * @return 角色详情
     */
    public Character getCharacterById(Long characterId) {
        log.info("获取角色详情, characterId: {}", characterId);
        QueryWrapper<Character> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id", characterId)
                .eq("is_active", true);
        return this.getOne(queryWrapper);
    }

    /**
     * 根据名称搜索角色
     * @param name 角色名称关键词
     * @return 匹配的角色列表
     */
    public List<Character> searchCharactersByName(String name) {
        log.info("根据名称搜索角色, name: {}", name);
        QueryWrapper<Character> queryWrapper = new QueryWrapper<>();
        queryWrapper.like("name", name)
                .eq("is_active", true)
                .orderBy(true, true, "created_at");
        return this.list(queryWrapper);
    }

    /**
     * 根据标签搜索角色
     * @param tag 标签关键词
     * @return 匹配的角色列表
     */
    public List<Character> searchCharactersByTag(String tag) {
        log.info("根据标签搜索角色, tag: {}", tag);
        QueryWrapper<Character> queryWrapper = new QueryWrapper<>();
        queryWrapper.like("tags", tag)
                .eq("is_active", true)
                .orderBy(true, true, "created_at");
        return this.list(queryWrapper);
    }

    /**
     * 检查角色是否存在且激活
     * @param characterId 角色ID
     * @return 是否存在且激活
     */
    public boolean isCharacterActiveById(Long characterId) {
        log.info("检查角色是否存在且激活, characterId: {}", characterId);
        QueryWrapper<Character> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id", characterId)
                .eq("is_active", true);
        return this.count(queryWrapper) > 0;
    }

    /**
     * 根据关键词搜索角色（名称或标签）
     * @param keyword 搜索关键词
     * @return 匹配的角色列表
     */
    public List<Character> searchCharacters(String keyword) {
        log.info("根据关键词搜索角色, keyword: {}", keyword);
        if (keyword == null || keyword.trim().isEmpty()) {
            return getActiveCharacters();
        }
        
        QueryWrapper<Character> queryWrapper = new QueryWrapper<>();
        queryWrapper.and(wrapper -> wrapper
                .like("name", keyword)
                .or()
                .like("tags", keyword))
                .eq("is_active", true)
                .orderBy(true, true, "created_at");
        return this.list(queryWrapper);
    }

    /**
     * 获取所有激活的角色列表（用于开场白预生成）
     * @return 所有激活的角色列表
     */
    public List<Character> getAllActiveCharacters() {
        log.info("获取所有激活的角色列表（用于开场白预生成）");
        return getActiveCharacters();
    }
}

