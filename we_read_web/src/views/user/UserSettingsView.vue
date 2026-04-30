<template>
  <div class="user-settings">
    <el-tabs v-model="activeTab" class="settings-tabs">
      <!-- 安全设置 -->
      <el-tab-pane label="安全设置" name="security">
        <div class="tab-content">
          <h2 class="section-title">账号安全</h2>

          <!-- 修改密码 -->
          <el-card class="settings-card">
            <template #header>
              <div class="card-header">
                <span>修改密码</span>
                <el-button
                  type="primary"
                  @click="showPasswordForm = !showPasswordForm"
                  text
                >
                  {{ showPasswordForm ? "取消" : "修改" }}
                </el-button>
              </div>
            </template>

            <div v-if="!showPasswordForm" class="card-placeholder">
              <el-icon class="placeholder-icon"><Lock /></el-icon>
              <span>定期修改密码可以提高账号安全性</span>
            </div>

            <el-form
              v-else
              ref="passwordFormRef"
              :model="passwordForm"
              :rules="passwordRules"
              label-width="120px"
              status-icon
            >
              <el-form-item label="当前密码" prop="currentPassword">
                <el-input
                  v-model="passwordForm.currentPassword"
                  type="password"
                  placeholder="请输入当前密码"
                  show-password
                />
              </el-form-item>

              <el-form-item label="新密码" prop="newPassword">
                <el-input
                  v-model="passwordForm.newPassword"
                  type="password"
                  placeholder="请输入新密码"
                  show-password
                />
              </el-form-item>

              <el-form-item label="确认新密码" prop="confirmPassword">
                <el-input
                  v-model="passwordForm.confirmPassword"
                  type="password"
                  placeholder="请再次输入新密码"
                  show-password
                />
              </el-form-item>

              <el-form-item>
                <el-button
                  type="primary"
                  @click="submitPasswordForm"
                  :loading="loading.password"
                >
                  确认修改
                </el-button>
                <el-button @click="showPasswordForm = false">取消</el-button>
              </el-form-item>
            </el-form>
          </el-card>

          <!-- 绑定手机 -->
          <el-card class="settings-card">
            <template #header>
              <div class="card-header">
                <span>绑定手机</span>
                <el-button
                  type="primary"
                  @click="showPhoneForm = !showPhoneForm"
                  text
                  :disabled="!!userInfo.phone && !showPhoneForm"
                >
                  {{
                    userInfo.phone ? (showPhoneForm ? "取消" : "修改") : "绑定"
                  }}
                </el-button>
              </div>
            </template>

            <div v-if="!showPhoneForm" class="card-info">
              <el-icon class="info-icon"><Cellphone /></el-icon>
              <span>
                {{
                  userInfo.phone
                    ? `已绑定手机：${maskPhone(userInfo.phone)}`
                    : "未绑定手机号码"
                }}
              </span>
            </div>

            <el-form
              v-else
              ref="phoneFormRef"
              :model="phoneForm"
              :rules="phoneRules"
              label-width="120px"
              status-icon
            >
              <el-form-item label="手机号码" prop="phone">
                <el-input
                  v-model="phoneForm.phone"
                  placeholder="请输入手机号码"
                />
              </el-form-item>

              <el-form-item label="验证码" prop="code">
                <div class="verification-code">
                  <el-input
                    v-model="phoneForm.code"
                    placeholder="请输入验证码"
                  />
                  <el-button
                    type="primary"
                    :disabled="countdown > 0"
                    @click="sendPhoneCode"
                  >
                    {{ countdown > 0 ? `${countdown}秒后重试` : "获取验证码" }}
                  </el-button>
                </div>
              </el-form-item>

              <el-form-item>
                <el-button
                  type="primary"
                  @click="submitPhoneForm"
                  :loading="loading.phone"
                >
                  确认绑定
                </el-button>
                <el-button @click="showPhoneForm = false">取消</el-button>
              </el-form-item>
            </el-form>
          </el-card>

          <!-- 绑定邮箱 -->
          <el-card class="settings-card">
            <template #header>
              <div class="card-header">
                <span>绑定邮箱</span>
                <el-button
                  type="primary"
                  @click="showEmailForm = !showEmailForm"
                  text
                  :disabled="!!userInfo.email && !showEmailForm"
                >
                  {{
                    userInfo.email ? (showEmailForm ? "取消" : "修改") : "绑定"
                  }}
                </el-button>
              </div>
            </template>

            <div v-if="!showEmailForm" class="card-info">
              <el-icon class="info-icon"><Message /></el-icon>
              <span>
                {{
                  userInfo.email
                    ? `已绑定邮箱：${maskEmail(userInfo.email)}`
                    : "未绑定邮箱"
                }}
                <el-tag
                  v-if="userInfo.email && userInfo.emailVerified"
                  size="small"
                  type="success"
                  class="verify-tag"
                >
                  已验证
                </el-tag>
                <el-button
                  v-else-if="userInfo.email && !userInfo.emailVerified"
                  type="primary"
                  size="small"
                  @click="sendVerifyEmail"
                  :loading="loading.emailVerify"
                >
                  验证邮箱
                </el-button>
              </span>
            </div>

            <el-form
              v-else
              ref="emailFormRef"
              :model="emailForm"
              :rules="emailRules"
              label-width="120px"
              status-icon
            >
              <el-form-item label="邮箱地址" prop="email">
                <el-input
                  v-model="emailForm.email"
                  placeholder="请输入邮箱地址"
                />
              </el-form-item>

              <el-form-item label="验证码" prop="code">
                <div class="verification-code">
                  <el-input
                    v-model="emailForm.code"
                    placeholder="请输入验证码"
                  />
                  <el-button
                    type="primary"
                    :disabled="countdown > 0"
                    @click="sendEmailCode"
                  >
                    {{ countdown > 0 ? `${countdown}秒后重试` : "获取验证码" }}
                  </el-button>
                </div>
              </el-form-item>

              <el-form-item>
                <el-button
                  type="primary"
                  @click="submitEmailForm"
                  :loading="loading.email"
                >
                  确认绑定
                </el-button>
                <el-button @click="showEmailForm = false">取消</el-button>
              </el-form-item>
            </el-form>
          </el-card>
        </div>
      </el-tab-pane>

      <!-- 隐私设置 -->
      <el-tab-pane label="隐私设置" name="privacy">
        <div class="tab-content">
          <h2 class="section-title">隐私与数据</h2>

          <el-card class="settings-card">
            <template #header>
              <div class="card-header">
                <span>隐私设置</span>
                <el-button
                  type="primary"
                  @click="savePrivacySettings"
                  :loading="loading.privacy"
                  text
                >
                  保存设置
                </el-button>
              </div>
            </template>

            <div class="privacy-settings">
              <el-form label-position="left" label-width="200px">
                <el-form-item label="个人资料可见性">
                  <el-radio-group v-model="privacySettings.profileVisibility">
                    <el-radio label="public">公开</el-radio>
                    <el-radio label="friends">仅好友可见</el-radio>
                    <el-radio label="private">私密</el-radio>
                  </el-radio-group>
                </el-form-item>

                <el-form-item label="阅读历史可见性">
                  <el-radio-group
                    v-model="privacySettings.readingHistoryVisibility"
                  >
                    <el-radio label="public">公开</el-radio>
                    <el-radio label="friends">仅好友可见</el-radio>
                    <el-radio label="private">私密</el-radio>
                  </el-radio-group>
                </el-form-item>

                <el-form-item label="收藏书籍可见性">
                  <el-radio-group v-model="privacySettings.favoritesVisibility">
                    <el-radio label="public">公开</el-radio>
                    <el-radio label="friends">仅好友可见</el-radio>
                    <el-radio label="private">私密</el-radio>
                  </el-radio-group>
                </el-form-item>

                <el-form-item label="允许数据分析">
                  <el-switch v-model="privacySettings.allowDataAnalysis" />
                  <span class="setting-desc"
                    >允许分析阅读习惯以提供个性化推荐</span
                  >
                </el-form-item>
              </el-form>
            </div>
          </el-card>

          <!-- 数据导出与删除 -->
          <el-card class="settings-card">
            <template #header>
              <div class="card-header">
                <span>数据管理</span>
              </div>
            </template>

            <div class="data-management">
              <div class="data-action">
                <div class="action-info">
                  <h3>导出我的数据</h3>
                  <p>下载包含您的个人信息、阅读历史和收藏书籍的数据文件</p>
                </div>
                <el-button
                  type="primary"
                  @click="exportUserData"
                  :loading="loading.export"
                >
                  导出数据
                </el-button>
              </div>

              <el-divider />

              <div class="data-action danger-zone">
                <div class="action-info">
                  <h3>删除账号</h3>
                  <p>永久删除您的账号和所有相关数据，此操作不可撤销</p>
                </div>
                <el-button type="danger" @click="showDeleteAccountDialog">
                  删除账号
                </el-button>
              </div>
            </div>
          </el-card>
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- 删除账号确认对话框 -->
    <el-dialog v-model="deleteAccountDialog" title="删除账号确认" width="500px">
      <div class="delete-account-dialog">
        <el-alert
          title="此操作将永久删除您的账号和所有数据，无法恢复！"
          type="error"
          :closable="false"
          show-icon
        />

        <p class="dialog-text">请输入您的密码以确认删除账号：</p>

        <el-form
          ref="deleteAccountFormRef"
          :model="deleteAccountForm"
          :rules="deleteAccountRules"
          label-width="0"
        >
          <el-form-item prop="password">
            <el-input
              v-model="deleteAccountForm.password"
              type="password"
              placeholder="请输入密码"
              show-password
            />
          </el-form-item>

          <el-form-item prop="confirm">
            <el-checkbox v-model="deleteAccountForm.confirm">
              我理解此操作不可撤销，确认删除我的账号
            </el-checkbox>
          </el-form-item>
        </el-form>
      </div>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="deleteAccountDialog = false">取消</el-button>
          <el-button
            type="danger"
            @click="confirmDeleteAccount"
            :loading="loading.deleteAccount"
          >
            确认删除
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from "vue";
import { useRouter } from "vue-router";
import { ElMessage, ElMessageBox } from "element-plus";
import { Lock, Cellphone, Message } from "@element-plus/icons-vue";
import { useUserStore } from "@/stores/modules/user";

