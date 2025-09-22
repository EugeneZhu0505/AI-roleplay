# AI角色扮演网站技术开发文档

## 项目概述

开发一个基于AI的角色扮演网站，用户可以搜索并与各种AI角色（如哈利波特、苏格拉底等）进行语音聊天。项目采用前后端分离架构，后端使用Java Spring Boot框架。

## 技术栈选型

### 后端技术栈
- **框架**: Spring Boot 3.x
- **数据库**: MySQL 8.0 + Redis 7.x
- **ORM**: MyBatis Plus
- **安全**: Spring Security + JWT
- **消息队列**: RabbitMQ
- **文件存储**: MinIO
- **API文档**: Swagger/OpenAPI 3
- **构建工具**: Maven

### 核心服务集成
- **LLM模型**: OpenAI GPT-4 API / 阿里云通义千问 API
- **语音识别**: 百度语音识别 API / 阿里云语音识别
- **TTS语音合成**: 百度TTS API / 阿里云语音合成
- **实时通信**: WebSocket

### 中间件和工具
- **缓存**: Redis (角色信息、会话缓存)
- **消息队列**: RabbitMQ (异步处理语音转换)
- **对象存储**: MinIO (音频文件存储)
- **监控**: Actuator + Micrometer
- **日志**: Logback + ELK Stack

## 系统架构设计

### 整体架构
```
前端 (Vue.js/React) 
    ↓ HTTP/WebSocket
后端服务层 (Spring Boot)
    ├── 用户管理服务
    ├── 角色管理服务  
    ├── 对话服务
    ├── 语音处理服务
    └── AI集成服务
    ↓
数据层 (MySQL + Redis)
外部服务 (LLM API + 语音API)
```

### 核心模块设计

#### 1. 用户管理模块
- 用户注册/登录
- JWT token管理
- 用户偏好设置

#### 2. 角色管理模块
- 角色信息管理
- 角色搜索功能
- 角色个性化配置

#### 3. 对话服务模块
- 会话管理
- 消息历史记录
- 实时通信

#### 4. 语音处理模块
- 语音上传处理
- 语音识别转文字
- 文字转语音合成

#### 5. AI集成模块
- LLM API调用封装
- 角色prompt管理
- 响应优化处理

## 数据库设计

### 核心表结构

```sql
-- 用户表
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    avatar_url VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 角色表
CREATE TABLE characters (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    avatar_url VARCHAR(500),
    personality TEXT,
    background_story TEXT,
    system_prompt TEXT NOT NULL,
    voice_config JSON,
    tags VARCHAR(500),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 会话表
CREATE TABLE conversations (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    character_id BIGINT NOT NULL,
    title VARCHAR(200),
    status ENUM('active', 'ended') DEFAULT 'active',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (character_id) REFERENCES characters(id)
);

-- 消息表
CREATE TABLE messages (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    conversation_id BIGINT NOT NULL,
    sender_type ENUM('user', 'character') NOT NULL,
    content_type ENUM('text', 'audio') NOT NULL,
    text_content TEXT,
    audio_url VARCHAR(500),
    audio_duration INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (conversation_id) REFERENCES conversations(id)
);
```

## API接口设计

### 认证相关
- `POST /api/auth/register` - 用户注册
- `POST /api/auth/login` - 用户登录
- `POST /api/auth/refresh` - 刷新token

### 角色相关
- `GET /api/characters` - 获取角色列表
- `GET /api/characters/search` - 搜索角色
- `GET /api/characters/{id}` - 获取角色详情

### 对话相关
- `POST /api/conversations` - 创建新对话
- `GET /api/conversations` - 获取用户对话列表
- `GET /api/conversations/{id}/messages` - 获取对话消息
- `POST /api/conversations/{id}/messages` - 发送消息
- `WebSocket /ws/chat/{conversationId}` - 实时聊天

### 语音相关
- `POST /api/voice/upload` - 上传语音文件
- `POST /api/voice/synthesize` - 文字转语音

