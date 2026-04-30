<template>
  <div class="admin-profile">
    <el-card shadow="never" class="card">
      <template #header>基本信息</template>
      <el-form :model="profile" label-width="120px" class="form">
        <el-form-item label="用户名">
          <el-input v-model="profile.username" disabled />
        </el-form-item>
        <el-form-item label="姓名">
          <el-input v-model="profile.realname" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="profile.email" />
        </el-form-item>
        <div class="actions">
          <el-button type="primary" @click="saveProfile" :loading="loading.saveInfo">保存</el-button>
          <el-button @click="load">恢复</el-button>
        </div>
      </el-form>
    </el-card>

    <el-card shadow="never" class="card">
      <template #header>修改密码</template>
      <el-form :model="pwd" label-width="120px" class="form" @submit.prevent>
        <el-form-item label="旧密码">
          <el-input type="password" v-model="pwd.oldPassword" show-password />
        </el-form-item>
        <el-form-item label="新密码">
          <el-input type="password" v-model="pwd.newPassword" show-password />
        </el-form-item>
        <div class="actions">
          <el-button type="primary" @click="changePassword" :loading="loading.changePwd">修改密码</el-button>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, onMounted } from "vue";
import { ElMessage } from "element-plus";
import { adminApi } from "@/api/admin";

const profile = reactive({ id: null, username: "", realname: "", email: "" });
const pwd = reactive({ oldPassword: "", newPassword: "" });
const loading = reactive({ saveInfo: false, changePwd: false });

const load = async () => {
  try {
    const res = await adminApi.getInfo();
    if (res?.meta?.code === 200 && res.data) {
      profile.id = res.data.id;
      profile.username = res.data.username || "";
      profile.realname = res.data.realname || "";
      profile.email = res.data.email || "";
    }
  } catch (e) {}
};

const saveProfile = async () => {
  loading.saveInfo = true;
  try {
    await adminApi.updateInfo({ realname: profile.realname, email: profile.email });
    ElMessage.success("资料已更新");
  } catch (e) {
    ElMessage.error(e?.response?.data?.meta?.message || "保存失败");
  } finally {
    loading.saveInfo = false;
  }
};

const changePassword = async () => {
  if (!pwd.newPassword || pwd.newPassword.length < 6) {
    ElMessage.warning("新密码至少6位");
    return;
  }
  loading.changePwd = true;
  try {
    await adminApi.changePassword({ oldPassword: pwd.oldPassword, newPassword: pwd.newPassword });
    ElMessage.success("密码已更新");
    pwd.oldPassword = "";
    pwd.newPassword = "";
  } catch (e) {
    ElMessage.error(e?.response?.data?.meta?.message || "修改失败");
  } finally {
    loading.changePwd = false;
  }
};

onMounted(load);
</script>

<style scoped>
.admin-profile { padding: 12px; }
.card { margin-bottom: 12px; }
.actions { margin-top: 6px; }
</style>
