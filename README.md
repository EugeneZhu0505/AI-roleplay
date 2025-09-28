# AI-roleplay

AI角色扮演网站，基于Spring Boot WebFlux + React的全栈项目。

## 项目结构

```
AI-roleplay/
├── backend/                    # 后端项目 (Spring Boot WebFlux)
│   ├── src/main/java/         # Java源码
│   ├── src/main/resources/    # 配置文件
│   ├── sql/                   # 数据库初始化脚本
│   └── pom.xml               # Maven配置文件
├── front-end/                 # 前端项目 (React)
│   ├── src/                   # React源码
│   ├── public/               # 静态资源
│   └── package.json          # npm配置文件
├── data/                      # 数据存储目录
└── logs/                      # 日志文件
```

## 技术栈

### 后端
- **框架**: Spring Boot 3.4.10 + WebFlux
- **数据库**: MySQL 8.0 + MyBatis Plus
- **缓存**: Redis
- **安全**: Spring Security + JWT
- **构建工具**: Maven

### 前端
- **框架**: React 19.1.1
- **UI组件**: Ant Design + Material-UI
- **构建工具**: Create React App

## 环境要求

需要安装以下软件：

- **Java**: JDK 17+
- **Node.js**: 16.0+
- **MySQL**: 8.0+
- **Redis**: 6.0+
- **Maven**: 3.6+

## 数据库初始化

1. 创建数据库：
```sql
CREATE DATABASE ai_roleplay CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. 执行初始化脚本：
```bash
mysql -u root -p ai_roleplay < backend/sql/init.sql
```

## 后端打包与启动

### 开发环境启动
```bash
cd backend
mvn spring-boot:run
```

### 生产环境打包
```bash
cd backend
mvn clean package -DskipTests
java -jar target/ai-roleplay-0.0.1-SNAPSHOT.jar
```

### 使用IDE启动
1. 打开IDEA，导入backend项目
2. 找到 `com.hzau.BackendApplication` 主类
3. 右键选择 "Run BackendApplication"

**后端启动成功后访问：**
- API地址: http://localhost:8080
- API文档: http://localhost:8080/swagger-ui.html

## 前端打包与启动

### 开发环境启动
```bash
cd front-end
npm install
npm start
```

### 生产环境打包
```bash
cd front-end
npm run build
```

**前端启动成功后访问：**
- 前端地址: http://localhost:3000

## 快速启动步骤

1. **启动基础服务**
   - 启动MySQL服务
   - 启动Redis服务

2. **启动后端**
   ```bash
   cd backend
   mvn spring-boot:run
   ```

3. **启动前端**
   ```bash
   cd front-end
   npm install
   npm start
   ```

4. **访问应用**
   - 前端: http://localhost:3000
   - 后端API: http://localhost:8080
   - API文档: http://localhost:8080/swagger-ui.html

## 配置说明

### 后端配置
配置文件: `backend/src/main/resources/application.yml`
- 数据库连接信息
- Redis连接信息
- 端口配置（默认8080）

### 前端配置
配置文件: `front-end/.env`
- 后端API地址配置
- 端口配置（默认3000）