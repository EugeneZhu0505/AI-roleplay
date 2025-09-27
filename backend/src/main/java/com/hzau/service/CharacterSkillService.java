package com.hzau.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hzau.entity.CharacterSkill;
import com.hzau.mapper.CharacterSkillMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @projectName: AI-roleplay
 * @package: com.hzau.service
 * @className: CharacterSkillService
 * @author: zhuyuchen
 * @description: TODO
 * @date: 2025/9/26 下午3:27
 */
@Slf4j
@Service
public class CharacterSkillService extends ServiceImpl<CharacterSkillMapper, CharacterSkill> {

    /**
     * 根据角色ID获取所有技能信息
     * @param characterId 角色ID
     * @return 角色技能列表
     */
    public List<CharacterSkill> getSkillsByCharacterId(Long characterId) {
        log.info("根据角色ID获取所有技能信息, characterId: {}", characterId);
        QueryWrapper<CharacterSkill> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("character_id", characterId);
        queryWrapper.eq("deleted", 0);
        return this.list(queryWrapper);
    }

    /**
     * 根据角色ID和技能名称获取特定技能
     * @param characterId 角色ID
     * @param skillName 技能名称
     * @return 角色技能信息
     */
    public CharacterSkill getSkillByCharacterIdAndName(Long characterId, String skillName) {
        log.info("根据角色ID和技能名称获取技能信息, characterId: {}, skillName: {}", characterId, skillName);
        QueryWrapper<CharacterSkill> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("character_id", characterId);
        queryWrapper.eq("skill_name", skillName);
        queryWrapper.eq("deleted", 0);
        return this.getOne(queryWrapper);
    }

    /**
     * 根据角色ID获取技能信息（兼容旧方法）
     * @param characterId 角色ID
     * @return 角色技能信息
     */
    @Deprecated
    public CharacterSkill getSkillByCharacterId(Long characterId) {
        log.info("根据角色ID获取技能信息（兼容方法）, characterId: {}", characterId);
        List<CharacterSkill> skills = getSkillsByCharacterId(characterId);
        return skills.isEmpty() ? null : skills.get(0);
    }

    /**
     * 检查角色是否有技能
     * @param characterId 角色ID
     * @return 是否有技能
     */
    public boolean hasSkill(Long characterId) {
        log.info("检查角色是否有技能, characterId: {}", characterId);
        QueryWrapper<CharacterSkill> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("character_id", characterId);
        queryWrapper.eq("deleted", 0);
        return this.count(queryWrapper) > 0;
    }
}
