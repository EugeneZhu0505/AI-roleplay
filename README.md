# AI-roleplay

## 后端项目架构

backend/src/main/java/com/hzau/ai/roleplay/
├── config/          # 配置类
├── controller/      # 控制器层
├── service/         # 服务层
├── mapper/          # 数据访问层
├── entity/          # 实体类
├── dto/             # 数据传输对象
├── vo/              # 视图对象
├── common/          # 公共组件
│   ├── exception/   # 异常处理
│   ├── utils/       # 工具类
│   └── constants/   # 常量定义
├── security/        # 安全相关
└── websocket/       # WebSocket处理

backend/src/main/resources/
├── mapper/          # MyBatis XML文件
├── static/          # 静态资源
└── templates/       # 模板文件