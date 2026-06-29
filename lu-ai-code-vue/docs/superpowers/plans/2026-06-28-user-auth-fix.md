# 用户登录注册功能修复 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) for syntax tracking.

**Goal:** 修复前端 userEmail 字段缺失导致的注册失败和用户管理信息不完整问题

**Architecture:** 前端 TypeScript 类型补齐 `userEmail` 字段 → 注册表单增加邮箱输入框（必填+格式校验）→ 用户管理页增加邮箱搜索和展示列。后端无需任何改动。

**Tech Stack:** Vue 3 + TypeScript + Ant Design Vue + Vite

---

### Task 1: TypeScript 类型定义补齐

**Files:**
- Modify: `src/api/typings.d.ts` (4 个类型增加 userEmail 字段)

- [ ] **Step 1: 给 UserRegisterRequest 添加 userEmail**

修改 `UserRegisterRequest`，在第 257 行 `checkPassword` 之前插入 `userEmail`：

```typescript
type UserRegisterRequest = {
    userAccount?: string
    userEmail?: string
    userPassword?: string
    checkPassword?: string
}
```

- [ ] **Step 2: 给 LoginUserVO 添加 userEmail**

修改 `LoginUserVO`，在第 174 行 `userName` 之后插入 `userEmail`：

```typescript
type LoginUserVO = {
    id?: number
    userAccount?: string
    userName?: string
    userEmail?: string
    userAvatar?: string
    userProfile?: string
    userRole?: string
    createTime?: string
    updateTime?: string
}
```

- [ ] **Step 3: 给 UserVO 添加 userEmail**

修改 `UserVO`，在第 273 行 `userName` 之后插入 `userEmail`：

```typescript
type UserVO = {
    id?: number
    userAccount?: string
    userName?: string
    userEmail?: string
    userAvatar?: string
    userProfile?: string
    userRole?: string
    createTime?: string
}
```

- [ ] **Step 4: 给 UserQueryRequest 添加 userEmail**

修改 `UserQueryRequest`，在第 249 行 `userAccount` 之后插入 `userEmail`：

```typescript
type UserQueryRequest = {
    pageNum?: number
    pageSize?: number
    sortField?: string
    sortOrder?: string
    id?: number
    userName?: string
    userAccount?: string
    userEmail?: string
    userProfile?: string
    userRole?: string
}
```

- [ ] **Step 5: Commit**

```bash
git add src/api/typings.d.ts
git commit -m "fix: add userEmail field to frontend type definitions"
```

---

### Task 2: 注册页面增加邮箱字段

**Files:**
- Modify: `src/pages/user/UserRegisterPage.vue`

- [ ] **Step 1: 表单模板增加邮箱输入框**

在现有模板中，账号输入框之后、密码输入框之前，插入邮箱表单项：

```vue
<a-form-item
  name="userEmail"
  :rules="[
    { required: true, message: '请输入邮箱' },
    { type: 'email', message: '请输入正确的邮箱格式' },
  ]"
>
  <a-input v-model:value="formState.userEmail" placeholder="请输入邮箱" />
</a-form-item>
```

插入位置：账号项（第 6-8 行）和密码项（第 9-17 行）之间。

- [ ] **Step 2: 更新 formState 添加 userEmail**

修改 `formState` 初始化，在 `userAccount` 之后添加 `userEmail`：

```typescript
const formState = reactive<API.UserRegisterRequest>({
  userAccount: '',
  userEmail: '',
  userPassword: '',
  checkPassword: '',
})
```

- [ ] **Step 3: 验证构建**

Run: `npx vue-tsc --noEmit`
Expected: 无 TypeScript 类型错误

- [ ] **Step 4: Commit**

```bash
git add src/pages/user/UserRegisterPage.vue
git commit -m "fix: add email field to registration form"
```

---

### Task 3: 用户管理页面增加邮箱搜索和展示列

**Files:**
- Modify: `src/pages/admin/UserManagePage.vue`

- [ ] **Step 1: 搜索表单增加邮箱输入框**

在现有搜索表单中，用户名输入框之后添加邮箱搜索项：

```vue
<a-form-item label="邮箱">
  <a-input v-model:value="searchParams.userEmail" placeholder="输入邮箱" />
</a-form-item>
```

插入位置：用户名字段（第 8-10 行）和搜索按钮（第 11-13 行）之间。

- [ ] **Step 2: 表格列定义增加邮箱列**

在 `columns` 数组中，现有「用户角色」列之前插入邮箱列：

```typescript
{
  title: '邮箱',
  dataIndex: 'userEmail',
},
```

插入位置：`userProfile` 列（第 70 行）和 `userRole` 列（第 73 行）之间。

- [ ] **Step 3: 验证构建**

Run: `npx vue-tsc --noEmit`
Expected: 无 TypeScript 类型错误

- [ ] **Step 4: Commit**

```bash
git add src/pages/admin/UserManagePage.vue
git commit -m "fix: add email search and column to user management page"
```

---

### Task 4: 验证端到端功能

**Files:** 无代码改动

- [ ] **Step 1: 启动后端服务**

Run: 在项目根目录启动 Spring Boot 应用

```bash
cd d:/java/project/lu-ai-code
mvn spring-boot:run
```

- [ ] **Step 2: 启动前端开发服务**

Run: 在 vue 项目目录启动 Vite

```bash
cd d:/java/project/lu-ai-code/lu-ai-code-vue
npm run dev
```

- [ ] **Step 3: 验证注册流程**

1. 打开浏览器访问注册页面 `/user/register`
2. 填写账号、邮箱、密码、确认密码
3. 点击注册，验证成功跳转到登录页
4. 故意输入错误邮箱格式，验证前端校验拦截

- [ ] **Step 4: 验证登录流程**

1. 使用刚注册的账号登录
2. 验证登录成功跳转到首页
3. 验证右上角显示用户名

- [ ] **Step 5: 验证用户管理页邮箱展示**

1. 使用 admin 账号登录
2. 访问 `/admin/userManage`
3. 验证表格显示邮箱列和数据
4. 使用邮箱搜索过滤用户

- [ ] **Step 6: 验证退出登录**

1. 点击右上角退出登录
2. 验证跳转到登录页
3. 验证状态恢复为未登录
