<template>
  <div class="explore-paper">
    <section class="masthead">
      <div class="brand">
        <div class="brand-title">探索更多</div>
        <div class="brand-sub">A Quiet Reading Gazette</div>
      </div>
      <div class="mast-meta">
        <div class="meta-line">{{ today }}</div>
        <div class="meta-dot">•</div>
        <div class="meta-line">Vol. {{ volume }}</div>
      </div>
    </section>

    <div class="divider"></div>

    <section class="section">
      <header class="section-head">
        <span class="section-badge">Daily</span>
        <h2 class="section-title">今日闲翻</h2>
        <p class="section-note">随手翻开，或许正合你意</p>
      </header>
      <div class="cards">
        <article v-for="(it, i) in dailyPicks" :key="i" class="card">
          <div class="card-title">{{ it.title }}</div>
          <div class="card-author">{{ it.author }}</div>
          <div class="card-brief">{{ it.brief }}</div>
          <div class="card-tagline">{{ it.tagline }}</div>
        </article>
      </div>
    </section>

    <div class="divider"></div>

    <section class="section">
      <header class="section-head">
        <span class="section-badge">Scenarios</span>
        <h2 class="section-title">如果你…</h2>
        <p class="section-note mono">情境对照表：给当下的你一份更贴近的选书方向</p>
      </header>
      <div class="scenarios">
        <div v-for="(s, i) in scenarios" :key="i" class="scenario">
          <div class="scenario-q">如果你 {{ s.when }}</div>
          <ul class="scenario-a">
            <li v-for="(r, j) in s.recos" :key="j">{{ r }}</li>
          </ul>
        </div>
      </div>
    </section>

    <div class="divider"></div>

    <section class="section">
      <header class="section-head">
        <span class="section-badge">Blind Box</span>
        <h2 class="section-title">盲盒书架</h2>
        <p class="section-note">不做选择题，让好书替你“撞”进来</p>
      </header>
      <div class="blind-grid">
        <div v-for="(b, i) in blindBoxes" :key="i" class="blind-box">
          <div class="blind-no">拆封编号 {{ b.no }}</div>
          <div class="blind-line">{{ b.line }}</div>
          <div class="blind-meta mono">{{ b.hint }}</div>
        </div>
      </div>
    </section>

    <div class="divider"></div>

    <section class="section">
      <header class="section-head">
        <span class="section-badge">Time</span>
        <h2 class="section-title">时间碎片</h2>
        <p class="section-note mono">把阅读分装进不同长度的片刻</p>
      </header>
      <div class="time-chips">
        <div v-for="(t, i) in timeSlices" :key="i" class="time-chip">
          <div class="chip-time">{{ t.label }}</div>
          <div class="chip-sug">{{ t.sug }}</div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { computed } from "vue";

const now = new Date();
const today = computed(() => {
  const y = now.getFullYear();
  const m = String(now.getMonth() + 1).padStart(2, "0");
  const d = String(now.getDate()).padStart(2, "0");
  return `${y}.${m}.${d}`;
});
const volume = computed(() => {
  const dayOfYear = Math.floor((now - new Date(now.getFullYear(), 0, 0)) / 86400000);
  return `${now.getFullYear().toString().slice(-2)}-${dayOfYear}`;
});

const dailyPicks = [
  { title: "风从海面来", author: "林栖", brief: "清淡里见真味", tagline: "适合午后微风与慢咖啡" },
  { title: "局外人的边上", author: "S·M", brief: "一点旁观，一点自辩", tagline: "给想与自己对话的夜晚" },
  { title: "远方仍在远方", author: "简行者", brief: "旅行札记与地理私语", tagline: "边走边记，比到达更重要" },
  { title: "纸上故人来", author: "旧笺", brief: "信札里的人情练达", tagline: "读书，也读人间温度" },
];

const scenarios = [
  { when: "只想放空头脑", recos: ["短篇随笔", "轻松治愈系", "漫画小品"] },
  { when: "需要一点力量", recos: ["人物传记", "真实故事", "心理复健"] },
  { when: "睡前十页刚好", recos: ["诗歌集", "小短篇合集", "微型小说"] },
  { when: "想和世界和解", recos: ["散文地理", "人类学小书", "乡土文学"] },
];