## 7天开发计划

### Day 1: 项目初始化和基础架构
**目标**: 搭建项目基础框架和开发环境

**任务清单**:
1. 创建Spring Boot项目结构
2. 配置Maven依赖和数据库连接
3. 设置Redis和基础配置
4. 创建基础实体类和数据库表
5. 配置Swagger API文档
6. 实现基础的异常处理和响应封装

**技术要点**:
- 使用Spring Boot Starter快速搭建
- 配置多环境profile (dev/test/prod)
- 集成MyBatis Plus进行数据库操作
- 配置跨域和安全策略

### Day 2: 用户认证和角色管理
**目标**: 完成用户系统和角色管理功能

**任务清单**:
1. 实现用户注册/登录功能
2. 集成JWT token认证
3. 创建角色管理CRUD接口
4. 实现角色搜索功能
5. 添加基础的3个AI角色数据

**技术要点**:
- Spring Security配置JWT认证
- 密码加密存储(BCrypt)
- 角色数据预设和搜索优化
- Redis缓存角色信息

**预设角色**:
1. **哈利波特**: 勇敢的魔法师，霍格沃茨学生
2. **苏格拉底**: 古希腊哲学家，善于启发式提问
3. **夏洛克·福尔摩斯**: 天才侦探，逻辑推理专家

### Day 3: LLM集成和对话核心功能
**目标**: 集成LLM API并实现基础对话功能

**任务清单**:
1. 封装LLM API调用服务
2. 实现角色prompt管理
3. 创建对话会话管理
4. 实现文本消息发送和接收
5. 添加消息历史记录功能

**技术要点**:
- 使用RestTemplate/WebClient调用外部API
- API密钥安全管理(配置文件加密)
- 异步处理LLM响应
- 会话上下文管理和优化

**LLM API集成方案**:
```java
@Service
public class LLMService {
    // API配置
    private String apiKey = "${llm.api.key}";
    private String baseUrl = "${llm.api.url}";
    
    // 调用LLM生成回复
    public String generateResponse(String characterPrompt, String userMessage, List<String> history) {
        // 构建请求参数
        // 调用API
        // 处理响应
    }
}
```

### Day 4: 语音处理功能开发
**目标**: 实现语音识别和TTS功能

**任务清单**:
1. 集成语音识别API
2. 实现音频文件上传和存储
3. 集成TTS语音合成API
4. 实现语音消息的完整流程
5. 优化音频文件处理性能

**技术要点**:
- MinIO对象存储配置
- 音频格式转换和压缩
- 异步处理语音转换任务
- RabbitMQ消息队列处理

**语音处理流程**:
```
用户上传音频 → MinIO存储 → 语音识别API → 文本内容 → LLM处理 → TTS合成 → 返回音频URL
```

### Day 5: WebSocket实时通信
**目标**: 实现实时聊天功能

**任务清单**:
1. 配置WebSocket连接
2. 实现实时消息推送
3. 处理连接管理和心跳检测
4. 优化并发连接性能
5. 添加消息状态管理

**技术要点**:
- Spring WebSocket配置
- 会话管理和用户状态跟踪
- 消息广播和点对点通信
- 连接池和资源管理

### Day 6: 性能优化和缓存
**目标**: 优化系统性能和用户体验

**任务清单**:
1. 实现Redis缓存策略
2. 优化数据库查询性能
3. 添加接口限流和防护
4. 实现异步任务处理
5. 性能监控和日志完善

**技术要点**:
- Redis缓存角色信息和会话数据
- 数据库索引优化
- 接口限流(令牌桶算法)
- 异步处理耗时操作

### Day 7: 测试和部署准备
**目标**: 完善测试和部署配置

**任务清单**:
1. 编写单元测试和集成测试
2. API接口测试和文档完善
3. 配置生产环境参数
4. 性能测试和压力测试
5. 部署脚本和文档编写

