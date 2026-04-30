<template>
  <div class="find-container">
    <!-- 页面标题区域 -->
    <div class="find-header">
      <h1 class="page-title">发现我们</h1>
      <p class="page-subtitle">关注微读，随时了解最新动态</p>
    </div>

    <!-- 社交媒体卡片区域 -->
    <div class="social-media-section">
      <h2 class="section-title">关注我们的社交媒体</h2>
      <div class="social-cards">
        <el-card
          v-for="platform in socialPlatforms"
          :key="platform.id"
          class="social-card"
        >
          <div class="social-card-content">
            <div class="platform-icon">
              <el-icon :size="40"><component :is="platform.icon" /></el-icon>
            </div>
            <div class="platform-info">
              <h3 class="platform-name">{{ platform.name }}</h3>
              <p class="platform-desc">{{ platform.description }}</p>
              <div class="platform-account">
                <span class="account-label">{{ platform.accountLabel }}：</span>
                <span class="account-value">{{ platform.account }}</span>
              </div>
              <el-button
                type="primary"
                :icon="Link"
                class="follow-button"
                @click="openSocialLink(platform.url)"
              >
                {{ platform.buttonText }}
              </el-button>
            </div>
          </div>
        </el-card>
      </div>
    </div>

    <!-- 联系我们区域 -->
    <div class="contact-section">
      <el-divider>
        <el-icon><ChatDotRound /></el-icon>
        <span class="divider-text">联系我们</span>
      </el-divider>

      <el-row :gutter="20">
        <el-col
          :xs="24"
          :sm="12"
          :md="8"
          v-for="contact in contactInfo"
          :key="contact.id"
        >
          <el-card class="contact-card">
            <div class="contact-icon">
              <el-icon :size="32"><component :is="contact.icon" /></el-icon>
            </div>
            <h3 class="contact-title">{{ contact.title }}</h3>
            <p class="contact-value">{{ contact.value }}</p>
            <p class="contact-desc">{{ contact.description }}</p>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 微读应用下载区域 -->
    <div class="app-download-section">
      <el-divider>
        <el-icon><Download /></el-icon>
        <span class="divider-text">下载微读应用</span>
      </el-divider>

      <div class="app-cards">
        <el-card class="app-card">
          <div class="qr-code">
            <el-image src="/images/qrcode-android.png" fit="contain">
              <template #error>
                <div class="image-placeholder">
                  <el-icon><Picture /></el-icon>
                </div>
              </template>
            </el-image>
          </div>
          <div class="app-info">
            <h3>Android 版</h3>
            <p>扫描二维码或点击下方按钮下载</p>
            <el-button type="success" @click="downloadApp('android')">
              <el-icon><Download /></el-icon>
              下载安卓版
            </el-button>
          </div>
        </el-card>

        <el-card class="app-card">
          <div class="qr-code">
            <el-image src="/images/qrcode-ios.png" fit="contain">
              <template #error>
                <div class="image-placeholder">
                  <el-icon><Picture /></el-icon>
                </div>
              </template>
            </el-image>
          </div>
          <div class="app-info">
            <h3>iOS 版</h3>
            <p>扫描二维码或点击下方按钮下载</p>
            <el-button type="primary" @click="downloadApp('ios')">
              <el-icon><Download /></el-icon>
              下载 iOS 版
            </el-button>
          </div>
        </el-card>
      </div>
    </div>

    <!-- 常见问题区域 -->
    <div class="faq-section">
      <el-divider>
        <el-icon><QuestionFilled /></el-icon>
        <span class="divider-text">常见问题</span>
      </el-divider>

      <el-collapse accordion>
        <el-collapse-item
          v-for="(faq, index) in faqs"
          :key="index"
          :title="faq.question"
        >
          <div class="faq-answer">{{ faq.answer }}</div>
        </el-collapse-item>
      </el-collapse>

      <div class="more-help">
        <p>还有其他问题？</p>
        <el-button type="info" @click="goToHelpCenter">
          <el-icon><InfoFilled /></el-icon>
          访问帮助中心
        </el-button>
      </div>
    </div>

    <!-- 订阅我们的通讯 -->
    <div class="newsletter-section">
      <el-divider>
        <el-icon><Message /></el-icon>
        <span class="divider-text">订阅通讯</span>
      </el-divider>

      <el-card class="newsletter-card">
        <h3 class="newsletter-title">订阅微读通讯</h3>
        <p class="newsletter-desc">
          订阅我们的电子通讯，获取最新图书推荐和阅读资讯
        </p>

        <el-form
          :model="subscribeForm"
          :rules="subscribeRules"
          ref="subscribeFormRef"
          class="subscribe-form"
        >
          <el-form-item prop="email">
            <el-input
              v-model="subscribeForm.email"
              placeholder="请输入您的邮箱"
              clearable
            >
              <template #prefix>
                <el-icon><Message /></el-icon>
              </template>
            </el-input>
          </el-form-item>
          <el-form-item>
            <el-button
              type="primary"
              @click="submitSubscribe"
              :loading="subscribing"
            >
              <el-icon><Promotion /></el-icon>
              立即订阅
            </el-button>
          </el-form-item>
        </el-form>

        <p class="privacy-note">
          我们尊重您的隐私，不会向第三方分享您的信息。
          <el-link type="primary" :underline="false" @click="showPrivacyPolicy"
            >隐私政策</el-link
          >
        </p>
      </el-card>
    </div>

    <!-- 合作伙伴区域 -->
    <div class="partners-section">
      <el-divider>
        <el-icon><Connection /></el-icon>
        <span class="divider-text">合作伙伴</span>
      </el-divider>

      <div class="partners-list">
        <div v-for="partner in partners" :key="partner.id" class="partner-item">
          <el-image :src="partner.logo" fit="contain" class="partner-logo">
            <template #error>
              <div class="image-placeholder">
                <el-icon><Picture /></el-icon>
              </div>
            </template>
          </el-image>
          <p class="partner-name">{{ partner.name }}</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
/**
 * @file FindView.vue
 * @description "发现我们"页面，展示微读平台的社交媒体和联系方式
 * @updated 2025-03-12
 * @module views/FindView
 */
import { ref, reactive } from "vue";
import { useRouter } from "vue-router";
import { ElMessage, ElMessageBox } from "element-plus";

const router = useRouter();

// 社交媒体平台数据
const socialPlatforms = ref([
  {
    id: 1,
    name: "微博",
    icon: "Promotion",
    description: "关注微读官方微博，获取最新图书推荐和活动信息",
    accountLabel: "官方账号",
    account: "@微读官方",
    url: "https://weibo.com/weread",
    buttonText: "前往关注",
  },
  {
    id: 2,
    name: "微信公众号",
    icon: "ChatLineRound",
    description: "关注微读公众号，每日推送精选书籍和阅读心得",
    accountLabel: "公众号",
    account: "WeRead微读",
    url: "https://mp.weixin.qq.com/weread",
    buttonText: "扫码关注",
  },
  {
    id: 3,
    name: "抖音",
    icon: "Opportunity",
    description: "关注微读抖音号，观看精彩书评和作者访谈",
    accountLabel: "抖音号",
    account: "@微读书房",
    url: "https://douyin.com/weread",
    buttonText: "查看主页",
  },
]);

// 联系方式数据
const contactInfo = ref([
  {
    id: 1,
    title: "客服电话",
    icon: "Phone",
    value: "400-123-4567",
    description: "周一至周日 9:00-18:00",
  },
  {
    id: 2,
    title: "客服邮箱",
    icon: "Message",
    value: "support@weread.com",
    description: "我们会在24小时内回复您",
  },
  {
    id: 3,
    title: "公司地址",
    icon: "Location",
    value: "北京市海淀区中关村软件园",
    description: "欢迎参观我们的阅读空间",
  },
]);

// 常见问题数据
const faqs = ref([
  {
    question: "微读平台是做什么的？",
    answer:
      "微读是一个专注于电子书阅读的平台，提供海量图书资源，支持多端阅读，让用户随时随地享受阅读的乐趣。",
  },
  {
    question: "如何注册微读账号？",
    answer:
      "您可以通过微信、QQ、微博等第三方账号直接登录，也可以使用手机号或邮箱注册新账号。注册过程简单快捷，只需几步即可完成。",
  },
  {
    question: "微读支持哪些设备阅读？",
    answer:
      "微读支持Web网页版、iOS、Android等多种平台，您可以在手机、平板、电脑等设备上使用微读，数据会自动同步。",
  },
  {
    question: "如何联系客服解决问题？",
    answer:
      "您可以通过页面上提供的客服电话、邮箱联系我们，也可以在微信公众号留言，我们的客服团队会尽快为您解答问题。",
  },
]);

// 合作伙伴数据
const partners = ref([
  {
    id: 1,
    name: "腾讯",
    logo: new URL("@/assets/images/partners/tencent.png", import.meta.url).href,
  },
  {
    id: 2,
    name: "阿里巴巴",
    logo: new URL("@/assets/images/partners/alibaba.png", import.meta.url).href,
  },
  {
    id: 3,
    name: "百度",
    logo: new URL("@/assets/images/partners/baidu.png", import.meta.url).href,
  },
  {
    id: 4,
    name: "京东",
    logo: new URL("@/assets/images/partners/jd.png", import.meta.url).href,
  },
  {
    id: 5,
    name: "网易",
    logo: new URL("@/assets/images/partners/netease.png", import.meta.url).href,
  },
  {
    id: 6,
    name: "字节跳动",
    logo: new URL("@/assets/images/partners/bytedance.png", import.meta.url)
      .href,
  },
]);

// 订阅表单
const subscribeFormRef = ref(null);
const subscribing = ref(false);
const subscribeForm = reactive({
  email: "",
});
const subscribeRules = {
  email: [
    { required: true, message: "请输入邮箱地址", trigger: "blur" },
    { type: "email", message: "请输入正确的邮箱地址", trigger: "blur" },
  ],
};

// 方法：打开社交媒体链接
const openSocialLink = (url) => {
  window.open(url, "_blank");
};

// 方法：下载应用
const downloadApp = (platform) => {
  const downloadUrls = {
    android: "https://download.weread.com/android",
    ios: "https://apps.apple.com/cn/app/weread",
  };

  window.open(downloadUrls[platform], "_blank");
};

// 方法：前往帮助中心
const goToHelpCenter = () => {
  router.push("/user/help");
};

// 方法：提交订阅
const submitSubscribe = () => {
  if (!subscribeFormRef.value) return;

  subscribeFormRef.value.validate((valid) => {
    if (valid) {
      subscribing.value = true;

      // 模拟API请求
      setTimeout(() => {
        subscribing.value = false;
        ElMessage({
          type: "success",
          message: "订阅成功！感谢您的关注",
        });
        subscribeForm.email = "";
      }, 1500);
    }
  });
};

// 方法：显示隐私政策
const showPrivacyPolicy = () => {
  ElMessageBox.alert(
    "微读尊重并保护所有用户的个人隐私权。为了给您提供更准确、更有个性化的服务，微读会按照本隐私权政策的规定使用和披露您的个人信息。但微读将以高度的勤勉、审慎义务对待这些信息。除本隐私权政策另有规定外，在未征得您事先许可的情况下，微读不会将这些信息对外披露或向第三方提供。微读会不时更新本隐私权政策。您在同意微读服务使用协议之时，即视为您已经同意本隐私权政策全部内容。",
    "隐私政策",
    {
      confirmButtonText: "我已了解",
      customClass: "privacy-dialog",
    }
  );
};
</script>

<style scoped>
.find-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.find-header {
  text-align: center;
  margin-bottom: 40px;
  padding: 30px 0;
  background: linear-gradient(to right, #f0f9eb, #e1f3d8);
  border-radius: 8px;
}

.page-title {
  font-size: 32px;
  color: #409eff;
  margin-bottom: 10px;
}

.page-subtitle {
  font-size: 16px;
  color: #606266;
}

.section-title {
  font-size: 24px;
  color: #303133;
  margin-bottom: 20px;
  text-align: center;
}

.social-media-section {
  margin-bottom: 40px;
}

.social-cards {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
  justify-content: center;
}

.social-card {
  width: 100%;
  max-width: 350px;
  transition: transform 0.3s;
}

.social-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 15px rgba(0, 0, 0, 0.1);
}

.social-card-content {
  display: flex;
  align-items: center;
  gap: 15px;
}

.platform-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 70px;
  height: 70px;
  background-color: #f5f7fa;
  border-radius: 50%;
  color: #409eff;
}

.platform-info {
  flex: 1;
}

.platform-name {
  font-size: 18px;
  margin-bottom: 5px;
  color: #303133;
}

.platform-desc {
  font-size: 14px;
  color: #606266;
  margin-bottom: 10px;
}

.platform-account {
  font-size: 14px;
  margin-bottom: 15px;
}

.account-label {
  color: #909399;
}

.account-value {
  color: #303133;
  font-weight: bold;
}

.follow-button {
  margin-top: 5px;
}

.divider-text {
  margin: 0 8px;
  font-size: 16px;
}

.contact-section {
  margin: 40px 0;
}

.contact-card {
  height: 100%;
  text-align: center;
  padding: 10px;
  transition: transform 0.3s;
}

.contact-card:hover {
  transform: translateY(-5px);
}

.contact-icon {
  margin: 15px 0;
  color: #409eff;
}

.contact-title {
  font-size: 18px;
  margin-bottom: 10px;
  color: #303133;
}

.contact-value {
  font-size: 16px;
  font-weight: bold;
  color: #409eff;
  margin-bottom: 5px;
}

.contact-desc {
  font-size: 14px;
  color: #909399;
}

.app-download-section {
  margin: 40px 0;
}

.app-cards {
  display: flex;
  flex-wrap: wrap;
  gap: 30px;
  justify-content: center;
  margin-top: 20px;
}

.app-card {
  width: 100%;
  max-width: 300px;
  text-align: center;
}

.qr-code {
  width: 150px;
  height: 150px;
  margin: 0 auto 20px;
  border: 1px solid #ebeef5;
}

.image-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f5f7fa;
  color: #909399;
}

.app-info h3 {
  font-size: 18px;
  margin-bottom: 10px;
}

.app-info p {
  font-size: 14px;
  color: #606266;
  margin-bottom: 15px;
}

.faq-section {
  margin: 40px 0;
}

.faq-answer {
  padding: 10px;
  line-height: 1.6;
  color: #606266;
}

.more-help {
  text-align: center;
  margin-top: 30px;
}

.more-help p {
  margin-bottom: 10px;
  color: #606266;
}

.newsletter-section {
  margin: 40px 0;
}

.newsletter-card {
  max-width: 600px;
  margin: 0 auto;
  text-align: center;
}

.newsletter-title {
  font-size: 20px;
  color: #303133;
  margin-bottom: 10px;
}

.newsletter-desc {
  font-size: 14px;
  color: #606266;
  margin-bottom: 20px;
}

.subscribe-form {
  max-width: 400px;
  margin: 0 auto;
}

.privacy-note {
  font-size: 12px;
  color: #909399;
  margin-top: 15px;
}

.partners-section {
  margin: 40px 0;
}

.partners-list {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 20px;
  margin-top: 20px;
}

.partner-item {
  width: 120px;
  text-align: center;
}

.partner-logo {
  width: 80px;
  height: 80px;
  margin: 0 auto 10px;
  padding: 10px;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  background-color: #fff;
  transition: transform 0.3s;
}

.partner-logo:hover {
  transform: scale(1.05);
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.partner-name {
  font-size: 14px;
  color: #606266;
}

/* 响应式样式 */
@media screen and (max-width: 768px) {
  .find-header {
    padding: 20px 0;
  }

  .page-title {
    font-size: 24px;
  }

  .section-title {
    font-size: 20px;
  }

  .social-card {
    max-width: 100%;
  }

  .social-card-content {
    flex-direction: column;
    text-align: center;
  }

  .platform-icon {
    margin: 0 auto 15px;
  }

  .app-cards {
    flex-direction: column;
    align-items: center;
  }

  .partner-item {
    width: 100px;
  }

  .partner-logo {
    width: 60px;
    height: 60px;
  }
}

/* 自定义弹窗样式 */
:deep(.privacy-dialog) {
  max-width: 90%;
}

:deep(.privacy-dialog .el-message-box__content) {
  max-height: 60vh;
  overflow-y: auto;
  line-height: 1.6;
  text-align: justify;
}
</style>