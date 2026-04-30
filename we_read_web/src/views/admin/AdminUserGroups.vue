<template>
  <div class="admin-page" v-loading="loading">
    <div class="header">
      <h2>用户分组</h2>
      <div class="tools">
        <el-input v-model="keyword" placeholder="搜索用户名/邮箱" clearable @keyup.enter="reload" style="width:240px" />
        <el-button type="primary" @click="reload">搜索</el-button>
      </div>
    </div>

    <el-alert
      v-if="truncated"
      title="为提高加载速度，当前仅加载了部分用户数据。可通过搜索进一步筛选。"
      type="info"
      show-icon
      class="tip"
    />

    <el-tabs v-model="activeTab">
      <el-tab-pane :label="`真实用户（${realUsers.length}）`" name="real">
        <el-table :data="pagedRealUsers" border style="width:100%">
          <el-table-column label="序号" width="80">
            <template #default="scope">{{ (realPage - 1) * pageSize + scope.$index + 1 }}</template>
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
            :current-page="realPage"
            :page-size="pageSize"
            :total="realUsers.length"
            @current-change="(p)=>realPage=p"
          />
        </div>
      </el-tab-pane>

      <el-tab-pane :label="`测试用户（${testUsers.length}）`" name="test">
        <el-table :data="pagedTestUsers" border style="width:100%">
          <el-table-column label="序号" width="80">
            <template #default="scope">{{ (testPage - 1) * pageSize + scope.$index + 1 }}</template>
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
            :current-page="testPage"
            :page-size="pageSize"
            :total="testUsers.length"
            @current-change="(p)=>testPage=p"
          />
        </div>
      </el-tab-pane>
    </el-tabs>

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
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminApi } from '@/api/admin'

const loading = ref(false)
const keyword = ref('')
const users = ref([])
const truncated = ref(false)

const activeTab = ref('real')
const pageSize = ref(10)
const realPage = ref(1)
const testPage = ref(1)

const load = async () => {
  loading.value = true
  try {
    const size = 200
    const res = await adminApi.getUserList({ page: 1, size, keyword: keyword.value || undefined })
    if (res?.meta?.code === 200) {
      const data = res.data || {}
      const list = data.content || []
      users.value = list
      const total = data.totalElements || list.length
      truncated.value = total > list.length
    }
  } finally {
    loading.value = false
    realPage.value = 1
    testPage.value = 1
  }
}

const reload = () => {
  load()
}

const testUsers = computed(() => (users.value || []).filter(u => (u?.username || '').startsWith('user_')))
const realUsers = computed(() => (users.value || []).filter(u => !(u?.username || '').startsWith('user_')))

const paged = (arr, page) => {
  const start = (page - 1) * pageSize.value
  return arr.slice(start, start + pageSize.value)
}

const pagedRealUsers = computed(() => paged(realUsers.value, realPage.value))
const pagedTestUsers = computed(() => paged(testUsers.value, testPage.value))

const resetDialog = ref({ visible: false, userId: null, newPassword: '' })

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

watch(keyword, () => {
  realPage.value = 1
  testPage.value = 1
})

onMounted(load)
</script>

<style scoped>
.admin-page { padding: 16px; }
.header { display:flex; justify-content:space-between; align-items:center; margin-bottom:12px; }
.tools { display:flex; gap:8px; align-items:center; }
.pager { display:flex; justify-content:center; margin-top:12px; }
.tip { margin-bottom: 12px; }
</style>