**技术要点**:
- JUnit 5单元测试
- MockMvc集成测试
- Postman API测试集合
- Docker容器化配置

## 核心技术实现细节

### 1. LLM API调用管理

**API密钥存储方案**:
```yaml
# application.yml
llm:
  providers:
    openai:
      api-key: ${OPENAI_API_KEY:}
      base-url: https://api.openai.com/v1
      model: gpt-4
    qianwen:
      api-key: ${QIANWEN_API_KEY:}
      base-url: https://dashscope.aliyuncs.com/api/v1
      model: qwen-turbo
```

**调用封装**:
```java
@Component
public class LLMApiClient {
    @Value("${llm.providers.openai.api-key}")
    private String openaiApiKey;
    
    public CompletableFuture<String> generateResponse(LLMRequest request) {
        // 异步调用LLM API
        // 错误重试机制
        // 响应缓存
    }
}
```

### 2. 语音处理流程

**音频文件处理**:
- 支持格式: MP3, WAV, M4A
- 文件大小限制: 10MB
- 存储路径: `/audio/{userId}/{conversationId}/{messageId}.{ext}`

**语音识别集成**:
```java
@Service
public class SpeechRecognitionService {
    public String recognizeSpeech(MultipartFile audioFile) {
        // 1. 验证文件格式和大小
        // 2. 上传到MinIO
        // 3. 调用语音识别API
        // 4. 返回识别结果
    }
}
```

### 3. 实时通信架构

**WebSocket配置**:
```java
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(new ChatWebSocketHandler(), "/ws/chat/{conversationId}")
                .setAllowedOrigins("*");
    }
}
```

**消息处理流程**:
1. 用户发送消息 → WebSocket接收
2. 保存到数据库 → 异步处理
3. 调用LLM生成回复 → 实时推送
4. 语音合成(可选) → 推送音频URL

### 4. 缓存策略

**Redis缓存设计**:
- 角色信息: `character:{id}` (TTL: 1小时)
- 会话缓存: `conversation:{id}` (TTL: 30分钟)
- 用户token: `user:token:{userId}` (TTL: 24小时)
- API限流: `rate_limit:{userId}:{endpoint}` (TTL: 1分钟)

### 5. 安全和限流

**接口限流配置**:
```java
@Component
public class RateLimitInterceptor implements HandlerInterceptor {
    // 基于Redis的令牌桶算法
    // 不同接口不同限流策略
    // 用户级别和IP级别限流
}
```

## 部署和运维

### Docker配置
```dockerfile
FROM openjdk:17-jre-slim
COPY target/ai-roleplay-backend.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

### 环境变量配置
```bash
# 数据库配置
DB_HOST=localhost
DB_PORT=3306
DB_NAME=ai_roleplay
DB_USERNAME=root
DB_PASSWORD=password

# Redis配置
REDIS_HOST=localhost
REDIS_PORT=6379

# API密钥
OPENAI_API_KEY=your_openai_key
BAIDU_API_KEY=your_baidu_key
BAIDU_SECRET_KEY=your_baidu_secret
```

## 项目风险和应对方案

### 1. API调用风险
- **风险**: 第三方API限流或故障
- **应对**: 多供应商备份、本地缓存、降级策略

### 2. 性能风险
- **风险**: 高并发下响应延迟
- **应对**: 异步处理、缓存优化、负载均衡

### 3. 成本控制
- **风险**: API调用成本过高
- **应对**: 请求优化、缓存策略、用户限流

## 后续扩展计划

1. **多语言支持**: 国际化和本地化
2. **角色定制**: 用户自定义AI角色
3. **群聊功能**: 多角色同时对话
4. **情感分析**: 角色情感状态模拟
5. **移动端适配**: 响应式设计和PWA

---

**开发团队**: 后端开发
**项目周期**: 7天
**技术栈**: Java Spring Boot + MySQL + Redis + 第三方AI服务
**交付物**: 完整的后端API服务 + 技术文档 + 部署指南