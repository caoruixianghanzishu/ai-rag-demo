<template>
  <div class="chat-page">
    <!-- 左侧会话列表 -->
    <aside class="session-panel">
      <div class="panel-header">
        <span>会话列表</span>
        <el-button type="primary" size="small" :icon="Plus" @click="handleNewChat">
          新对话
        </el-button>
      </div>
      <div class="session-list" v-loading="sessionsLoading">
        <div
          v-for="item in sessions"
          :key="item.id"
          class="session-item"
          :class="{ active: currentSessionId === item.id }"
          @click="selectSession(item.id)"
        >
          <el-icon><ChatDotRound /></el-icon>
          <div class="session-info">
            <div class="session-name">{{ item.sessionName }}</div>
            <div class="session-time">{{ formatTime(item.createTime) }}</div>
          </div>
        </div>
        <el-empty v-if="!sessionsLoading && sessions.length === 0" description="暂无会话" />
      </div>
    </aside>

    <!-- 右侧聊天区域 -->
    <main class="chat-panel">
      <div class="chat-header">
        <span>{{ currentSessionId ? '继续对话' : '新对话' }}</span>
        <el-switch
          v-model="streamMode"
          active-text="流式输出"
          inactive-text="普通模式"
        />
      </div>

      <div ref="messageBoxRef" class="message-box">
        <div v-if="messages.length === 0" class="welcome">
          <el-icon :size="48" color="#409eff"><ChatLineSquare /></el-icon>
          <h3>高校学工智能助手</h3>
          <p>你可以问我关于奖学金、学工政策等问题</p>
          <div class="quick-questions">
            <el-tag
              v-for="q in quickQuestions"
              :key="q"
              class="quick-tag"
              effect="plain"
              @click="sendQuickQuestion(q)"
            >
              {{ q }}
            </el-tag>
          </div>
        </div>

        <div
          v-for="(msg, index) in messages"
          :key="index"
          class="message-item"
          :class="msg.role"
        >
          <div class="avatar">
            <el-icon v-if="msg.role === 'user'"><User /></el-icon>
            <el-icon v-else><Cpu /></el-icon>
          </div>
          <div class="bubble">
            <div class="content" v-html="formatContent(msg.content)"></div>
            <div v-if="msg.loading" class="typing">
              <span></span><span></span><span></span>
            </div>
          </div>
        </div>
      </div>

      <div class="input-area">
        <el-input
          v-model="inputMessage"
          type="textarea"
          :rows="3"
          placeholder="输入你的问题，Enter 发送，Shift+Enter 换行"
          :disabled="sending"
          @keydown.enter="handleKeydown"
        />
        <div class="input-actions">
          <span class="tip">支持多轮对话，系统会记住上下文</span>
          <el-button
            type="primary"
            :loading="sending"
            :disabled="!inputMessage.trim()"
            @click="handleSend"
          >
            发送
          </el-button>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { chat, streamChat } from '../api/chat'
import { listSessions, getHistory } from '../api/session'

const sessions = ref([])
const sessionsLoading = ref(false)
const currentSessionId = ref(null)
const messages = ref([])
const inputMessage = ref('')
const sending = ref(false)
const streamMode = ref(true)
const messageBoxRef = ref(null)
let streamController = null

const quickQuestions = [
  '奖学金申请条件是什么？',
  '申请时间是什么时候？',
  '你好'
]

onMounted(() => {
  loadSessions()
})

async function loadSessions() {
  sessionsLoading.value = true
  try {
    const res = await listSessions()
    sessions.value = res.data || []
  } finally {
    sessionsLoading.value = false
  }
}

async function selectSession(sessionId) {
  if (sending.value) return
  currentSessionId.value = sessionId
  messages.value = []
  try {
    const res = await getHistory(sessionId)
    messages.value = (res.data || []).map((m) => ({
      role: m.role,
      content: m.content
    }))
    scrollToBottom()
  } catch {
    ElMessage.error('加载历史消息失败')
  }
}

function handleNewChat() {
  if (sending.value) return
  currentSessionId.value = null
  messages.value = []
  inputMessage.value = ''
}

function sendQuickQuestion(q) {
  inputMessage.value = q
  handleSend()
}

function handleKeydown(e) {
  if (e.key === 'Enter' && !e.shiftKey) {
    e.preventDefault()
    handleSend()
  }
}

async function handleSend() {
  const text = inputMessage.value.trim()
  if (!text || sending.value) return

  messages.value.push({ role: 'user', content: text })
  inputMessage.value = ''
  sending.value = true
  scrollToBottom()

  if (streamMode.value) {
    await sendStream(text)
  } else {
    await sendNormal(text)
  }

  sending.value = false
  await loadSessions()
  if (!currentSessionId.value && sessions.value.length > 0) {
    currentSessionId.value = sessions.value[0].id
  }
}

