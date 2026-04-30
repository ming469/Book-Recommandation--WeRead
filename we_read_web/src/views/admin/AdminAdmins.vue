<template>
  <div class="admin-admins">
    <div class="header">
      <div class="title">管理员管理</div>
      <div class="tools">
        <el-input v-model="keyword" placeholder="搜索用户名/邮箱/姓名" clearable @keyup.enter="load" style="width:260px" />
        <el-button type="primary" @click="load">搜索</el-button>
        <el-button type="success" @click="openCreate">新增管理员</el-button>
      </div>
    </div>
    <el-table :data="list" border style="width:100%">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="username" label="用户名" />
      <el-table-column prop="realname" label="姓名" />
      <el-table-column prop="email" label="邮箱" />
      <el-table-column prop="statusId" label="状态" width="100">
        <template #default="{row}">
          <el-tag :type="row.statusId===1?'success':'info'">{{ row.statusId===1?'启用':'停用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="260">
        <template #default="{row}">
          <el-button text type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button text type="warning" @click="openReset(row)">重置密码</el-button>
          <el-popconfirm title="确认删除该管理员？" @confirm="del(row)">
            <template #reference>
              <el-button text type="danger">删除</el-button>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>
    <div class="pager">
      <el-pagination
        background
        layout="prev, pager, next, total"
        :page-size="size"
        :total="total"
        :current-page="page"
        @current-change="onPage" />
    </div>

    <el-dialog v-model="edit.visible" :title="edit.mode==='create'?'新增管理员':'编辑管理员'" width="520px" :close-on-click-modal="false">
      <el-form :model="edit.form" label-width="120px">
        <el-form-item label="用户名">
          <el-input v-model="edit.form.username" :disabled="edit.mode==='edit'" />
        </el-form-item>
        <el-form-item label="姓名">
          <el-input v-model="edit.form.realname" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="edit.form.email" />
        </el-form-item>
        <el-form-item v-if="edit.mode==='create'" label="初始密码">
          <el-input v-model="edit.form.password" type="password" show-password />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="edit.form.statusId" style="width:140px">
            <el-option :value="1" label="启用" />
            <el-option :value="0" label="停用" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="edit.visible=false">取消</el-button>
        <el-button type="primary" @click="submitEdit">{{ edit.mode==='create'?'创建':'保存' }}</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="reset.visible" title="重置密码" width="420px" :close-on-click-modal="false">
      <el-form label-width="120px">
        <el-form-item label="新密码">
          <el-input v-model="reset.newPassword" type="password" show-password placeholder="至少6位" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="reset.visible=false">取消</el-button>
        <el-button type="primary" @click="submitReset">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from "vue";
import { ElMessage } from "element-plus";
import { adminApi } from "@/api/admin";

const list = ref([]);
const total = ref(0);
const page = ref(1);
const size = ref(10);
const keyword = ref("");

const edit = reactive({
  visible: false,
  mode: "create",
  form: { id: null, username: "", realname: "", email: "", password: "", statusId: 1 },
});

const reset = reactive({ visible: false, id: null, newPassword: "" });

const load = async () => {
  try {
    const res = await adminApi.getAdminList({ page: page.value, size: size.value, keyword: keyword.value });
    if (res?.meta?.code === 200) {
      list.value = res.data.list || [];
      total.value = res.data.total || 0;
    }
  } catch (e) { /* ignore */ }
};

const onPage = (p) => { page.value = p; load(); };

const openCreate = () => {
  edit.mode = "create";
  edit.form = { id: null, username: "", realname: "", email: "", password: "", statusId: 1 };
  edit.visible = true;
};
const openEdit = (row) => {
  edit.mode = "edit";
  edit.form = { id: row.id, username: row.username, realname: row.realname, email: row.email, statusId: row.statusId };
  edit.visible = true;
};
const submitEdit = async () => {
  try {
    if (edit.mode === "create") {
      if (!edit.form.password || edit.form.password.length < 6) {
        ElMessage.warning("初始密码至少6位");
        return;
      }
      await adminApi.createAdmin(edit.form);
      ElMessage.success("创建成功");
    } else {
      await adminApi.updateAdmin(edit.form.id, { realname: edit.form.realname, email: edit.form.email, statusId: edit.form.statusId });
      ElMessage.success("保存成功");
    }
    edit.visible = false;
    load();
  } catch (e) {
    ElMessage.error(e?.response?.data?.meta?.message || "操作失败");
  }
};

const openReset = (row) => { reset.id = row.id; reset.newPassword = ""; reset.visible = true; };
const submitReset = async () => {
  if (!reset.newPassword || reset.newPassword.length < 6) {
    ElMessage.warning("新密码至少6位"); return;
  }
  try {
    await adminApi.resetAdminPassword(reset.id, reset.newPassword);
    ElMessage.success("密码已重置");
    reset.visible = false;
  } catch (e) {
    ElMessage.error("操作失败");
  }
};

const del = async (row) => {
  try {
    await adminApi.deleteAdmin(row.id);
    ElMessage.success("已删除");
    load();
  } catch (e) {
    ElMessage.error("删除失败");
  }
};

onMounted(load);
</script>

<style scoped>
.admin-admins { padding: 12px; }
.header { display:flex; justify-content:space-between; align-items:center; margin-bottom:12px; }
.tools > * { margin-left: 8px; }
.pager { margin-top: 10px; text-align: right; }
</style>