const router = useRouter();
const userStore = useUserStore();

// 用户信息
const userInfo = computed(() => userStore.userInfo || {});

// 当前激活的标签页
const activeTab = ref("security");

// 表单显示状态
const showPasswordForm = ref(false);
const showPhoneForm = ref(false);
const showEmailForm = ref(false);

// 表单引用
const passwordFormRef = ref(null);
const phoneFormRef = ref(null);
const emailFormRef = ref(null);
const deleteAccountFormRef = ref(null);

// 加载状态
const loading = reactive({
  password: false,
  phone: false,
  email: false,
  emailVerify: false,
  privacy: false,
  export: false,
  deleteAccount: false,
});

// 验证码倒计时
const countdown = ref(0);
let countdownTimer = null;

// 删除账号对话框
const deleteAccountDialog = ref(false);

// 密码修改表单
const passwordForm = reactive({
  currentPassword: "",
  newPassword: "",
  confirmPassword: "",
});

// 手机绑定表单
const phoneForm = reactive({
  phone: "",
  code: "",
});

// 邮箱绑定表单
const emailForm = reactive({
  email: "",
  code: "",
});

// 隐私设置
const privacySettings = reactive({
  profileVisibility: "public",
  readingHistoryVisibility: "friends",
  favoritesVisibility: "public",
  allowDataAnalysis: true,
});

