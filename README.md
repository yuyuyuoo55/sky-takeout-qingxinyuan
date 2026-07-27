# 清心园（Qingxinyuan）

本项目基于“苍穹外卖（Sky Take Out）”课程项目进行整理和简单定制，并非完全从零开发。

当前主要调整：

- 项目中文名称由“苍穹外卖”改为“清心园”
- 商家端主视觉调整为绿色与白色
- 整理本地部署结构和运行配置
- 对后端 JWT 验证代码进行了统一整理
- 补充只包含表结构、不包含业务数据的数据库脚本

## 当前状态

目前项目仍以原有餐饮外卖业务功能为主，尚未完成智能化升级。

我正在学习如何将 AI 能力应用到该项目中。后续只会根据实际学习和实现情况逐步更新，不会将尚未完成的功能写成已有成果。

> [!IMPORTANT]
> 公开仓库只提供数据库表结构，不包含本机的员工、用户、订单、菜品、套餐等业务数据。因此，首次初始化后商家端没有展示订单信息、员工信息等内容属于正常现象，并非前端或接口故障。请使用自行创建的测试数据进行功能验证。

## 项目结构

```text
backend/         Java 后端源码
merchant-admin/  商家管理端已构建静态文件和本地代理服务器
miniprogram/     微信小程序编译产物
database/        MySQL 表结构脚本
```

说明：

- 商家管理端当前保留的是已构建 `dist`，不是完整前端源码工程。
- 小程序当前保留的是 uni-app 编译后的 `mp-weixin`，不包含原始 `.vue` 工程。
- `project.config.json` 和 `project.private.config.json` 不上传，请在微信开发者工具中自行创建本地项目配置。

## 技术栈

- Java 17
- Spring Boot 2.7
- MyBatis
- MySQL 8
- Redis
- JWT
- Vue 2 商家管理端构建产物
- 微信小程序编译产物

## 后端启动

### 1. 初始化数据库

创建数据库：

```sql
CREATE DATABASE sky_take_out
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
```

然后导入：

```text
database/schema.sql
```

脚本只包含 11 张表的结构，不包含用户、订单或其他业务数据。

因此，新数据库中的业务表默认为空，前端列表没有订单、员工、用户、菜品和套餐等信息属于正常现象。

### 2. 配置环境变量

参考根目录的 `.env.example` 设置本机环境变量。不要把真实密码、Token 或密钥提交到仓库。

至少需要配置：

```text
DB_PASSWORD
REDIS_PASSWORD
JWT_ADMIN_SECRET
JWT_USER_SECRET
```

### 3. 构建并运行

```powershell
cd backend
mvn clean package -DskipTests
java -jar .\sky-server\target\sky-server-1.0-SNAPSHOT.jar
```

后端默认端口：

```text
http://127.0.0.1:8080
```

## 商家管理端

需要 Node.js，后端需先运行在 8080：

```powershell
cd merchant-admin
node .\server.js
```

访问：

```text
http://127.0.0.1:8888
```

本地服务器会将 `/api` 请求代理到后端 `/admin`，并处理工作台 WebSocket 地址。

## 微信小程序

使用微信开发者工具导入：

```text
miniprogram
```

本机模拟器可访问 `http://localhost:8080`。真机调试不能使用手机自身的 `localhost`，需要改为局域网地址或已配置的 HTTPS 域名。

## 已验证内容

- Maven 四模块构建成功
- Spring Boot 8080 启动成功
- MySQL 和 Redis 连接成功
- 管理员登录与 JWT 校验成功
- 分类分页数据库接口返回正常
- 商家端登录、工作台数据和 WebSocket 正常

## 个人职责边界

- 主要负责和学习：Java 后端、数据库表设计、SQL 编写
- 简单接触和整理：商家端、小程序端、本地部署

项目说明、GitHub 展示和简历描述均保持以上真实边界。

## 来源与许可证

本项目来源与个人修改范围见 [ATTRIBUTION.md](./ATTRIBUTION.md)。

商家端包含的 MIT License 见 [merchant-admin/LICENSE](./merchant-admin/LICENSE)。
