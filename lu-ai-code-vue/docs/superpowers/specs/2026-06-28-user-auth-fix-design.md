# 用户登录注册功能修复 — 设计文档

## 概述

修复现有代码中前后端字段不一致问题，使登录、注册、退出登录、用户管理页面功能正常运行。核心问题是后端 `UserRegisterRequest` 要求必填 `userEmail`，但前端类型定义和表单均缺少该字段。

## 修复范围

最小改动原则，只修复前后端不匹配导致功能不可用的问题。

## 改动清单

### 1. TypeScript 类型定义补齐

**文件:** `src/api/typings.d.ts`

| 类型 | 新增字段 | 说明 |
|------|---------|------|
| `UserRegisterRequest` | `userEmail?: string` | 注册请求参数 |
| `LoginUserVO` | `userEmail?: string` | 登录用户视图对象 |
| `UserVO` | `userEmail?: string` | 用户视图对象 |
| `UserQueryRequest` | `userEmail?: string` | 用户查询请求参数 |

### 2. 注册页面增加邮箱字段

**文件:** `src/pages/user/UserRegisterPage.vue`

- 在「账号」和「密码」之间插入邮箱输入框
- 校验规则: 必填 + 邮箱格式校验 (`type: 'email'`)
- 表单布局与现有风格一致（ant-design-vue 表单）

### 3. 用户管理页面增加邮箱支持

**文件:** `src/pages/admin/UserManagePage.vue`

- 搜索表单增加「邮箱」输入框，位于「用户名」之后
- 表格在「用户角色」之前增加「邮箱」列，显示 userEmail

### 4. 后端无改动

后端以下功能已原生支持 `userEmail`:

| 接口 | 状态 |
|------|------|
| `POST /user/register` — 已要求 userEmail 必填 | ✅ 无需改动 |
| `POST /user/list/page/vo` — 查询包装器已支持 userEmail like 查询 | ✅ 无需改动 |
| `UserVO` / `LoginUserVO` — 已包含 userEmail 字段 | ✅ 无需改动 |

## 数据流

```
注册: 前端表单(含email) → UserRegisterRequest(含email) → POST /register
管理: 前端搜索(含email) → UserQueryRequest(含email) → POST /list/page/vo
                                        → 后端QueryWrapper .like("userEmail", ...)
                                        → 返回UserVO(含email) → 表格展示
```

## 不做的事情

- 不修改登录页（后端 `userLogin` 不使用 `userEmail`）
- 不修改退出登录（功能已完整）
- 不添加邮箱找回密码等新功能
- 不做全局 DTO 全量审计（`vipCode`、`shareCode` 等与前端无关）
