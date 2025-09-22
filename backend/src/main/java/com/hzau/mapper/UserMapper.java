package com.hzau.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hzau.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * @projectName: AI-roleplay
 * @package: com.hzau.mapper
 * @className: UserMapper
 * @author: zhuyuchen
 * @description: TODO
 * @date: 2025/9/22 下午4:13
 */

@Mapper
public interface UserMapper extends BaseMapper<User> {
}