// 删除账号表单
const deleteAccountForm = reactive({
  password: "",
  confirm: false,
});

// 表单验证规则
const passwordRules = {
  currentPassword: [
    { required: true, message: "请输入当前密码", trigger: "blur" },
  ],
  newPassword: [
    { required: true, message: "请输入新密码", trigger: "blur" },
    { min: 8, message: "密码长度不能小于8个字符", trigger: "blur" },
    {
      pattern: /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d).+$/,
      message: "密码必须包含大小写字母和数字",
      trigger: "blur",
    },
  ],
  confirmPassword: [
    { required: true, message: "请确认新密码", trigger: "blur" },
    {
      validator: (rule, value, callback) => {
        if (value !== passwordForm.newPassword) {
          callback(new Error("两次输入的密码不一致"));
        } else {
          callback();
        }
      },
      trigger: "blur",
    },
  ],
};

const phoneRules = {
  phone: [
    { required: true, message: "请输入手机号码", trigger: "blur" },
    {
      pattern: /^1[3-9]\d{9}$/,
      message: "请输入正确的手机号码",
      trigger: "blur",
    },
  ],
  code: [
    { required: true, message: "请输入验证码", trigger: "blur" },
    { pattern: /^\d{6}$/, message: "验证码为6位数字", trigger: "blur" },
  ],
};

