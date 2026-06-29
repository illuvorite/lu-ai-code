# App Module Design

## Overview
Complete App CRUD module following the existing User module pattern. Supports user-owned app management and admin oversight with role-based access control.

## Architecture
- **Controller layer**: `AppController` — REST endpoints
- **Service layer**: `AppService` / `AppServiceImpl` — business logic
- **Mapper layer**: `AppMapper` (MyBatis-Flex `BaseMapper<App>`) — already exists
- **Entity**: `App` — already exists with all fields

## Data Model (App Entity, existing)
| Field | Type | Notes |
|-------|------|-------|
| id | Long | Snowflake ID |
| appName | String | 应用名称 |
| cover | String | 应用封面 |
| initPrompt | String | 初始化 prompt（创建时必须填写） |
| codeGenType | String | 代码生成类型枚举 |
| deployKey | String | 部署标识（内部） |
| deployedTime | LocalDateTime | 部署时间 |
| priority | Integer | 优先级，精选排序依据 |
| userId | Long | 创建用户 id |
| editTime/updateTime/createTime | LocalDateTime | 审计字段 |
| isDelete | Integer | 逻辑删除 |

## DTOs

### AppAddRequest
- `initPrompt` (String, required) — 创建应用时必须填写

### AppUpdateRequest
- `id` (Long, required)
- `appName` (String) — 用户只能修改应用名称

### AppAdminUpdateRequest
- `id` (Long, required)
- `appName` (String)
- `cover` (String)
- `priority` (Integer)

### AppQueryRequest (extends PageRequest)
- `id` (Long)
- `appName` (String)
- `userId` (Long)
- `codeGenType` (String)

## VO

### AppVO
Safe view of App — excludes: `deployKey`, `deployedTime`, `editTime`, `updateTime`, `isDelete`.

## API Endpoints

### User Endpoints (require login via session)
| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | /app/add | login | Create app, initPrompt required |
| POST | /app/update | login | Update own app (appName only) |
| POST | /app/delete | login | Delete own app |
| GET | /app/get | login | View own app detail (returns App) |
| POST | /app/list/my | login | Paginate own apps (max 20) |
| POST | /app/list/featured | none/public | Paginate featured apps (priority desc, max 20) |

### Admin Endpoints (require admin/superadmin role)
| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | /app/delete/admin | @AuthCheck(admin) | Delete any app |
| POST | /app/update/admin | @AuthCheck(admin) | Update any app (name/cover/priority) |
| POST | /app/list/admin | @AuthCheck(admin) | Paginate all apps (no pageSize limit) |
| GET | /app/get/admin | @AuthCheck(admin) | View any app detail |

## Key Logic
1. **Ownership check**: User endpoints verify `app.userId == loginUser.id` before modify/delete
2. **Featured ordering**: `listFeaturedApps` sorts by `priority DESC, createTime DESC`
3. **Admin query**: `AppQueryRequest` fields conditionally applied to QueryWrapper; excludes time fields from query criteria
4. **Page size enforcement**: User endpoints cap at 20; admin endpoint does not cap
