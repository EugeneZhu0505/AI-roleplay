package com.hzau.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hzau.entity.Message;
import org.apache.ibatis.annotations.Mapper;

/**
 * @projectName: AI-roleplay
 * @package: com.hzau.mapper
 * @className: MessageMapper
 * @author: zhuyuchen
 * @description: TODO
 * @date: 2025/9/23 下午2:57
 */
@Mapper
public interface MessageMapper extends BaseMapper<Message> {
}