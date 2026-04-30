<template>
  <div class="system-config">
    <div class="header">
      <h2>系统配置</h2>
      <div class="tools">
        <el-button type="warning" @click="evict('home:*')" :loading="loading.evict">清空首页缓存</el-button>
        <el-button type="warning" @click="evict('item_sim:*')" :loading="loading.evict">清空相似度缓存</el-button>
        <el-button type="primary" @click="recompute" :loading="loading.recompute">触发推荐相似度重算</el-button>
      </div>
    </div>

    <el-form :model="form" label-width="180px" class="form">
      <el-card shadow="never" class="card">
        <template #header>基础</template>
        <el-form-item label="开放注册">
          <el-switch v-model="form.allowRegister" />
        </el-form-item>
        <el-form-item label="默认分页大小">
          <el-input-number v-model="form.pageSizeDefault" :min="5" :max="100" />
        </el-form-item>
      </el-card>

      <el-card shadow="never" class="card">
        <template #header>推荐算法</template>
        <el-form-item label="种子数上限 seedLimit">
          <el-input-number v-model="form['recommend.seedLimit']" :min="5" :max="100" />
        </el-form-item>
        <el-form-item label="邻域大小 topK">
          <el-input-number v-model="form['recommend.topK']" :min="10" :max="200" />
        </el-form-item>
        <el-form-item label="偏好融合 alpha">
          <el-input-number v-model="form['recommend.alpha']" :step="0.05" :min="0" :max="1" />
        </el-form-item>
        <el-form-item label="分类多样化上限">
          <el-input-number v-model="form['recommend.categoryCapRatio']" :step="0.05" :min="0.1" :max="1" />
        </el-form-item>
      </el-card>

      <div class="actions">
        <el-button type="primary" @click="save" :loading="loading.save">保存配置</el-button>
        <el-button @click="load">恢复</el-button>
      </div>
    </el-form>
  </div>
</template>

<script setup>
import { onMounted, reactive } from "vue";
import { ElMessage } from "element-plus";
import { adminApi } from "@/api/admin";

const form = reactive({
  allowRegister: true,
  pageSizeDefault: 10,
  "recommend.seedLimit": 20,
  "recommend.topK": 80,
  "recommend.alpha": 0.2,
  "recommend.categoryCapRatio": 0.4,
});

const loading = reactive({ save: false, evict: false, recompute: false });

const load = async () => {
  try {
    const res = await adminApi.getSystemConfig();
    if (res?.meta?.code === 200 && res.data) {
      Object.assign(form, res.data);
    }
  } catch (e) {}
};

const save = async () => {
  loading.save = true;
  try {
    await adminApi.updateSystemConfig({ ...form });
    ElMessage.success("配置已保存");
  } catch (e) {
    ElMessage.error("保存失败");
  } finally {
    loading.save = false;
  }
};

const evict = async (pattern) => {
  loading.evict = true;
  try {
    const res = await adminApi.evictCache(pattern);
    const removed = res?.data?.removed ?? 0;
    const redis = !!res?.data?.redis;
    if (!redis) {
      ElMessage.warning(`未检测到 Redis，已跳过后端清理（pattern: ${pattern}）`);
    } else {
      ElMessage.success(`清理完成（pattern: ${pattern}，删除 ${removed} 个键）`);
    }
  } catch (e) {
    ElMessage.error("清理失败");
  } finally {
    loading.evict = false;
  }
};

const recompute = async () => {
  loading.recompute = true;
  try {
    await adminApi.triggerRecommendRecompute();
    ElMessage.success("已触发相似度重算（后台异步执行）");
  } catch (e) {
    const msg = e?.response?.data?.meta?.message || "触发失败";
    ElMessage.error(msg);
  } finally {
    loading.recompute = false;
  }
};

onMounted(load);
</script>

<style scoped>
.system-config { padding: 12px; }
.header { display:flex; justify-content:space-between; align-items:center; margin-bottom:12px; }
.tools > * { margin-left: 8px; }
.card { margin-bottom: 12px; }
.actions { margin-top: 8px; }
</style>
