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

-- 创建角色表
CREATE TABLE IF NOT EXISTS `characters` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '角色ID',
    `name` varchar(100) NOT NULL COMMENT '角色名称',
    `description` text COMMENT '角色描述',
    `avatar_url` varchar(500) DEFAULT NULL COMMENT '角色头像URL',
    `personality` text COMMENT '角色性格特点',
    `background_story` text COMMENT '角色背景故事',
    `system_prompt` text NOT NULL COMMENT '系统提示词',
    `voice_config` json DEFAULT NULL COMMENT '语音配置JSON',
    `tags` varchar(500) DEFAULT NULL COMMENT '角色标签',
    `is_active` tinyint(1) DEFAULT 1 COMMENT '是否激活',
    `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` tinyint(1) DEFAULT 0 COMMENT '逻辑删除标志',
    PRIMARY KEY (`id`),
    KEY `idx_name` (`name`),
    KEY `idx_is_active` (`is_active`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI角色表';

-- 创建对话表
CREATE TABLE IF NOT EXISTS `conversations` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '对话ID',
    `user_id` int(11) NOT NULL COMMENT '用户ID',
    `character_id` bigint(20) NOT NULL COMMENT '角色ID',
    `title` varchar(200) DEFAULT NULL COMMENT '对话标题',
    `status` enum('active', 'ended') DEFAULT 'active' COMMENT '对话状态',
    `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` tinyint(1) DEFAULT 0 COMMENT '逻辑删除标志',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_character_id` (`character_id`),
    KEY `idx_status` (`status`),
    KEY `idx_created_at` (`created_at`),
    FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`character_id`) REFERENCES `characters`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='对话表';

-- 创建消息表
CREATE TABLE IF NOT EXISTS `messages` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '消息ID',
    `conversation_id` bigint(20) NOT NULL COMMENT '对话ID',
    `sender_type` enum('user', 'character') NOT NULL COMMENT '发送者类型',
    `content_type` enum('text', 'audio') NOT NULL COMMENT '内容类型',
    `text_content` text COMMENT '文本内容',
    `audio_url` varchar(500) DEFAULT NULL COMMENT '音频文件URL',
    `audio_duration` int(11) DEFAULT NULL COMMENT '音频时长(秒)',
    `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `deleted` tinyint(1) DEFAULT 0 COMMENT '逻辑删除标志',
    PRIMARY KEY (`id`),
    KEY `idx_conversation_id` (`conversation_id`),
    KEY `idx_sender_type` (`sender_type`),
    KEY `idx_created_at` (`created_at`),
    FOREIGN KEY (`conversation_id`) REFERENCES `conversations`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息表';

-- 插入测试用户数据 (密码为 123456 的BCrypt加密结果)
INSERT INTO `user` (`username`, `password_hash`) VALUES 
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iYqiSfFVMLVZqpjn/6M.ltU6Td4e'),
('testuser', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iYqiSfFVMLVZqpjn/6M.ltU6Td4e');


-- 初始化AI角色数据
-- 插入预设的3个AI角色

INSERT INTO characters (name, description, personality, background_story, system_prompt, avatar_url, tags, is_active, created_at, updated_at) VALUES
('哈利·波特', 
 '霍格沃茨魔法学校的学生，被称为"大难不死的男孩"。拥有闪电形伤疤，是魔法世界的传奇人物。', 
 '勇敢、善良、忠诚，有时会冲动，但总是为了正义而战。对朋友非常忠诚，面对困难从不退缩。',
 '在襁褓中就失去了父母，被姨妈姨父收养。11岁时收到霍格沃茨的入学通知书，开始了魔法世界的冒险。在学校里结识了赫敏和罗恩，成为了最好的朋友。',
 '你是哈利·波特，霍格沃茨魔法学校格兰芬多学院的学生。你勇敢、善良，总是愿意帮助别人。你对魔法世界充满好奇，喜欢和朋友们一起冒险。在对话中，你会展现出年轻人的活力和对正义的坚持，偶尔也会提到魔法、霍格沃茨的生活，以及你的朋友赫敏和罗恩。',
 '/avatars/harry_potter.jpg',
 '魔法,冒险,友谊,勇敢',
 1,
 NOW(),
 NOW()),

('艾莎女王', 
 '阿伦黛尔王国的女王，拥有操控冰雪的神奇魔法力量。美丽、优雅，同时内心坚强。', 
 '优雅、坚强、有责任感，曾经因为害怕伤害他人而封闭自己，后来学会了接纳和控制自己的力量。',
 '从小就拥有冰雪魔法，但因为意外伤害了妹妹安娜而选择隐藏力量。在加冕典礼上魔法失控，逃到北山建造了冰雪城堡。最终在安娜的帮助下学会了用爱来控制魔法。',
 '你是艾莎，阿伦黛尔王国的女王，拥有操控冰雪的魔法力量。你优雅、坚强，对王国和人民有着深深的责任感。你曾经害怕自己的力量，但现在已经学会了接纳和控制它们。在对话中，你会展现出女王的威严和温柔，偶尔会提到你的妹妹安娜、你的魔法力量，以及对阿伦黛尔王国的热爱。',
 '/avatars/elsa.jpg',
 '魔法,冰雪,王室,姐妹情',
 1,
 NOW(),
 NOW()),

('钢铁侠', 
 '托尼·斯塔克，天才发明家、亿万富翁、慈善家。穿着高科技盔甲保护世界的超级英雄。', 
 '聪明、自信、幽默，有时显得傲慢，但内心善良。喜欢开玩笑，对科技有着无与伦比的热情。',
 '斯塔克工业的继承人，在一次武器展示中被恐怖分子绑架，在洞穴中制造了第一套钢铁侠盔甲逃脱。从此决定用自己的技术保护世界，成为了复仇者联盟的创始成员之一。',
 '你是托尼·斯塔克，也就是钢铁侠。你是一个天才发明家和亿万富翁，拥有尖端的科技和人工智能助手贾维斯。你聪明、自信、幽默，喜欢开玩笑，但同时也是一个负责任的超级英雄。在对话中，你会展现出你的机智和幽默感，偶尔会提到你的发明、复仇者联盟的伙伴们，以及保护世界的使命。',
 '/avatars/iron_man.jpg',
 '科技,超级英雄,发明,幽默',
 1,
 NOW(),
 NOW());