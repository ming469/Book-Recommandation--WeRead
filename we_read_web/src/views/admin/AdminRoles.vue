<template>
  <div class="admin-roles">
    <div class="header">
      <h2>角色权限</h2>
    </div>
    <el-alert
      title="当前系统仅启用了单一管理员角色：ROLE_ADMIN"
      type="info"
      description="鉴于后端安全策略基于 ROLE_ADMIN 统一鉴权，角色细粒度授权将在后续版本引入（如：超级管理员/运营管理员/只读运维）。当前页面提供结构占位，以便未来扩展。"
      show-icon
      :closable="false"
    />
    <el-card shadow="never" class="card">
      <template #header>内置角色</template>
      <el-descriptions :column="1" border>
        <el-descriptions-item label="ROLE_ADMIN">
          拥有访问 /api/admin/** 的全部权限（受业务接口校验与操作日志审计）
        </el-descriptions-item>
      </el-descriptions>
    </el-card>
    <el-card shadow="never" class="card">
      <template #header>权限点（规划中）</template>
      <ul class="list">
        <li>系统配置读/写、缓存清理、推荐重算</li>
        <li>管理员 CRUD、重置密码、禁用/启用</li>
        <li>数据导入导出与敏感操作需要二次确认</li>
      </ul>
    </el-card>
  </div>
</template>

<script setup>
</script>

<style scoped>
.admin-roles { padding: 12px; }
.card { margin-top: 12px; }
.list { margin: 0 0 0 18px; line-height: 1.9; color: #606266; }
</style>
