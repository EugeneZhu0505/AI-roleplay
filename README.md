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
│   ├── audio/                # 音频文件
│   ├── image/                # 图片文件
│   └── video/                # 视频文件
└── logs/                      # 日志文件
```

## 技术栈

### 后端
- **框架**: Spring Boot 3.4.10 + WebFlux (响应式编程)
- **数据库**: MySQL 8.0 + MyBatis Plus
- **缓存**: Redis
- **安全**: Spring Security + JWT
- **AI服务**: 七牛云AI API
- **构建工具**: Maven

### 前端
- **框架**: React 19.1.1
- **UI组件**: Ant Design + Material-UI
- **路由**: React Router DOM
- **3D渲染**: Three.js + React Three Fiber
- **构建工具**: Create React App

## 环境要求

- **Java**: JDK 17+
- **Node.js**: 16.0+
- **MySQL**: 8.0+
- **Redis**: 6.0+
- **Maven**: 3.6+

## 快速启动

### 1. 环境准备

#### 1.1 Windows环境安装

**安装JDK 17**
```powershell
# 方法1: 使用Chocolatey (推荐)
choco install openjdk17

# 方法2: 手动下载安装
# 访问 https://adoptium.net/ 下载JDK 17
# 安装后配置环境变量 JAVA_HOME 和 PATH
```

**安装Node.js**
```powershell
# 方法1: 使用Chocolatey
choco install nodejs

# 方法2: 官网下载
# 访问 https://nodejs.org/ 下载LTS版本
```

**安装MySQL 8.0**
```powershell
# 方法1: 使用Chocolatey
choco install mysql

# 方法2: 官网下载MySQL Installer
# 访问 https://dev.mysql.com/downloads/installer/
# 选择MySQL Server 8.0
```

**安装Redis**
```powershell
# 方法1: 使用Chocolatey
choco install redis-64

# 方法2: 下载Windows版本
# 访问 https://github.com/microsoftarchive/redis/releases
# 下载Redis-x64-*.msi安装包
```

**安装Maven**
```powershell
# 方法1: 使用Chocolatey
choco install maven

# 方法2: 手动安装
# 访问 https://maven.apache.org/download.cgi
# 下载后配置环境变量 MAVEN_HOME 和 PATH
```

**验证安装**
```powershell
java -version
node -v
npm -v
mysql --version
redis-server --version
mvn -version
```

#### 1.2 Ubuntu环境安装

**更新系统包**
```bash
sudo apt update && sudo apt upgrade -y
```

**安装JDK 17**
```bash
# 安装OpenJDK 17
sudo apt install openjdk-17-jdk -y

# 配置JAVA_HOME
echo 'export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64' >> ~/.bashrc
echo 'export PATH=$PATH:$JAVA_HOME/bin' >> ~/.bashrc
source ~/.bashrc
```

**安装Node.js**
```bash
# 方法1: 使用NodeSource仓库 (推荐)
curl -fsSL https://deb.nodesource.com/setup_18.x | sudo -E bash -
sudo apt-get install -y nodejs

# 方法2: 使用snap
sudo snap install node --classic
```

**安装MySQL 8.0**
```bash
# 安装MySQL Server
sudo apt install mysql-server -y

# 启动MySQL服务
sudo systemctl start mysql
sudo systemctl enable mysql

# 安全配置
sudo mysql_secure_installation
```

**安装Redis**
```bash
# 安装Redis
sudo apt install redis-server -y

# 启动Redis服务
sudo systemctl start redis-server
sudo systemctl enable redis-server

# 测试Redis
redis-cli ping
```

**安装Maven**
```bash
# 安装Maven
sudo apt install maven -y

# 或者手动安装最新版本
# wget https://downloads.apache.org/maven/maven-3/3.9.6/binaries/apache-maven-3.9.6-bin.tar.gz
# sudo tar xzf apache-maven-3.9.6-bin.tar.gz -C /opt
# sudo ln -s /opt/apache-maven-3.9.6 /opt/maven
```

**验证安装**
```bash
java -version
node -v
npm -v
mysql --version
redis-server --version
mvn -version
```

#### 1.3 数据库初始化

**Windows环境**
```powershell
# 启动MySQL服务
net start mysql

