<template>
  <div class="admin-page" v-loading="loading">
    <div class="header">
      <h2>用户列表</h2>
      <div class="tools">
        <el-input v-model="keyword" placeholder="搜索用户名/邮箱" clearable @keyup.enter="reload" style="width:240px" />
        <el-button type="primary" @click="reload">搜索</el-button>
        <el-button type="warning" @click="openBatchTestReset">测试用户批量修改密码</el-button>
      </div>
    </div>

    <el-table :data="users" border style="width:100%">
      <el-table-column label="序号" width="80">
        <template #default="scope">{{ (page - 1) * size + scope.$index + 1 }}</template>
      </el-table-column>
      <el-table-column prop="username" label="用户名" min-width="160" />
      <el-table-column prop="email" label="邮箱" min-width="200" />
      <el-table-column prop="createdAt" label="注册时间" min-width="180">
        <template #default="{ row }">{{ formatDate(row.createdAt) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="260">
        <template #default="{ row }">
          <el-button size="small" @click="openReset(row)">修改密码</el-button>
          <el-button size="small" type="danger" plain @click="confirmDelete(row)">删除用户</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pager">
      <el-pagination
        background
        layout="prev, pager, next"
        :current-page="page"
        :page-size="size"
        :total="total"
        @current-change="changePage"
      />
    </div>

    <el-dialog v-model="resetDialog.visible" title="修改密码" width="420px" :close-on-click-modal="false">
      <el-form @submit.prevent>
        <el-form-item label="新密码">
          <el-input v-model="resetDialog.newPassword" type="password" placeholder="至少6位" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="resetDialog.visible=false">取消</el-button>
        <el-button type="primary" @click="doReset">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="batchTestDialog.visible" title="测试用户批量修改密码" width="460px" :close-on-click-modal="false">
      <el-form @submit.prevent>
        <el-form-item label="新密码">
          <el-input v-model="batchTestDialog.newPassword" type="password" placeholder="至少6位" show-password />
        </el-form-item>
        <div style="color:#888;line-height:22px;">
          将对所有用户名以 <b>user_</b> 开头的测试用户统一重置为该密码。
        </div>
      </el-form>
      <template #footer>
        <el-button @click="batchTestDialog.visible=false">取消</el-button>
        <el-button type="primary" @click="doBatchTestReset">确定</el-button>
      </template>
    </el-dialog>
  </div>
  </template>

  <script setup>
  import { ref, onMounted } from 'vue'
  import { ElMessage, ElMessageBox } from 'element-plus'
  import { adminApi } from '@/api/admin'

  const loading = ref(false)
  const users = ref([])
  const page = ref(1)
  const size = ref(10)
  const total = ref(0)
  const keyword = ref('')

  const resetDialog = ref({ visible: false, userId: null, newPassword: '' })
  const batchTestDialog = ref({ visible: false, newPassword: '' })

  const load = async () => {
    loading.value = true
    try {
      const res = await adminApi.getUserList({ page: page.value, size: size.value, keyword: keyword.value || undefined })
      if (res?.meta?.code === 200) {
        const data = res.data || {}
        users.value = data.content || []
        total.value = data.totalElements || 0
        size.value = data.size || size.value
        page.value = data.currentPage || page.value
      }
    } finally {
      loading.value = false
    }
  }

  const reload = () => {
    page.value = 1
    load()
  }

  const changePage = (p) => {
    page.value = p
    load()
  }

  const openReset = (row) => {
    resetDialog.value = { visible: true, userId: row.id, newPassword: '' }
  }

  const doReset = async () => {
    const np = resetDialog.value.newPassword?.trim()
    if (!np || np.length < 6) {
      ElMessage.warning('新密码至少6位')
      return
    }
    try {
      await adminApi.resetUserPassword(resetDialog.value.userId, np)
      ElMessage.success('密码已重置')
      resetDialog.value.visible = false
    } catch (e) {
      ElMessage.error('操作失败')
    }
  }

  const openBatchTestReset = () => {
    batchTestDialog.value = { visible: true, newPassword: '' }
  }

  const doBatchTestReset = async () => {
    const np = batchTestDialog.value.newPassword?.trim()
    if (!np || np.length < 6) {
      ElMessage.warning('新密码至少6位')
      return
    }
    try {
      await adminApi.batchResetTestUserPasswords(np)
      ElMessage.success('已对测试用户批量重置密码')
      batchTestDialog.value.visible = false
    } catch (e) {
      ElMessage.error('批量操作失败')
    }
  }

  const confirmDelete = (row) => {
    ElMessageBox.confirm(`确定删除用户「${row.username}」吗？`, '删除用户', { type: 'warning' })
      .then(async () => {
        try {
          await adminApi.deleteUser(row.id)
          ElMessage.success('已删除')
          load()
        } catch (e) {
          ElMessage.error('删除失败，可能存在关联数据')
        }
      })
      .catch(() => {})
  }

  const formatDate = (iso) => (iso ? new Date(iso).toLocaleString() : '')

  onMounted(load)
  </script>

  <style scoped>
  .admin-page { padding: 16px; }
  .header { display:flex; justify-content:space-between; align-items:center; margin-bottom:12px; }
  .tools { display:flex; gap:8px; align-items:center; }
  .pager { display:flex; justify-content:center; margin-top:12px; }
  </style>
