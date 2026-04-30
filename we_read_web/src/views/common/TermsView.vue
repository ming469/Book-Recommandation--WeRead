<template>
  <div class="terms-page">
    <header class="hero">
      <div class="hero-inner">
        <h1 class="title">图书推荐系统用户协议（草案）</h1>
        <p class="subtitle">请在使用本系统前认真阅读并理解以下条款</p>
      </div>
    </header>

    <section class="section">
      <h2>1. 服务内容</h2>
      <p>
        本系统是一款基于用户兴趣与行为数据的个性化图书推荐工具。我们根据您的阅读历史、评分、搜索行为及其他互动信息，为您生成图书推荐列表。我们保留随时更新、优化推荐算法的权利，以不断提升推荐质量。
      </p>
    </section>

    <section class="section">
      <h2>2. 用户行为规范</h2>
      <p>
        您在使用本系统时，应遵守国家法律法规，尊重图书版权与知识产权。禁止利用本系统进行任何形式的虚假刷量、恶意抓取推荐数据、发布违法或侵权书评等行为。若发现违规，我们有权限制或终止您的使用权限。
      </p>
    </section>

    <section class="section">
      <h2>3. 数据收集与使用</h2>
      <p>
        为了提供精准推荐，我们会收集您主动提供的数据（如对图书的评分、标记“想读/读过”、撰写的书评、收藏的书单）以及自动记录的数据（如搜索关键词、在每本书详情页的停留时间、点击的推荐位）。这些数据将仅用于优化您的个性化推荐体验，不会以可识别您个人身份的形式向第三方出售或分享。具体请参考我们的《隐私政策》。
      </p>
    </section>

    <section class="section">
      <h2>4. 推荐系统的运行逻辑</h2>
      <p>
        您在本系统看到的推荐结果，是由算法根据以下一个或多个因素自动生成的：您的历史阅读偏好、与您相似的用户群体的阅读选择、近期热门图书、编辑精选或您主动关注的阅读达人。请注意，推荐系统并非绝对客观，可能无法完全捕捉您瞬息万变的阅读心情。您可以通过点击“不感兴趣”或“减少此类推荐”来主动调校推荐模型。
      </p>
    </section>

    <section class="section">
      <h2>5. 第三方链接与内容</h2>
      <p>
        本系统的推荐结果可能包含指向第三方电商平台或书评网站的链接。我们对这些第三方网站的内容、隐私政策或服务不承担任何责任。在跳转前，建议您查阅相关网站的条款。
      </p>
    </section>

    <section class="section">
      <h2>6. 免责声明</h2>
      <p>
        图书推荐结果仅作为您的阅读参考，不构成必须购买或阅读的建议。您根据推荐进行的任何消费或阅读决策，责任自负。在法律允许的最大范围内，我们不对推荐结果的准确性、完整性或适用性承担任何明示或暗示的担保。
      </p>
    </section>

    <section class="section">
      <h2>7. 协议修改</h2>
      <p>
        我们可能会不时修订本协议。对于重大变更，我们将通过应用内通知或您注册的邮箱进行告知。在变更生效后继续使用本系统，即表示您同意受修订后的协议约束。
      </p>
    </section>

    <section class="section">
      <h2>8. 联系我们</h2>
      <p>
        如您对本协议或推荐机制有任何疑问，欢迎通过 [你的联系邮箱] 与我们沟通。
      </p>
    </section>
  </div>
  </template>

<script setup>
</script>

<style scoped>
.terms-page {
  max-width: 900px;
  margin: 0 auto;
  padding: 24px 16px 60px;
}
.hero {
  background: linear-gradient(135deg, #f5f7fa 0%, #eef2f6 100%);
  border-radius: 12px;
  padding: 28px 20px;
  margin-bottom: 24px;
  border: 1px solid #ebeef5;
}
.hero-inner {
  text-align: center;
}
.title {
  font-size: 24px;
  font-weight: 700;
  margin: 0;
  color: #303133;
}
.subtitle {
  margin: 6px 0 0;
  color: #606266;
  font-size: 14px;
}
.section {
  padding: 18px 2px;
  border-bottom: 1px solid #f0f2f5;
}
.section:last-child {
  border-bottom: none;
}
.section h2 {
  font-size: 18px;
  margin: 0 0 10px;
  color: #303133;
}
.section p {
  line-height: 1.8;
  color: #606266;
  margin: 0;
}
@media (max-width: 768px) {
  .title { font-size: 20px; }
  .terms-page { padding: 16px 12px 40px; }
}
</style>