# 连接MySQL并创建数据库
mysql -u root -p
CREATE DATABASE ai_roleplay CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
exit

# 执行初始化脚本
mysql -u root -p ai_roleplay < backend\sql\init.sql
```

**Ubuntu环境**
```bash
# 确保MySQL服务运行
sudo systemctl status mysql

# 连接MySQL并创建数据库
sudo mysql -u root -p
CREATE DATABASE ai_roleplay CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
exit

# 执行初始化脚本
mysql -u root -p ai_roleplay < backend/sql/init.sql
```

#### 1.4 Redis启动

**Windows环境**
```powershell
# 启动Redis服务
redis-server

# 或者作为Windows服务启动
net start redis

# 测试Redis连接
redis-cli ping
```

**Ubuntu环境**
```bash
# 启动Redis服务
sudo systemctl start redis-server

# 设置开机自启
sudo systemctl enable redis-server

# 测试Redis连接
redis-cli ping
```

### 2. 后端启动

#### 2.1 环境变量配置（可选）

**Windows环境 (PowerShell)**
```powershell
# 数据库配置
$env:DB_HOST="localhost"
$env:DB_PORT="3306"
$env:DB_NAME="ai_roleplay"
$env:DB_USERNAME="root"
$env:DB_PASSWORD="12345678"

# Redis配置
$env:REDIS_HOST="localhost"
$env:REDIS_PORT="6379"
$env:REDIS_PASSWORD=""

# 七牛云AI配置
$env:QINIU_AI_API_KEY="your_api_key_here"
```

**Ubuntu环境**
```bash
# 数据库配置
export DB_HOST=localhost
export DB_PORT=3306
export DB_NAME=ai_roleplay
export DB_USERNAME=root
export DB_PASSWORD=12345678

# Redis配置
export REDIS_HOST=localhost
export REDIS_PORT=6379
export REDIS_PASSWORD=

# 七牛云AI配置
export QINIU_AI_API_KEY=your_api_key_here

# 将环境变量写入配置文件（可选）
echo 'export DB_HOST=localhost' >> ~/.bashrc
echo 'export DB_PORT=3306' >> ~/.bashrc
echo 'export DB_NAME=ai_roleplay' >> ~/.bashrc
echo 'export DB_USERNAME=root' >> ~/.bashrc
echo 'export DB_PASSWORD=12345678' >> ~/.bashrc
source ~/.bashrc
```

#### 2.2 使用Maven启动
```bash
cd backend

# 安装依赖
mvn clean install

# 启动应用
mvn spring-boot:run
```

#### 2.3 使用IDEA启动
1. 打开IDEA，导入backend项目
2. 等待Maven依赖下载完成
3. 找到 `com.hzau.BackendApplication` 主类
4. 右键选择 "Run BackendApplication"

#### 2.4 使用JAR包启动
```bash
cd backend

# 打包
mvn clean package -DskipTests

# 运行
java -jar target/ai-roleplay-0.0.1-SNAPSHOT.jar
```

**后端启动成功标志**:
- 控制台显示 "Started BackendApplication"
- 访问 http://localhost:8080/swagger-ui.html 可以看到API文档
- 默认端口: 8080

### 3. 前端启动

#### 3.1 安装依赖
```bash
cd front-end

# 安装npm依赖
npm install
```

#### 3.2 启动开发服务器
```bash
# 启动React开发服务器
npm start
```

#### 3.3 构建生产版本
```bash
# 构建生产版本
npm run build