async function sendNormal(text) {
  const assistantMsg = { role: 'assistant', content: '', loading: true }
  messages.value.push(assistantMsg)

  try {
    const payload = { message: text }
    if (currentSessionId.value) {
      payload.sessionId = currentSessionId.value
    }
    const res = await chat(payload)
    assistantMsg.content = res.data.answer
    assistantMsg.loading = false
    currentSessionId.value = res.data.sessionId
  } catch (e) {
    assistantMsg.content = '请求失败，请稍后重试'
    assistantMsg.loading = false
  }
  scrollToBottom()
}

function sendStream(text) {
  return new Promise((resolve) => {
    const assistantMsg = { role: 'assistant', content: '', loading: true }
    messages.value.push(assistantMsg)
    scrollToBottom()

    const params = new URLSearchParams({ message: text })
    if (currentSessionId.value) {
      params.append('sessionId', currentSessionId.value)
    }

    streamController = streamChat(
      text,
      currentSessionId.value,
      (chunk) => {
        assistantMsg.loading = false
        assistantMsg.content += chunk
        scrollToBottom()
      },
      () => {
        assistantMsg.loading = false
        resolve()
      },
      () => {
        assistantMsg.loading = false
        if (!assistantMsg.content) {
          assistantMsg.content = '流式响应中断，请重试'
        }
        resolve()
      }
    )
  })
}

function scrollToBottom() {
  nextTick(() => {
    if (messageBoxRef.value) {
      messageBoxRef.value.scrollTop = messageBoxRef.value.scrollHeight
    }
  })
}

function formatTime(time) {
  if (!time) return ''
  const d = new Date(time)
  return `${d.getMonth() + 1}/${d.getDate()} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
}

function formatContent(text) {
  if (!text) return ''
  return text
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
    .replace(/\n/g, '<br/>')
}
</script>

<style scoped>
.chat-page {
  display: flex;
  height: 100%;
  gap: 16px;
}

.session-panel {
  width: 280px;
  background: #fff;
  border-radius: 12px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px;
  border-bottom: 1px solid #ebeef5;
  font-weight: 600;
}

.session-list {
  flex: 1;
  overflow-y: auto;
  padding: 8px;
}

.session-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px;
  border-radius: 8px;
  cursor: pointer;
  transition: background 0.2s;
}

.session-item:hover {
  background: #f5f7fa;
}

.session-item.active {
  background: #ecf5ff;
  color: #409eff;
}

.session-info {
  flex: 1;
  min-width: 0;
}

.session-name {
  font-size: 14px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.session-time {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.chat-panel {
  flex: 1;
  background: #fff;
  border-radius: 12px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.chat-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  border-bottom: 1px solid #ebeef5;
  font-weight: 600;
}

.message-box {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
}

.welcome {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #606266;
  gap: 12px;
}

.welcome h3 {
  color: #303133;
}

.quick-questions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  justify-content: center;
  margin-top: 8px;
}

.quick-tag {
  cursor: pointer;
}

.message-item {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
}

.message-item.user {
  flex-direction: row-reverse;
}

.avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  font-size: 18px;
}

.message-item.user .avatar {
  background: #409eff;
  color: #fff;
}

.message-item.assistant .avatar {
  background: #f0f2f5;
  color: #606266;
}

.bubble {
  max-width: 70%;
}

.message-item.user .bubble .content {
  background: #409eff;
  color: #fff;
  border-radius: 12px 12px 4px 12px;
}

.message-item.assistant .bubble .content {
  background: #f4f4f5;
  color: #303133;
  border-radius: 12px 12px 12px 4px;
}

.content {
  padding: 12px 16px;
  line-height: 1.6;
  font-size: 14px;
  word-break: break-word;
}

.typing {
  display: flex;
  gap: 4px;
  padding: 8px 0 0 16px;
}

.typing span {
  width: 6px;
  height: 6px;
  background: #909399;
  border-radius: 50%;
  animation: bounce 1.4s infinite ease-in-out both;
}

.typing span:nth-child(1) { animation-delay: -0.32s; }
.typing span:nth-child(2) { animation-delay: -0.16s; }

@keyframes bounce {
  0%, 80%, 100% { transform: scale(0); }
  40% { transform: scale(1); }
}

.input-area {
  padding: 16px 20px;
  border-top: 1px solid #ebeef5;
}

.input-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 12px;
}

.tip {
  font-size: 12px;
  color: #909399;
}
</style>