const emailRules = {
  email: [
    { required: true, message: "请输入邮箱地址", trigger: "blur" },
    { type: "email", message: "请输入正确的邮箱地址", trigger: "blur" },
  ],
  code: [
    { required: true, message: "请输入验证码", trigger: "blur" },
    { pattern: /^\d{6}$/, message: "验证码为6位数字", trigger: "blur" },
  ],
};

const deleteAccountRules = {
  password: [{ required: true, message: "请输入密码", trigger: "blur" }],
  confirm: [
    {
      validator: (rule, value, callback) => {
        if (!value) {
          callback(new Error("请确认您理解此操作不可撤销"));
        } else {
          callback();
        }
      },
      trigger: "change",
    },
  ],
};

/**
 * 掩码处理手机号
 * @param {string} phone - 手机号
 * @returns {string} 掩码后的手机号
 */
const maskPhone = (phone) => {
  if (!phone || phone.length < 11) return phone;
  return phone.replace(/(\d{3})\d{4}(\d{4})/, "$1****$2");
};

/**
 * 掩码处理邮箱
 * @param {string} email - 邮箱
 * @returns {string} 掩码后的邮箱
 */
const maskEmail = (email) => {
  if (!email || !email.includes("@")) return email;
  const [username, domain] = email.split("@");
  if (username.length <= 2) return email;
  return `${username.substring(0, 2)}***@${domain}`;
};

/**
 * 开始倒计时
 */
const startCountdown = () => {
  countdown.value = 60;
  clearInterval(countdownTimer);
  countdownTimer = setInterval(() => {
    countdown.value--;
    if (countdown.value <= 0) {
      clearInterval(countdownTimer);
    }
  }, 1000);
};

/**
 * 提交密码修改表单
 */
const submitPasswordForm = async () => {
  if (!passwordFormRef.value) return;

  await passwordFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.password = true;
      try {
        await userStore.changePassword({
          oldPassword: passwordForm.currentPassword,
          newPassword: passwordForm.newPassword,
        });

        ElMessage.success("密码修改成功");
        showPasswordForm.value = false;

        // 重置表单
        passwordForm.currentPassword = "";
        passwordForm.newPassword = "";
        passwordForm.confirmPassword = "";
      } catch (error) {
        console.error("修改密码失败", error);
        ElMessage.error("修改密码失败，请重试");
      } finally {
        loading.password = false;
      }
    }
  });
};

/**
 * 发送手机验证码
 */
const sendPhoneCode = async () => {
  if (!phoneForm.phone) {
    ElMessage.warning("请先输入手机号码");
    return;
  }

  if (!/^1[3-9]\d{9}$/.test(phoneForm.phone)) {
    ElMessage.warning("请输入正确的手机号码");
    return;
  }

  try {
    // 模拟API调用
    await new Promise((resolve) => setTimeout(resolve, 1000));

    // 实际项目中应调用API发送验证码
    // await userStore.sendPhoneCode(phoneForm.phone);

    ElMessage.success(`验证码已发送至 ${maskPhone(phoneForm.phone)}`);
    startCountdown();
  } catch (error) {
    console.error("发送验证码失败", error);
    ElMessage.error("发送验证码失败，请重试");
  }
};

/**
 * 提交手机绑定表单
 */