# 构建完成后，build目录包含可部署的静态文件
```

**前端启动成功标志**:
- 控制台显示 "webpack compiled with 0 errors"
- 自动打开浏览器访问 http://localhost:3000
- 默认端口: 3000

### 4. 完整启动流程

#### 4.1 一键启动脚本（推荐）

**Windows (start.bat)**:
```batch
@echo off
echo ========================================
echo     启动AI角色扮演项目 (Windows)
echo ========================================

echo.
echo [1/5] 检查环境...
where java >nul 2>nul
if %errorlevel% neq 0 (
    echo 错误: 未找到Java，请先安装JDK 17
    pause
    exit /b 1
)

where node >nul 2>nul
if %errorlevel% neq 0 (
    echo 错误: 未找到Node.js，请先安装Node.js
    pause
    exit /b 1
)

where mvn >nul 2>nul
if %errorlevel% neq 0 (
    echo 错误: 未找到Maven，请先安装Maven
    pause
    exit /b 1
)

echo 环境检查通过！

echo.
echo [2/5] 启动MySQL服务...
net start mysql >nul 2>nul
if %errorlevel% neq 0 (
    echo 警告: MySQL服务启动失败，请手动启动MySQL
)

echo.
echo [3/5] 启动Redis服务...
start /min redis-server
timeout /t 3 >nul

echo.
echo [4/5] 启动后端服务...
cd backend
start "后端服务" cmd /k "mvn spring-boot:run"

echo.
echo [5/5] 等待后端启动完成...
timeout /t 45

echo.
echo [6/6] 启动前端服务...
cd ..\front-end
start "前端服务" cmd /k "npm start"

echo.
echo ========================================
echo          项目启动完成！
echo ========================================
echo 前端地址: http://localhost:3000
echo 后端API: http://localhost:8080
echo API文档: http://localhost:8080/swagger-ui.html
echo.
echo 按任意键退出...
pause >nul
```

**Ubuntu (start.sh)**:
```bash
#!/bin/bash

echo "========================================"
echo "    启动AI角色扮演项目 (Ubuntu)"
echo "========================================"

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 检查命令是否存在
check_command() {
    if ! command -v $1 &> /dev/null; then
        echo -e "${RED}错误: 未找到 $1，请先安装 $1${NC}"
        exit 1
    fi
}

echo
echo -e "${BLUE}[1/6] 检查环境...${NC}"
check_command java
check_command node
check_command npm
check_command mvn
check_command mysql
check_command redis-server
echo -e "${GREEN}环境检查通过！${NC}"

echo
echo -e "${BLUE}[2/6] 启动MySQL服务...${NC}"
sudo systemctl start mysql
if [ $? -eq 0 ]; then
    echo -e "${GREEN}MySQL服务启动成功${NC}"
else
    echo -e "${YELLOW}警告: MySQL服务启动失败，请检查MySQL配置${NC}"
fi

echo
echo -e "${BLUE}[3/6] 启动Redis服务...${NC}"
sudo systemctl start redis-server
if [ $? -eq 0 ]; then
    echo -e "${GREEN}Redis服务启动成功${NC}"
else
    echo -e "${YELLOW}警告: Redis服务启动失败，请检查Redis配置${NC}"
fi

echo
echo -e "${BLUE}[4/6] 检查前端依赖...${NC}"
cd front-end
if [ ! -d "node_modules" ]; then
    echo -e "${YELLOW}正在安装前端依赖...${NC}"
    npm install
fi

echo
echo -e "${BLUE}[5/6] 启动后端服务...${NC}"
cd ../backend
gnome-terminal --title="后端服务" -- bash -c "mvn spring-boot:run; exec bash" 2>/dev/null || \
xterm -title "后端服务" -e "mvn spring-boot:run; bash" 2>/dev/null || \
konsole --title "后端服务" -e bash -c "mvn spring-boot:run; exec bash" 2>/dev/null || \
(echo -e "${YELLOW}无法打开新终端，在后台启动后端...${NC}"; nohup mvn spring-boot:run > ../logs/backend.log 2>&1 &)

echo
echo -e "${BLUE}[6/6] 等待后端启动完成...${NC}"
sleep 45

