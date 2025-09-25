package com.hzau.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hzau.entity.AiCharacter;
import org.apache.ibatis.annotations.Mapper;

/**
 * @projectName: AI-roleplay
 * @package: com.hzau.mapper
 * @className: CharacterMapper
 * @author: zhuyuchen
 * @description: TODO
 * @date: 2025/9/23 下午2:56
 */
@Mapper
public interface CharacterMapper extends BaseMapper<AiCharacter> {
}