const blindBoxes = [
  { no: "A-07", line: "当春天迟到三周，花也会学会耐心。", hint: "主题：耐心与成长" },
  { no: "B-12", line: "夜里的一封信，比白天更诚实。", hint: "主题：自我表达" },
  { no: "C-03", line: "地图上的虚线，也在引路。", hint: "主题：旅行与迷路" },
  { no: "D-21", line: "把疑问折好，夹进书页。", hint: "主题：提问的艺术" },
];

const timeSlices = [
  { label: "5–10 分钟", sug: "诗歌 / 微随笔 / 书摘" },
  { label: "20–30 分钟", sug: "短篇小说 / 主题专栏" },
  { label: "1 小时+", sug: "长文随笔 / 长篇连载" },
];
</script>

<style scoped>
.explore-paper {
  background:
    linear-gradient(180deg, rgba(0,0,0,0.02), rgba(0,0,0,0.02)),
    #f7f2e7;
  min-height: 100%;
  padding: 28px 16px 80px;
  color: #333;
  font-family: Georgia, "Times New Roman", serif;
}
.mono { font-family: "Courier New", Courier, monospace; }

.masthead {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  padding: 12px 6px 6px;
}
.brand-title {
  font-size: 30px;
  letter-spacing: 1px;
  color: #2f2f2f;
}
.brand-sub {
  margin-top: 4px;
  color: #6b4b3a;
  font-size: 12px;
  letter-spacing: .6px;
}
.mast-meta {
  display: flex;
  align-items: center;
  color: #6b4b3a;
  font-size: 12px;
}
.meta-line { padding: 0 6px; }
.meta-dot { color: #8b5e3c; }

.divider {
  height: 12px;
  margin: 10px 0 18px;
  background-image: repeating-linear-gradient(90deg, transparent 0 10px, rgba(139,94,60,.35) 10px 12px);
  transform: rotate(-0.2deg);
}

.section { margin-bottom: 28px; }
.section-head { text-align: center; margin-bottom: 14px; }
.section-badge {
  display: inline-block;
  padding: 2px 8px;
  font-size: 11px;
  color: #f4ede4;
  background: #8b5e3c;
  letter-spacing: .5px;
  border-radius: 10px;
}
.section-title {
  margin: 8px 0 4px;
  font-size: 22px;
  color: #2e2d2b;
}
.section-note {
  margin: 0;
  font-size: 12px;
  color: #5f5b55;
}

.cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
}
.card {
  background: #f9f4ea;
  border: 1px solid #e7dfd2;
  border-radius: 8px;
  padding: 12px;
  min-height: 120px;
}
.card-title { font-size: 16px; color: #2f2f2f; }
.card-author { font-size: 12px; color: #6b4b3a; margin: 4px 0; }
.card-brief { font-size: 13px; color: #4b4b4b; margin: 6px 0; }
.card-tagline { font-size: 12px; color: #7b6a5b; }

.scenarios {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 14px;
}
.scenario {
  background: #fbf6ec;
  border: 1px dashed #d9cdbb;
  border-radius: 8px;
  padding: 12px;
}
.scenario-q {
  color: #2e2d2b;
  font-size: 15px;
  margin-bottom: 6px;
}
.scenario-a {
  margin: 0 0 0 16px;
  padding: 0;
  color: #5b5752;
}
.scenario-a li { line-height: 1.8; }

.blind-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
}
.blind-box {
  background: #f6efe3;
  border: 1px solid #e3d6c2;
  border-radius: 8px;
  padding: 12px;
}
.blind-no {
  font-size: 12px;
  color: #8b5e3c;
  letter-spacing: 1px;
}
.blind-line { margin: 6px 0; color: #3a3937; }
.blind-meta { color: #6c625a; font-size: 12px; }

.time-chips {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 10px;
}
.time-chip {
  background: #f9f2e6;
  border: 1px solid #e7dbc6;
  border-radius: 10px;
  padding: 10px 12px;
}
.chip-time { color: #2f2f2f; font-size: 14px; }
.chip-sug { color: #6c625a; font-size: 12px; margin-top: 4px; }

@media (max-width: 1024px) {
  .cards { grid-template-columns: repeat(2, 1fr); }
  .blind-grid { grid-template-columns: repeat(2, 1fr); }
}
@media (max-width: 640px) {
  .cards { grid-template-columns: 1fr; }
  .scenarios { grid-template-columns: 1fr; }
  .blind-grid { grid-template-columns: 1fr; }
  .time-chips { grid-template-columns: 1fr; }
}
</style>