const submitPhoneForm = async () => {
  if (!phoneFormRef.value) return;

  await phoneFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.phone = true;
      try {
        // 模拟API调用
        await new Promise((resolve) => setTimeout(resolve, 1000));

        // 实际项目中应调用API绑定手机
        // await userStore.bindPhone(phoneForm);

        ElMessage.success("手机绑定成功");
        showPhoneForm.value = false;

        // 更新用户信息
        userInfo.value.phone = phoneForm.phone;

        // 重置表单
        phoneForm.code = "";
      } catch (error) {
        console.error("绑定手机失败", error);
        ElMessage.error("绑定手机失败，请重试");
      } finally {
        loading.phone = false;
      }
    }
  });
};

/**
 * 发送邮箱验证码
 */
const sendEmailCode = async () => {
  if (!emailForm.email) {
    ElMessage.warning("请先输入邮箱地址");
    return;
  }

  if (!/^[\w-]+(\.[\w-]+)*@[\w-]+(\.[\w-]+)+$/.test(emailForm.email)) {
    ElMessage.warning("请输入正确的邮箱地址");
    return;
  }

  try {
    // 模拟API调用
    await new Promise((resolve) => setTimeout(resolve, 1000));

    // 实际项目中应调用API发送验证码
    // await userStore.sendEmailCode(emailForm.email);

    ElMessage.success(`验证码已发送至 ${maskEmail(emailForm.email)}`);
    startCountdown();
  } catch (error) {
    console.error("发送验证码失败", error);
    ElMessage.error("发送验证码失败，请重试");
  }
};

/**
 * 提交邮箱绑定表单
 */
const submitEmailForm = async () => {
  if (!emailFormRef.value) return;

  await emailFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.email = true;
      try {
        // 模拟API调用
        await new Promise((resolve) => setTimeout(resolve, 1000));

        // 实际项目中应调用API绑定邮箱
        // await userStore.bindEmail(emailForm);

        ElMessage.success("邮箱绑定成功");
        showEmailForm.value = false;

        // 更新用户信息
        userInfo.value.email = emailForm.email;
        userInfo.value.emailVerified = false;

        // 重置表单
        emailForm.code = "";
      } catch (error) {
        console.error("绑定邮箱失败", error);
        ElMessage.error("绑定邮箱失败，请重试");
      } finally {
        loading.email = false;
      }
    }
  });
};

/**
 * 发送邮箱验证链接
 */
const sendVerifyEmail = async () => {
  loading.emailVerify = true;
  try {
    // 模拟API调用
    await new Promise((resolve) => setTimeout(resolve, 1000));

    // 实际项目中应调用API发送验证邮件
    // await userStore.sendVerifyEmail();

    ElMessage.success(`验证邮件已发送至 ${maskEmail(userInfo.value.email)}`);
  } catch (error) {
    console.error("发送验证邮件失败", error);
    ElMessage.error("发送验证邮件失败，请重试");
  } finally {
    loading.emailVerify = false;
  }
};

/**
 * 保存隐私设置
 */
const savePrivacySettings = async () => {
  loading.privacy = true;
  try {
    // 模拟API调用
    await new Promise((resolve) => setTimeout(resolve, 1000));

    // 实际项目中应调用API保存设置
    // await userStore.savePrivacySettings(privacySettings);

    ElMessage.success("隐私设置已保存");
  } catch (error) {
    console.error("保存隐私设置失败", error);
    ElMessage.error("保存隐私设置失败，请重试");
  } finally {
    loading.privacy = false;
  }
};

/**
 * 导出用户数据
 */
const exportUserData = async () => {
  loading.export = true;
  try {
    // 模拟API调用
    await new Promise((resolve) => setTimeout(resolve, 2000));

    // 实际项目中应调用API导出数据
    // const data = await userStore.exportUserData();
    // 下载数据文件
    // const blob = new Blob([JSON.stringify(data)], { type: 'application/json' });
    // const url = URL.createObjectURL(blob);
    // const a = document.createElement('a');
    // a.href = url;
    // a.download = `user_data_${Date.now()}.json`;
    // a.click();
    // URL.revokeObjectURL(url);

    ElMessage.success("数据导出成功，请在下载文件夹中查看");
  } catch (error) {
    console.error("导出数据失败", error);
    ElMessage.error("导出数据失败，请重试");
  } finally {
    loading.export = false;
  }
};

