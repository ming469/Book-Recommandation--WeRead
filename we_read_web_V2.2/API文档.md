# 用户行为与图书推荐系统API文档

## 通用说明

- 基础URL: `/api`
- 认证方式: Bearer Token认证
- 响应格式: JSON
- 请求头要求:
  ```
  Content-Type: application/json
  Authorization: Bearer <your_token>
  ```

## 用户认证与个人资料

### 1. 用户登录

#### 接口信息

- 请求路径：`/api/user/auth/login`
- 请求方法：POST
- 功能说明：用户登录认证，获取访问令牌

#### 请求参数

```json
{
    "username": "用户名或邮箱（必填，字符串）",
    "password": "密码（必填，字符串）",
    "rememberMe": "是否记住登录状态（可选，布尔值）"
}
```

#### 示例请求

```json
{
    "username": "example@email.com",
    "password": "your_password",
    "rememberMe": true
}
```

#### 响应格式

- 成功响应：HTTP 200
```json
{
    "meta": {
        "code": 200,
        "message": "登录成功"
    },
    "data": {
        "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
        "user": {
            "id": 1001,
            "username": "example"
        }
    }
}
```

- 错误响应：HTTP 401
```json
{
    "meta": {
        "code": 401,
        "message": "用户名/邮箱或密码不正确"
    },
    "data": null
}
```

### 2. 修改密码

#### 接口信息

- 请求路径：`/api/user/change-password`
- 请求方法：POST
- 功能说明：修改用户密码
- 认证要求：需要用户登录

#### 请求参数

```json
{
    "oldPassword": "原密码（必填，字符串）",
    "newPassword": "新密码（必填，字符串）"
}
```

#### 示例请求

```json
{
    "oldPassword": "old_password",
    "newPassword": "new_password"
}
```

#### 响应格式

- 成功响应：HTTP 200
```json
{
    "meta": {
        "code": 200,
        "message": "密码修改成功"
    },
    "data": {
        "user": {
            "id": 1001,
            "username": "example"
        }
    }
}
```

- 错误响应：HTTP 401
```json
{
    "meta": {
        "code": 401,
        "message": "原密码不正确"
    },
    "data": null
}
```

### 3. 获取个人资料

#### 接口信息

- 请求路径：`/api/user/profile`
- 请求方法：GET
- 功能说明：获取当前登录用户的个人资料信息
- 认证要求：需要用户登录

#### 响应格式

- 成功响应：HTTP 200
```json
{
    "code": 200,
    "message": "获取用户信息成功",
    "data": {
        "id": 1001,
        "username": "example",
        "nickname": "示例用户",
        "email": "example@email.com",
        "phone": "13800138000",
        "avatar": "avatar_url",
        "gender": "male",
        "createTime": "2023-01-01T00:00:00",
        "lastLoginTime": "2023-12-31T12:00:00"
    }
}
```

- 错误响应：HTTP 401
```json
{
    "code": 401,
    "message": "用户未登录",
    "data": null
}
```

## 用户行为记录

### 1. 记录用户行为

#### 接口信息

- 请求路径：`/api/user/behavior`
- 请求方法：POST
- 功能说明：记录用户的图书相关行为，包括点击、收藏、评分和评论等

#### 请求参数

```json
{
    "userId": "用户ID（必填，整数）",
    "bookId": "图书ID（必填，整数）",
    "categoryId": "分类ID（必填，整数）",
    "behaviorType": "行为类型（必填，整数）：1-点击，2-收藏，3-评分，4-评论"
}
```

#### 示例请求

```json
{
    "userId": 1001,
    "bookId": 2001,
    "categoryId": 3,
    "behaviorType": 2
}
```

#### 响应格式

- 成功响应：HTTP 200
```json
{}
```

- 错误响应：HTTP 4xx/5xx
```json
{
    "error": "错误信息描述"
}
```

### 2. 获取用户分类偏好

#### 接口信息

- 请求路径：`/api/user/behavior/preferences/{userId}`
- 请求方法：GET
- 功能说明：获取指定用户对不同图书分类的偏好程度

#### 路径参数

- userId：用户ID（必填，整数）

#### 示例请求

```
GET /api/user/behavior/preferences/1001
```

#### 响应格式

- 成功响应：HTTP 200
```json
{
    "1": 0.3,    // 分类ID: 偏好权重
    "2": 0.5,
    "3": 0.2
}
```

- 错误响应：HTTP 4xx/5xx
```json
{
    "error": "错误信息描述"
}
```

### 3. 获取推荐图书

#### 接口信息

- 请求路径：`/api/user/behavior/recommendations/{userId}`
- 请求方法：GET
- 功能说明：基于用户行为和偏好，获取个性化的图书推荐列表

#### 路径参数

- userId：用户ID（必填，整数）

#### 查询参数

- limit：返回推荐图书的数量限制（可选，整数，默认值：10）

#### 示例请求

```
GET /api/user/behavior/recommendations/1001?limit=5
```

#### 响应格式

- 成功响应：HTTP 200
```json
[
    2001,   // 推荐图书ID列表
    2002,
    2003,
    2004,
    2005
]
```

- 错误响应：HTTP 4xx/5xx
```json
{
    "error": "错误信息描述"
}
```

## 错误码说明

| 状态码 | 说明 |
|--------|------|
| 200 | 请求成功 |
| 400 | 请求参数错误 |
| 401 | 未授权访问 |
| 403 | 访问被禁止 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |