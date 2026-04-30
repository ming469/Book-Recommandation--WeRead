<template>
  <div class="user-help">
    <div class="help-header">
      <h2 class="section-title">帮助中心</h2>
      <div class="search-box">
        <el-input
          v-model="searchQuery"
          placeholder="搜索帮助内容"
          prefix-icon="Search"
          clearable
          @input="handleSearch"
        />
      </div>
    </div>

    <div class="help-content">
      <!-- 左侧导航 -->
      <div class="help-nav">
        <h3 class="nav-title">常见问题分类</h3>
        <el-menu
          :default-active="activeCategory"
          class="help-menu"
          @select="handleCategorySelect"
        >
          <el-menu-item v-for="category in categories" :key="category.id" :index="category.id">
            <el-icon><component :is="category.icon" /></el-icon>
            <span>{{ category.name }}</span>
          </el-menu-item>
        </el-menu>
        
        <div class="contact-support">
          <h3 class="nav-title">联系客服</h3>
          <div class="contact-methods">
            <div class="contact-method">
              <el-icon><Message /></el-icon>
              <span>客服邮箱：support@weread.com</span>
            </div>
            <div class="contact-method">
              <el-icon><Phone /></el-icon>
              <span>客服电话：400-123-4567</span>
            </div>
            <div class="contact-method">
              <el-icon><ChatDotRound /></el-icon>
              <span>在线客服：9:00-18:00</span>
            </div>
          </div>
          <el-button type="primary" class="contact-btn" @click="startChat">
            <el-icon><Service /></el-icon>
            在线咨询
          </el-button>
        </div>
      </div>

      <!-- 右侧内容 -->
      <div class="help-main">
        <div v-if="searchQuery" class="search-results">
          <h3 class="result-title">搜索结果：{{ filteredFaqs.length }} 条</h3>
          <el-empty v-if="filteredFaqs.length === 0" description="未找到相关内容" />
          <div v-else class="faq-list">
            <div v-for="faq in filteredFaqs" :key="faq.id" class="faq-item">
              <div class="faq-question" @click="toggleFaq(faq.id)">
                <span>{{ faq.question }}</span>
                <el-icon class="toggle-icon" :class="{ 'is-active': activeFaqs.includes(faq.id) }">
                  <ArrowDown />
                </el-icon>
              </div>
              <div class="faq-answer" v-show="activeFaqs.includes(faq.id)" v-html="faq.answer"></div>
            </div>
          </div>
        </div>
        
        <div v-else class="category-content">
          <h3 class="category-title">{{ currentCategory.name }}</h3>
          <p class="category-desc">{{ currentCategory.description }}</p>
          
          <div class="faq-list">
            <div v-for="faq in currentFaqs" :key="faq.id" class="faq-item">
              <div class="faq-question" @click="toggleFaq(faq.id)">
                <span>{{ faq.question }}</span>
                <el-icon class="toggle-icon" :class="{ 'is-active': activeFaqs.includes(faq.id) }">
                  <ArrowDown />
                </el-icon>
              </div>
              <div class="faq-answer" v-show="activeFaqs.includes(faq.id)" v-html="faq.answer"></div>
            </div>
          </div>
          
          <el-empty v-if="currentFaqs.length === 0" description="暂无相关内容" />
        </div>
        
        <div class="feedback-section">
          <h3 class="feedback-title">这些内容对您有帮助吗？</h3>
          <div class="feedback-buttons">
            <el-button @click="submitFeedback(true)">
              <el-icon><Check /></el-icon>
              有帮助
            </el-button>
            <el-button @click="showFeedbackForm = true">
              <el-icon><Close /></el-icon>
              没有帮助
            </el-button>
          </div>
          
          <el-collapse-transition>
            <div v-if="showFeedbackForm" class="feedback-form">
              <h4>请告诉我们您的问题</h4>
              <el-input
                v-model="feedbackContent"
                type="textarea"
                :rows="3"
                placeholder="请描述您遇到的问题或建议..."
                maxlength="500"
                show-word-limit
              />
              <div class="form-actions">
                <el-button @click="showFeedbackForm = false">取消</el-button>
                <el-button type="primary" @click="submitDetailedFeedback" :loading="submittingFeedback">
                  提交反馈
                </el-button>
              </div>
            </div>
          </el-collapse-transition>
        </div>
      </div>
    </div>
    
    <!-- 在线客服对话框 -->
    <el-dialog
      v-model="chatDialogVisible"
      title="在线客服"
      width="400px"
      custom-class="chat-dialog"
    >
      <div class="chat-container">
        <div class="chat-messages" ref="chatMessagesRef">
          <div v-for="(message, index) in chatMessages" :key="index" 
               class="chat-message" 
               :class="{ 'message-self': message.self }">
            <div class="message-content">{{ message.content }}</div>
            <div class="message-time">{{ message.time }}</div>
          </div>
        </div>
        
        <div class="chat-input">
          <el-input
            v-model="chatInput"
            placeholder="请输入您的问题..."
            :disabled="!chatConnected"
            @keyup.enter="sendChatMessage"
          >
            <template #append>
              <el-button @click="sendChatMessage" :disabled="!chatConnected || !chatInput.trim()">
                发送
              </el-button>
            </template>
          </el-input>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick, watch } from 'vue';
import { ElMessage } from 'element-plus';
import { format } from 'date-fns';

// 搜索查询
const searchQuery = ref('');

// 当前激活的分类
const activeCategory = ref('account');

// 展开的FAQ项
const activeFaqs = ref([]);

// 反馈表单
const showFeedbackForm = ref(false);
const feedbackContent = ref('');
const submittingFeedback = ref(false);

// 在线客服对话框
const chatDialogVisible = ref(false);
const chatConnected = ref(false);
const chatInput = ref('');
const chatMessages = ref([]);
const chatMessagesRef = ref(null);

// FAQ分类
const categories = [
  { 
    id: 'account', 
    name: '账号相关', 
    icon: 'User',
    description: '关于账号注册、登录、安全等问题的解答。'
  },
  { 
    id: 'reading', 
    name: '阅读体验', 
    icon: 'Reading',
    description: '关于图书阅读、书架管理、阅读进度等问题的解答。'
  },
  { 
    id: 'payment', 
    name: '支付与订阅', 
    icon: 'CreditCard',
    description: '关于支付方式、订阅服务、发票等问题的解答。'
  },
  { 
    id: 'technical', 
    name: '技术问题', 
    icon: 'Setting',
    description: '关于网站使用、功能操作、兼容性等技术问题的解答。'
  },
  { 
    id: 'policy', 
    name: '政策与规则', 
    icon: 'Document',
    description: '关于用户协议、隐私政策、版权等问题的解答。'
  },
  { 
    id: 'other', 
    name: '其他问题', 
    icon: 'Warning',
    description: '其他常见问题的解答。'
  }
];

// FAQ数据
const faqs = [
  {
    id: 'faq-1',
    categoryId: 'account',
    question: '如何注册微读账号？',
    answer: `
      <p>注册微读账号非常简单，只需按照以下步骤操作：</p>
      <ol>
        <li>点击网站右上角的"登录"按钮</li>
        <li>在登录页面点击"注册新账号"</li>
        <li>填写您的邮箱、手机号和密码</li>
        <li>点击"获取验证码"并输入收到的验证码</li>
        <li>阅读并同意用户协议和隐私政策</li>
        <li>点击"注册"完成账号创建</li>
      </ol>
      <p>注册成功后，您可以立即登录并开始使用微读的所有功能。</p>
    `
  },
  {
    id: 'faq-2',
    categoryId: 'account',
    question: '忘记密码怎么办？',
    answer: `
      <p>如果您忘记了密码，可以通过以下步骤重置：</p>
      <ol>
        <li>点击登录页面的"忘记密码"链接</li>
        <li>输入您注册时使用的邮箱或手机号</li>
        <li>选择通过邮箱或手机接收验证码</li>
        <li>输入收到的验证码</li>
        <li>设置新密码并确认</li>
        <li>点击"确认"完成密码重置</li>
      </ol>
      <p>如果您无法收到验证码，请检查垃圾邮件文件夹或联系客服获取帮助。</p>
    `
  },
  {
    id: 'faq-3',
    categoryId: 'account',
    question: '如何修改个人资料？',
    answer: `
      <p>您可以在个人中心修改您的个人资料：</p>
      <ol>
        <li>登录您的微读账号</li>
        <li>点击右上角的头像或用户名</li>
        <li>选择"个人中心"</li>
        <li>在左侧菜单中选择"个人资料"</li>
        <li>点击"编辑资料"按钮</li>
        <li>修改您的昵称、头像、个人简介等信息</li>
        <li>点击"保存修改"完成更新</li>
      </ol>
    `
  },
  {
    id: 'faq-4',
    categoryId: 'reading',
    question: '如何将书籍添加到我的收藏？',
    answer: `
      <p>将书籍添加到收藏非常简单：</p>
      <ol>
        <li>浏览或搜索您感兴趣的书籍</li>
        <li>点击书籍封面进入详情页</li>
        <li>点击书籍详情页上的"收藏"按钮</li>
      </ol>
      <p>您也可以在阅读过程中点击页面上的收藏图标。所有收藏的书籍都会显示在您的"我的收藏"页面中，您可以随时查看和管理。</p>
    `
  },
  {
    id: 'faq-5',
    categoryId: 'reading',
    question: '阅读历史会保存多久？',
    answer: `
      <p>微读会永久保存您的阅读历史记录，除非您手动删除。您可以随时在"阅读历史"页面查看您曾经阅读过的所有书籍。</p>
      <p>如果您希望清除阅读历史，可以：</p>
      <ol>
        <li>进入"个人中心"</li>
        <li>选择"阅读历史"</li>
        <li>点击"清空历史"按钮</li>
        <li>确认操作</li>
      </ol>
      <p>您也可以选择删除单条历史记录，只需点击该记录旁边的删除图标即可。</p>
    `
  },
  {
    id: 'faq-6',
    categoryId: 'technical',
    question: '微读支持哪些设备和浏览器？',
    answer: `
      <p>微读是一个响应式网站，支持各种设备和主流浏览器：</p>
      <p><strong>支持的设备：</strong></p>
      <ul>
        <li>台式电脑</li>
        <li>笔记本电脑</li>
        <li>平板电脑</li>
        <li>智能手机</li>
      </ul>
      <p><strong>支持的浏览器：</strong></p>
      <ul>
        <li>Google Chrome (推荐)</li>
        <li>Mozilla Firefox</li>
        <li>Microsoft Edge</li>
        <li>Safari</li>
        <li>Opera</li>
      </ul>
      <p>为了获得最佳体验，我们建议使用最新版本的浏览器。</p>
    `
  },
  {
    id: 'faq-7',
    categoryId: 'payment',
    question: '微读提供哪些支付方式？',
    answer: `
      <p>微读支持多种支付方式，包括：</p>
      <ul>
        <li>支付宝</li>
        <li>微信支付</li>
        <li>银联卡</li>
        <li>信用卡 (Visa, MasterCard, JCB)</li>
        <li>PayPal (国际用户)</li>
      </ul>
      <p>所有支付信息均经过加密处理，确保您的支付安全。</p>
    `
  },
  {
    id: 'faq-8',
    categoryId: 'policy',
    question: '如何保护我的个人隐私？',
    answer: `
      <p>微读非常重视用户隐私保护，我们采取多种措施保护您的个人信息：</p>
      <ol>
        <li>所有个人数据均经过加密存储</li>
        <li>我们不会未经您的同意向第三方分享您的个人信息</li>
        <li>您可以在账号设置中控制隐私选项</li>
        <li>您可以随时查看、修改或删除您的个人数据</li>
      </ol>
      <p>您可以在"账号设置">"隐私设置"中调整您的隐私偏好，包括个人资料可见性、阅读历史可见性等。</p>
      <p>详细信息请参阅我们的<a href="/privacy-policy">隐私政策</a>。</p>
    `
  },
  {
    id: 'faq-9',
    categoryId: 'other',
    question: '如何提交图书推荐？',
    answer: `
      <p>我们欢迎用户提交图书推荐，帮助我们丰富平台内容：</p>
      <ol>
        <li>点击网站底部的"联系我们"</li>
        <li>选择"图书推荐"类别</li>
        <li>填写图书信息，包括书名、作者、出版社等</li>
        <li>简要说明为什么推荐这本书</li>
        <li>提交表单</li>
      </ol>
      <p>我们的编辑团队会定期审核用户推荐，并将优质内容添加到平台中。</p>
    `
  },
  {
    id: 'faq-10',
    categoryId: 'account',
    question: '如何注销我的账号？',
    answer: `
      <p>如果您希望注销账号，请按照以下步骤操作：</p>
      <ol>
        <li>登录您的微读账号</li>
        <li>进入"个人中心">"账号设置"</li>
        <li>选择"隐私设置"标签页</li>
        <li>滚动到页面底部，找到"数据管理"部分</li>
        <li>点击"删除账号"按钮</li>
        <li>阅读注销须知并确认</li>
        <li>输入密码进行验证</li>
        <li>点击"确认删除"完成注销</li>
      </ol>
      <p><strong>注意：</strong>账号注销后，所有数据将被永久删除且无法恢复，包括您的个人信息、阅读历史、收藏书籍等。</p>
    `
  }
];

// 当前分类
const currentCategory = computed(() => {
  return categories.find(category => category.id === activeCategory.value) || categories[0];
});

// 当前分类的FAQ
const currentFaqs = computed(() => {
  return faqs.filter(faq => faq.categoryId === activeCategory.value);
});

// 搜索结果
const filteredFaqs = computed(() => {
  if (!searchQuery.value) return [];
  
  const query = searchQuery.value.toLowerCase();
  return faqs.filter(faq => 
    faq.question.toLowerCase().includes(query) || 
    faq.answer.toLowerCase().includes(query)
  );
});

/**
 * 处理分类选择
 * @param {string} categoryId - 分类ID
 */
const handleCategorySelect = (categoryId) => {
  activeCategory.value = categoryId;
  searchQuery.value = ''; // 清空搜索
};

/**
 * 处理搜索
 */
const handleSearch = () => {
  // 如果搜索框为空，恢复显示分类内容
  if (!searchQuery.value.trim()) {
    searchQuery.value = '';
  }
};

/**
 * 切换FAQ展开/折叠状态
 * @param {string} faqId - FAQ ID
 */
const toggleFaq = (faqId) => {
  const index = activeFaqs.value.indexOf(faqId);
  if (index === -1) {
    activeFaqs.value.push(faqId);
  } else {
    activeFaqs.value.splice(index, 1);
  }
};

/**
 * 提交简单反馈
 * @param {boolean} helpful - 是否有帮助
 */
const submitFeedback = (helpful) => {
  // 实际项目中应调用API提交反馈
  ElMessage.success(helpful ? '感谢您的反馈！' : '请告诉我们如何改进');
  
  if (helpful) {
    showFeedbackForm.value = false;
  } else {
    showFeedbackForm.value = true;
  }
};

/**
 * 提交详细反馈
 */
const submitDetailedFeedback = async () => {
  if (!feedbackContent.value.trim()) {
    ElMessage.warning('请输入反馈内容');
    return;
  }
  
  submittingFeedback.value = true;
  
  try {
    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 1000));
    
    // 实际项目中应调用API提交反馈
    // await api.submitFeedback({ content: feedbackContent.value });
    
    ElMessage.success('感谢您的反馈，我们会认真考虑您的建议');
    showFeedbackForm.value = false;
    feedbackContent.value = '';
  } catch (error) {
    console.error('提交反馈失败', error);
    ElMessage.error('提交反馈失败，请重试');
  } finally {
    submittingFeedback.value = false;
  }
};

/**
 * 启动在线客服聊天
 */
const startChat = () => {
  chatDialogVisible.value = true;
  chatConnected.value = false;
  chatMessages.value = [
    {
      content: '正在连接客服系统，请稍候...',
      time: format(new Date(), 'HH:mm:ss'),
      self: false
    }
  ];
  
  // 模拟连接过程
  setTimeout(() => {
    chatConnected.value = true;
    chatMessages.value.push({
      content: '您好，我是微读客服小微，很高兴为您服务。请问有什么可以帮助您的？',
      time: format(new Date(), 'HH:mm:ss'),
      self: false
    });
    
    scrollToBottom();
  }, 1500);
};

/**
 * 发送聊天消息
 */
const sendChatMessage = () => {
  if (!chatInput.value.trim() || !chatConnected.value) return;
  
  // 添加用户消息
  chatMessages.value.push({
    content: chatInput.value,
    time: format(new Date(), 'HH:mm:ss'),
    self: true
  });
  
  const userMessage = chatInput.value;
  chatInput.value = '';
  
  scrollToBottom();
  
  // 模拟客服回复
  setTimeout(() => {
    // 根据用户消息生成自动回复
    let reply = '感谢您的咨询。我们的客服人员会尽快处理您的问题。';
    
    if (userMessage.includes('密码') || userMessage.includes('忘记')) {
      reply = '如果您忘记了密码，可以通过登录页面的"忘记密码"功能重置。需要我为您提供详细步骤吗？';
    } else if (userMessage.includes('注册') || userMessage.includes('账号')) {
      reply = '注册微读账号非常简单，只需在首页点击"登录"按钮，然后选择"注册新账号"并按照提示操作即可。';
    } else if (userMessage.includes('收藏') || userMessage.includes('书架')) {
      reply = '您可以在浏览书籍时点击"收藏"按钮将书籍添加到您的收藏中。所有收藏的书籍都可以在"我的收藏"页面查看。';
    } else if (userMessage.includes('支付') || userMessage.includes('订阅')) {
      reply = '微读支持多种支付方式，包括支付宝、微信支付、银联卡和信用卡等。您可以在订阅页面选择适合您的支付方式。';
    }
    
    chatMessages.value.push({
      content: reply,
      time: format(new Date(), 'HH:mm:ss'),
      self: false
    });
    
    scrollToBottom();
  }, 1000);
};

/**
 * 滚动到聊天窗口底部
 */
const scrollToBottom = () => {
  nextTick(() => {
    if (chatMessagesRef.value) {
      chatMessagesRef.value.scrollTop = chatMessagesRef.value.scrollHeight;
    }
  });
};

// 监听聊天窗口变化，自动滚动到底部
watch(chatMessages, () => {
  scrollToBottom();
});

onMounted(() => {
  // 默认展开第一个FAQ
  if (currentFaqs.value.length > 0) {
    activeFaqs.value.push(currentFaqs.value[0].id);
  }
});
</script>

<style scoped>
.user-help {
  width: 100%;
}

.help-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  flex-wrap: wrap;
  gap: 16px;
}

.section-title {
  font-size: 20px;
  font-weight: 600;
  margin: 0;
  color: #303133;
}

.search-box {
  width: 300px;
}

.help-content {
  display: flex;
  gap: 24px;
}

/* 左侧导航 */
.help-nav {
  width: 240px;
  flex-shrink: 0;
}

.nav-title {
  font-size: 16px;
  font-weight: 600;
  margin: 0 0 12px;
  color: #303133;
}

.help-menu {
  margin-bottom: 30px;
  border-radius: 4px;
  border: 1px solid #e4e7ed;
}

.contact-support {
  margin-top: 30px;
}

.contact-methods {
  margin-bottom: 20px;
}

.contact-method {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
  font-size: 14px;
  color: #606266;
}