echo
echo -e "${BLUE}启动前端服务...${NC}"
cd ../front-end
gnome-terminal --title="前端服务" -- bash -c "npm start; exec bash" 2>/dev/null || \
xterm -title "前端服务" -e "npm start; bash" 2>/dev/null || \
konsole --title "前端服务" -e bash -c "npm start; exec bash" 2>/dev/null || \
(echo -e "${YELLOW}无法打开新终端，在后台启动前端...${NC}"; nohup npm start > ../logs/frontend.log 2>&1 &)

echo
echo -e "${GREEN}========================================"
echo "         项目启动完成！"
echo "========================================${NC}"
echo -e "${BLUE}前端地址:${NC} http://localhost:3000"
echo -e "${BLUE}后端API:${NC} http://localhost:8080"
echo -e "${BLUE}API文档:${NC} http://localhost:8080/swagger-ui.html"
echo
echo -e "${YELLOW}提示: 如果服务未正常启动，请检查日志文件：${NC}"
echo -e "  后端日志: logs/backend.log"
echo -e "  前端日志: logs/frontend.log"
echo

# 等待用户输入
read -p "按回车键退出..."
```

**使用方法**:

Windows:
```powershell
# 创建并运行启动脚本
# 将上述内容保存为 start.bat
# 双击运行或在PowerShell中执行:
.\start.bat
```

Ubuntu:
```bash
# 创建并运行启动脚本
# 将上述内容保存为 start.sh
chmod +x start.sh
./start.sh
```

#### 4.2 手动启动步骤
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
   npm start
   ```

4. **验证启动**
   - 前端: http://localhost:3000
   - 后端API: http://localhost:8080
   - API文档: http://localhost:8080/swagger-ui.html

## 配置说明

### 后端配置
主要配置文件: `backend/src/main/resources/application.yml`

- **数据库配置**: 修改MySQL连接信息
- **Redis配置**: 修改Redis连接信息  
- **七牛云AI配置**: 配置API密钥
- **端口配置**: 默认8080，可通过 `server.port` 修改

### 前端配置
主要配置文件: `front-end/.env`

- **API地址配置**: 配置后端API地址
- **端口配置**: 默认3000，可通过 `PORT` 环境变量修改

## 常见问题

### 1. 后端启动失败

#### Windows环境
- **数据库连接失败**: 
  ```powershell
  # 检查MySQL服务状态
  sc query mysql
  
  # 启动MySQL服务
  net start mysql
  
  # 检查端口占用
  netstat -ano | findstr :3306
  ```

- **Redis连接失败**: 
  ```powershell
  # 检查Redis服务状态
  sc query redis
  
  # 启动Redis服务
  net start redis
  
  # 或手动启动
  redis-server
  ```

- **端口占用**: 
  ```powershell
  # 查看8080端口占用
  netstat -ano | findstr :8080
  
  # 杀死占用进程 (PID为进程ID)
  taskkill /PID <PID> /F
  ```

- **Maven依赖下载失败**:
  ```powershell
  # 清理Maven缓存
  mvn clean
  
  # 使用阿里云镜像 (修改 settings.xml)
  # 或临时使用镜像
  mvn clean install -Dmaven.repo.local=D:\maven-repo
  ```

#### Ubuntu环境
- **数据库连接失败**: 
  ```bash
  # 检查MySQL服务状态
  sudo systemctl status mysql
  
  # 启动MySQL服务
  sudo systemctl start mysql
  
  # 重置MySQL密码
  sudo mysql_secure_installation
  
  # 检查端口占用
  sudo netstat -tlnp | grep :3306
  ```

- **Redis连接失败**: 
  ```bash
  # 检查Redis服务状态
  sudo systemctl status redis-server
  
  # 启动Redis服务
  sudo systemctl start redis-server
  
  # 检查Redis配置
  sudo nano /etc/redis/redis.conf
  ```

