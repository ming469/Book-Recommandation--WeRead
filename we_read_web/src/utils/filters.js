/**
 * 格式化日期
 * @param {string|Date} date - 需要格式化的日期
 * @param {string} [format='YYYY-MM-DD'] - 格式化格式
 * @returns {string} 格式化后的日期字符串
 */
export function formatDate(date, format = "YYYY-MM-DD") {
  if (!date) return "";
  const d = new Date(date);
  const year = d.getFullYear();
  const month = String(d.getMonth() + 1).padStart(2, "0");
  const day = String(d.getDate()).padStart(2, "0");

  return format.replace("YYYY", year).replace("MM", month).replace("DD", day);
}

/**
 * 截断文本
 * @param {string} text - 需要截断的文本
 * @param {number} [length=20] - 允许的最大长度
 * @returns {string} 截断后的文本
 */
export function truncateText(text, length = 20) {
  if (!text) return "";
  return text.length > length ? text.substring(0, length) + "..." : text;
}