.contact-method .el-icon {
  margin-right: 8px;
  color: #409EFF;
}

.contact-btn {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

/* 右侧内容 */
.help-main {
  flex: 1;
  min-width: 0;
}

.category-title {
  font-size: 18px;
  font-weight: 600;
  margin: 0 0 8px;
  color: #303133;
}

.category-desc {
  margin: 0 0 24px;
  color: #606266;
  font-size: 14px;
}

.result-title {
  font-size: 16px;
  font-weight: 600;
  margin: 0 0 16px;
  color: #303133;
}

.faq-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.faq-item {
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  overflow: hidden;
  transition: box-shadow 0.3s ease;
}

.faq-item:hover {
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
}

.faq-question {
  padding: 16px;
  background-color: #f5f7fa;
  cursor: pointer;
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 500;
  color: #303133;
}

.toggle-icon {
  transition: transform 0.3s ease;
}

.toggle-icon.is-active {
  transform: rotate(180deg);
}

.faq-answer {
  padding: 16px;
  color: #606266;
  line-height: 1.6;
  border-top: 1px solid #e4e7ed;
}

.faq-answer :deep(p) {
  margin: 0 0 12px;
}

.faq-answer :deep(ul), .faq-answer :deep(ol) {
  margin: 12px 0;
  padding-left: 24px;
}

.faq-answer :deep(li) {
  margin-bottom: 8px;
}

.faq-answer :deep(a) {
  color: #409EFF;
  text-decoration: none;
}

.faq-answer :deep(a:hover) {
  text-decoration: underline;
}

/* 反馈部分 */
.feedback-section {
  margin-top: 40px;
  padding-top: 20px;
  border-top: 1px solid #e4e7ed;
}

.feedback-title {
  font-size: 16px;
  font-weight: 500;
  margin: 0 0 16px;
  color: #303133;
}

.feedback-buttons {
  display: flex;
  gap: 12px;
}

.feedback-form {
  margin-top: 20px;
  padding: 16px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.feedback-form h4 {
  margin: 0 0 12px;
  font-size: 14px;
  font-weight: 500;
  color: #303133;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 12px;
  gap: 12px;
}

/* 聊天对话框 */
.chat-container {
  display: flex;
  flex-direction: column;
  height: 400px;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.chat-message {
  max-width: 80%;
  padding: 10px 12px;
  border-radius: 8px;
  background-color: #f2f6fc;
  align-self: flex-start;
}

.message-self {
  background-color: #ecf5ff;
  align-self: flex-end;
}

.message-content {
  font-size: 14px;
  color: #303133;
  word-break: break-word;
}

.message-time {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
  text-align: right;
}

.chat-input {
  padding: 12px 16px;
  border-top: 1px solid #e4e7ed;
}

/* 响应式布局 */
@media screen and (max-width: 768px) {
  .help-content {
    flex-direction: column;
  }
  
  .help-nav {
    width: 100%;
  }
  
  .search-box {
    width: 100%;
  }
}

/* 深色模式适配 */
@media (prefers-color-scheme: dark) {
  .section-title, .category-title, .nav-title, .feedback-title, .result-title {
    color: #e5e7eb;
  }
  
  .category-desc {
    color: #a0a0a0;
  }
  
  .help-menu, .faq-item {
    border-color: #333;
  }
  
  .faq-question {
    background-color: #2c2c2c;
    color: #e5e7eb;
  }
  
  .faq-answer {
    color: #a0a0a0;
    border-color: #333;
  }
  
  .contact-method {
    color: #a0a0a0;
  }
  
  .feedback-section {
    border-color: #333;
  }
  
  .feedback-form {
    background-color: #2c2c2c;
  }
  
  .feedback-form h4 {
    color: #e5e7eb;
  }
  
  .chat-message {
    background-color: #2c2c2c;
  }
  
  .message-self {
    background-color: #18222c;
  }
  
  .message-content {
    color: #e5e7eb;
  }
  
  .chat-input {
    border-color: #333;
  }
}
</style>