/**
 * 显示删除账号对话框
 */
const showDeleteAccountDialog = () => {
  ElMessageBox.confirm(
    "删除账号将永久移除您的所有数据，包括个人信息、阅读历史和收藏书籍。此操作不可撤销，确定要继续吗？",
    "警告",
    {
      confirmButtonText: "继续",
      cancelButtonText: "取消",
      type: "warning",
    }
  )
    .then(() => {
      deleteAccountDialog.value = true;
    })
    .catch(() => {
      // 取消操作
    });
};

/**
 * 确认删除账号
 */
const confirmDeleteAccount = async () => {
  if (!deleteAccountFormRef.value) return;

  await deleteAccountFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.deleteAccount = true;
      try {
        // 模拟API调用
        await new Promise((resolve) => setTimeout(resolve, 2000));

        // 实际项目中应调用API删除账号
        // await userStore.deleteAccount(deleteAccountForm.password);

        ElMessage.success("账号已删除");
        deleteAccountDialog.value = false;

        // 退出登录并跳转到首页
        userStore.logout();
        router.push("/");
      } catch (error) {
        console.error("删除账号失败", error);
        ElMessage.error("删除账号失败，请检查密码是否正确");
      } finally {
        loading.deleteAccount = false;
      }
    }
  });
};

/**
 * 初始化设置
 */
const initSettings = () => {
  // 初始化手机号
  if (userInfo.value.phone) {
    phoneForm.phone = userInfo.value.phone;
  }

  // 初始化邮箱
  if (userInfo.value.email) {
    emailForm.email = userInfo.value.email;
  }

  // 实际项目中应从API获取设置
  // const settings = await userStore.getUserSettings();
  // Object.assign(privacySettings, settings.privacy);
};

onMounted(() => {
  // 初始化设置
  initSettings();
});
</script>

<style scoped>
.user-settings {
  width: 100%;
}

.settings-tabs {
  width: 100%;
}

.tab-content {
  padding: 10px 0;
}

.section-title {
  font-size: 18px;
  font-weight: 600;
  margin: 0 0 20px;
  color: #303133;
}

.settings-card {
  margin-bottom: 24px;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 30px;
  color: #909399;
  text-align: center;
}

.placeholder-icon {
  font-size: 24px;
  margin-bottom: 10px;
}

.card-info {
  display: flex;
  align-items: center;
  padding: 20px;
  color: #606266;
}

.info-icon {
  font-size: 20px;
  margin-right: 10px;
  color: #409eff;
}

.verify-tag {
  margin-left: 10px;
}

.verification-code {
  display: flex;
  gap: 10px;
}

.verification-code .el-input {
  flex: 1;
}

.privacy-settings {
  padding: 10px;
}

.setting-desc {
  margin-left: 10px;
  font-size: 13px;
  color: #909399;
}

.data-management {
  padding: 10px;
}

.data-action {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 0;
}

.action-info h3 {
  margin: 0 0 5px;
  font-size: 16px;
  font-weight: 500;
  color: #303133;
}

.action-info p {
  margin: 0;
  font-size: 13px;
  color: #909399;
}

.danger-zone .action-info h3 {
  color: #f56c6c;
}

.delete-account-dialog {
  margin-bottom: 20px;
}

.dialog-text {
  margin: 20px 0;
  font-size: 14px;
  color: #606266;
}

/* 响应式布局 */
@media screen and (max-width: 768px) {
  .verification-code {
    flex-direction: column;
  }

  .data-action {
    flex-direction: column;
    align-items: flex-start;
    gap: 15px;
  }

  .data-action .el-button {
    width: 100%;
  }
}

/* 深色模式适配 */
@media (prefers-color-scheme: dark) {
  .section-title {
    color: #e5e7eb;
  }

  .action-info h3 {
    color: #e5e7eb;
  }

  .action-info p,
  .setting-desc,
  .dialog-text {
    color: #a0a0a0;
  }

  .card-placeholder {
    color: #a0a0a0;
  }

  .card-info {
    color: #a0a0a0;
  }

  .danger-zone .action-info h3 {
    color: #f56c6c;
  }
}
</style>