- **端口占用**: 
  ```bash
  # 查看8080端口占用
  sudo netstat -tlnp | grep :8080
  
  # 杀死占用进程
  sudo kill -9 <PID>
  ```

- **权限问题**:
  ```bash
  # 给予执行权限
  chmod +x mvnw
  
  # 修改文件所有者
  sudo chown -R $USER:$USER ./backend
  ```

### 2. 前端启动失败

#### Windows环境
- **依赖安装失败**: 
  ```powershell
  # 清理npm缓存
  npm cache clean --force
  
  # 删除node_modules重新安装
  rmdir /s node_modules
  del package-lock.json
  npm install
  
  # 使用淘宝镜像
  npm config set registry https://registry.npmmirror.com
  npm install
  ```

- **端口占用**: 
  ```powershell
  # 查看3000端口占用
  netstat -ano | findstr :3000
  
  # 杀死占用进程
  taskkill /PID <PID> /F
  
  # 或使用其他端口启动
  set PORT=3001 && npm start
  ```

#### Ubuntu环境
- **依赖安装失败**: 
  ```bash
  # 清理npm缓存
  npm cache clean --force
  
  # 删除node_modules重新安装
  rm -rf node_modules package-lock.json
  npm install
  
  # 使用淘宝镜像
  npm config set registry https://registry.npmmirror.com
  npm install
  
  # 如果权限问题，修改npm全局目录
  mkdir ~/.npm-global
  npm config set prefix '~/.npm-global'
  echo 'export PATH=~/.npm-global/bin:$PATH' >> ~/.bashrc
  source ~/.bashrc
  ```

- **端口占用**: 
  ```bash
  # 查看3000端口占用
  sudo netstat -tlnp | grep :3000
  
  # 杀死占用进程
  sudo kill -9 <PID>
  
  # 或使用其他端口启动
  PORT=3001 npm start
  ```

### 3. 跨域问题
后端已配置CORS，如遇跨域问题：

#### Windows环境
```powershell
# 检查防火墙设置
netsh advfirewall show allprofiles

# 临时关闭防火墙测试
netsh advfirewall set allprofiles state off
```

#### Ubuntu环境
```bash
# 检查防火墙状态
sudo ufw status

# 开放端口
sudo ufw allow 8080
sudo ufw allow 3000
```

### 4. 环境变量问题

#### Windows环境
```powershell
# 查看环境变量
echo $env:JAVA_HOME
echo $env:PATH

# 永久设置环境变量
[Environment]::SetEnvironmentVariable("JAVA_HOME", "C:\Program Files\Java\jdk-17", "Machine")
```

#### Ubuntu环境
```bash
# 查看环境变量
echo $JAVA_HOME
echo $PATH

# 永久设置环境变量
echo 'export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64' >> ~/.bashrc
source ~/.bashrc
```

### 5. 性能优化

#### Windows环境
```powershell
# 增加JVM内存
set MAVEN_OPTS=-Xmx2048m -Xms1024m
mvn spring-boot:run

# 关闭Windows Defender实时保护（临时）
# 在Windows安全中心手动关闭
```

#### Ubuntu环境
```bash
# 增加JVM内存
export MAVEN_OPTS="-Xmx2048m -Xms1024m"
mvn spring-boot:run

# 优化系统性能
echo 'vm.swappiness=10' | sudo tee -a /etc/sysctl.conf
sudo sysctl -p
```

## 开发说明

### 代码结构
- **后端**: 采用分层架构，Controller -> Service -> Repository
- **前端**: 采用组件化开发，使用React Hooks

### API文档
启动后端后访问: http://localhost:8080/swagger-ui.html

### 数据库设计
详见: `backend/sql/init.sql`

## 部署说明

### 生产环境部署
1. **后端**: 打包为JAR文件，使用java -jar运行
2. **前端**: 构建静态文件，部署到Nginx等Web服务器
3. **数据库**: 配置生产环境MySQL和Redis

### Docker部署（待完善）
项目支持Docker容器化部署，相关配置文件正在完善中。