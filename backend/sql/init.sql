-- 创建数据库
CREATE DATABASE IF NOT EXISTS ai_roleplay CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE ai_roleplay;

-- 创建用户表
CREATE TABLE IF NOT EXISTS `user` (
    `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username` varchar(50) NOT NULL COMMENT '用户名',
    `password_hash` varchar(255) NOT NULL COMMENT '密码哈希',
    `avatar_url` varchar(255) DEFAULT NULL COMMENT '头像URL',
    `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` tinyint(1) DEFAULT 0 COMMENT '逻辑删除标志',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 插入测试用户数据 (密码为 123456 的BCrypt加密结果)
INSERT INTO `user` (`username`, `password_hash`) VALUES 
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iYqiSfFVMLVZqpjn/6M.ltU6Td4e'),
('testuser', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iYqiSfFVMLVZqpjn/6M.ltU6Td4e');
