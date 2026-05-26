# 便利店会员管理系统

基于 Spring Boot + Vue 3 前后端分离的便利店会员管理系统，支持多角色权限控制、会员管理、积分体系、优惠券、消费记录、数据统计等功能。

## 技术栈

| 层级 | 技术 |
|------|------|
| 后端 | Spring Boot 3.1.5、MyBatis-Plus、MySQL 8.0、Redis、JWT |
| 前端 | Vue 3.4、TypeScript、Element Plus 2.4、Vite 5、Pinia、ECharts |
| 认证 | 双 JWT 体系（会员端 + 管理端独立鉴权）、BCrypt 加密 |

## 项目结构

```
├── cs_member/                  # 后端 (Spring Boot)
│   ├── src/main/java/          # Java 源码
│   │   └── cloud/hilang/cs_member/
│   │       ├── controller/     # 接口层
│   │       ├── service/        # 业务层
│   │       ├── mapper/         # 数据访问层
│   │       ├── entity/         # 实体类
│   │       ├── dto/            # 数据传输对象
│   │       ├── config/         # 配置类 (安全、Redis、CORS等)
│   │       └── util/           # 工具类
│   └── src/main/resources/
│       ├── application.yml     # 主配置
│       └── mapper/             # MyBatis XML
├── cs_member_frontend/         # 前端 (Vue 3)
│   ├── src/
│   │   ├── api/                # 接口请求
│   │   ├── views/              # 页面组件
│   │   │   ├── admin/          # 管理端页面
│   │   │   └── member/         # 会员端页面
│   │   ├── layouts/            # 布局组件
│   │   ├── stores/             # Pinia 状态管理
│   │   ├── router/             # 路由配置
│   │   └── utils/              # 工具函数
│   └── vite.config.ts          # Vite 配置
├── cs_member.sql               # 数据库初始化脚本
└── README.md
```

## 环境要求

- JDK 17+
- Node.js 16+
- MySQL 8.0+
- Redis 6.0+
- Maven 3.8+

## 快速开始

### 1. 初始化数据库

```bash
# 创建数据库
mysql -u root -p -e "CREATE DATABASE cs_member CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;"

# 导入数据
mysql -u root -p cs_member < cs_member.sql
```

### 2. 配置后端

```bash
# 复制配置模板并填入实际值
cp cs_member/src/main/resources/application-example.yml cs_member/src/main/resources/application.yml
```

需要修改的配置项：
- `spring.datasource.password` — 数据库密码
- `spring.mail.username` / `spring.mail.password` — 邮箱 SMTP 账号和授权码
- `jwt.member.secret` / `jwt.admin.secret` — JWT 签名密钥

### 3. 启动后端

```bash
cd cs_member
mvn spring-boot:run
```

后端运行在 `http://localhost:8080`，API 统一前缀 `/api/v1`。

### 4. 启动前端

```bash
cd cs_member_frontend
npm install
npm run dev
```

前端运行在 `http://localhost:3000`，通过 Vite 代理将 `/api` 请求转发到后端 `http://localhost:8080`。

## 访问地址

| 入口 | 地址 |
|------|------|
| 会员登录 | http://localhost:3000/member/login |
| 会员注册 | http://localhost:3000/member/register |
| 管理员登录 | http://localhost:3000/admin/login |

## 默认账号

系统预置一个系统管理员账号：

- **手机号**：13800000000
- **密码**：admin123
- **角色**：系统管理员

门店管理员、员工账号由系统管理员在后台「用户管理」中创建；会员账号通过注册页面自行注册。

## 角色权限

系统采用 RBAC 权限模型，定义四种角色，各角色权限如下：

| 功能模块 | 系统管理员 | 门店管理员 | 员工 | 会员 |
|---------|-----------|-----------|------|------|
| 仪表盘 | 全部 | 所属门店 | - | - |
| 会员管理 | 全部 | 所属门店 | 查看 | - |
| 消费记录 | 全部 | 所属门店 | 录入/查看 | 本人 |
| 积分管理 | 全部 | 所属门店 | 查看 | 本人 |
| 优惠券管理 | 全部 | 查看/发放 | - | 领取/使用 |
| 门店管理 | 全部 | - | - | - |
| 员工管理 | 全部 | 所属门店 | - | - |
| 统计分析 | 全部 | 所属门店 | - | - |
| 角色管理 | 全部 | - | - | - |
| 用户管理 | 全部 | - | - | - |
| 系统设置 | 全部 | - | - | - |
| 个人中心 | - | - | - | 查看/编辑 |
| 我的积分 | - | - | - | 查看 |
| 我的优惠券 | - | - | - | 查看 |

## 功能模块

**会员端**：注册/登录、个人信息管理、消费记录查询、积分查询、优惠券领取与使用、账户设置、密码找回（邮箱验证）

**管理端**：
- 仪表盘 — 数据概览（会员总数、今日消费、在线会员等）
- 会员管理 — CRUD、搜索筛选、批量操作、导出、余额/积分调整
- 消费记录 — 录入消费、状态管理（取消/退款）
- 积分管理 — 积分调整、记录查询
- 优惠券管理 — 创建/编辑/删除优惠券、发放、导出
- 门店管理 — 门店 CRUD
- 员工管理 — 员工账号管理
- 统计分析 — 收入趋势、会员增长、积分统计图表
- 角色管理 — 角色权限分配
- 用户管理 — 系统用户 CRUD、状态管理、密码重置
- 系统设置 — 积分规则、系统参数配置

## API 概览

后端接口统一前缀 `/api/v1`，主要模块：

- `POST /api/v1/auth/member/login` — 会员登录
- `POST /api/v1/auth/admin/login` — 管理员登录
- `GET /api/v1/admin/members` — 会员列表
- `GET /api/v1/admin/consumption` — 消费记录
- `GET /api/v1/admin/points` — 积分记录
- `GET /api/v1/admin/coupons` — 优惠券列表
- `GET /api/v1/admin/stores` — 门店列表
- `GET /api/v1/admin/dashboard` — 仪表盘数据
- `GET /api/v1/admin/statistics/*` — 统计数据

接口通过 `@PreAuthorize` 注解实现细粒度权限控制，JWT Token 通过请求头 `Authorization: Bearer <token>` 传递。

## 数据库

共 19 张表：

| 分类 | 表名 |
|------|------|
| 会员 | `member`、`member_card`、`member_grade_config`、`member_coupon` |
| 消费 | `consumption_record`、`points_record` |
| 营销 | `coupon`、`benefit_template`、`notification` |
| 门店 | `store` |
| 权限 | `sys_user`、`sys_role`、`sys_permission`、`sys_user_role`、`sys_role_permission` |
| 系统 | `system_config`、`email_verification`、`login_log`、`operation_log` |

初始化脚本 `cs_member.sql` 包含建表语句、外键约束、索引和默认种子数据（3 个角色、46 条权限、3 个门店、7 个会员等级、38 条系统配置）。
