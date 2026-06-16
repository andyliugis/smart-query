<template>
  <div class="chat-layout">
    <!-- 左侧：会话列表 -->
    <SessionSidebar
      ref="sidebarRef"
      :current-session-id="currentSessionId"
      @select="handleSelectSession"
    />

    <!-- 中间：对话区域 -->
    <div class="chat-main">
      <!-- 顶部栏 -->
      <div class="chat-header">
        <div class="header-left">
          <h2 class="session-title">{{ currentSessionTitle || '新会话' }}</h2>
          <span class="session-desc">与 AI 对话，探索数据</span>
        </div>
        <div class="header-right">
          <el-dropdown trigger="click">
            <el-button text size="small" class="user-menu-btn">
              <el-avatar :size="28" :icon="UserFilled" />
              <span class="user-name">{{ userInfo?.nickname || userInfo?.username || '未登录' }}</span>
            </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item v-if="!isLoggedIn" @click="goToLogin">
                  <el-icon><User /></el-icon> 登录
                </el-dropdown-item>
                <el-dropdown-item v-if="isLoggedIn" @click="handleLogout" divided>
                  <el-icon><SwitchButton /></el-icon> 退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>

      <!-- 消息区域 -->
      <div class="chat-messages" ref="messagesRef">
        <!-- 欢迎区域 -->
        <div v-if="messages.length === 0" class="welcome-section">
          <div class="welcome-content">
            <div class="welcome-icon">🚀</div>
            <h2>欢迎使用智能问数</h2>
            <p>用自然语言提问，AI 帮您生成 SQL 并分析数据</p>
            <div class="example-questions">
              <span class="example-label">试试这些问题：</span>
              <div class="example-list">
                <el-tag
                  v-for="q in exampleQuestions"
                  :key="q"
                  class="example-tag"
                  @click="sendExample(q)"
                  effect="plain"
                  type="info"
                >
                  {{ q }}
                </el-tag>
              </div>
            </div>
          </div>
        </div>

        <!-- 消息列表 -->
        <div
          v-for="(msg, index) in messages"
          :key="msg.id || index"
          class="message-wrapper"
          @click="handleSelectMessage(msg)"
        >
          <MessageCard
            :message="msg"
            :response="msg.response"
            :loading="msg.loading"
            @like="handleLike(msg)"
            @dislike="handleDislike(msg)"
            @sql-executed="(response) => handleSqlExecuted(msg, response)"
          />
        </div>
      </div>

      <!-- 底部输入区 -->
      <div class="chat-input-section">
        <div class="input-container">
          <el-input
            v-model="inputText"
            type="textarea"
            :rows="2"
            placeholder="输入您的问题，用自然语言描述您想查询的数据..."
            resize="none"
            @keydown="handleKeyDown"
            :disabled="isSending"
          />
          <div class="input-actions">
            <div class="action-left">
              <el-button text size="small">
                <span style="font-size: 16px;">📎</span>
              </el-button>
            </div>
            <div class="action-right">
              <el-button
                type="primary"
                :disabled="!inputText.trim() || isSending"
                :loading="isSending"
                @click="sendMessage"
              >
                <span style="margin-right: 4px;">🚀</span>
                发送
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 右侧：详情面板 -->
    <DetailPanel
      :selected-message="selectedMessage"
      @copy-sql="copySQL"
      @requery="handleRequery"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, nextTick, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { UserFilled, User, SwitchButton } from '@element-plus/icons-vue'
import SessionSidebar from '../components/SessionSidebar.vue'
import MessageCard from '../components/MessageCard.vue'
import DetailPanel from '../components/DetailPanel.vue'
import { queryData, type QueryResponse } from '../api/queryApi'
import { getSessionMessages, createSession, type ChatMessage } from '../api/chatApi'
import { submitFeedback } from '../api/feedbackApi'
import { getUserInfo, clearAuthData, isAuthenticated } from '../api/authApi'

interface MessageWithResponse extends ChatMessage {
  response?: QueryResponse
  loading?: boolean
}

// 状态
const router = useRouter()
const sidebarRef = ref()
const messagesRef = ref<HTMLElement>()
const currentSessionId = ref<number>()
const messages = ref<MessageWithResponse[]>([])
const inputText = ref('')
const isSending = ref(false)
const selectedMessage = ref<MessageWithResponse>()
const userInfo = ref<{ userId: number; username: string; nickname: string } | null>(null)
const isLoggedIn = computed(() => isAuthenticated())

onMounted(() => {
  // 从 localStorage 加载用户信息
  const info = getUserInfo()
  if (info) {
    userInfo.value = info
  } else if (!isAuthenticated()) {
    // 未登录，跳转到登录页
    router.push('/login')
  }
})

const exampleQuestions = [
  '上个月销售额最高的产品是什么？',
  '各产品类别的总销售额是多少？',
  '哪个区域的业绩最好？',
  '按月份统计订单数量趋势'
]

const currentSessionTitle = computed(() => {
  const firstUserMsg = messages.value.find(m => m.role === 'user')
  if (firstUserMsg) {
    const title = firstUserMsg.content.substring(0, 30)
    return title.length < firstUserMsg.content.length ? title + '...' : title
  }
  return '新会话'
})

// 会话操作
async function handleSelectSession(sessionId: number) {
  currentSessionId.value = sessionId
  await loadSessionMessages(sessionId)
}

async function loadSessionMessages(sessionId: number) {
  try {
    const sessionMessages = await getSessionMessages(sessionId)
    // 转换消息格式
    messages.value = sessionMessages.map(msg => {
      const enhancedMsg: MessageWithResponse = { ...msg }
      if (msg.role === 'assistant' && msg.resultData) {
        try {
          enhancedMsg.response = JSON.parse(msg.resultData)
        } catch {
          enhancedMsg.response = {
            success: false,
            sql: msg.generatedSql || '',
            columns: [],
            data: [],
            errorMessage: '解析失败',
            explanation: ''
          }
        }
      }
      return enhancedMsg
    })
  } catch (error) {
    console.error('加载会话失败', error)
    ElMessage.error('加载会话失败')
  }
  scrollToBottom()
}

// 消息操作
async function sendMessage() {
  const question = inputText.value.trim()
  if (!question || isSending.value) return

  // 如果没有会话，先创建
  if (!currentSessionId.value) {
    try {
      const newSession = await createSession(question)
      currentSessionId.value = newSession.id
      sidebarRef.value?.loadSessions()
    } catch (error) {
      ElMessage.error('创建会话失败')
      return
    }
  }

  // 添加用户消息
  const userMsg: MessageWithResponse = {
    id: Date.now(),
    sessionId: currentSessionId.value!,
    role: 'user',
    content: question,
    createdAt: new Date().toISOString()
  }
  messages.value.push(userMsg)
  inputText.value = ''
  scrollToBottom()

  // 添加 AI 加载消息
  const aiMsg: MessageWithResponse = {
    id: Date.now() + 1,
    sessionId: currentSessionId.value!,
    role: 'assistant',
    content: '',
    loading: true,
    createdAt: new Date().toISOString()
  }
  messages.value.push(aiMsg)
  scrollToBottom()

  isSending.value = true

  try {
    // 使用普通查询接口（一次性返回完整结果）
    const response = await queryData({ question, sessionId: currentSessionId.value })

    aiMsg.loading = false
    aiMsg.response = response
    aiMsg.generatedSql = response.sql
    if (response.success) {
      // 接口可能回传 sessionId（后端在新会话时会创建并返回）
      if (response.sessionId) {
        aiMsg.sessionId = response.sessionId
      }
      aiMsg.content = response.explanation
    }
  } catch (error: any) {
    aiMsg.loading = false
    aiMsg.response = {
      success: false,
      sql: '',
      columns: [],
      data: [],
      errorMessage: error.message || '查询失败，请检查后端服务',
      explanation: ''
    }
  } finally {
    isSending.value = false
    scrollToBottom()
  }
}

function sendExample(question: string) {
  inputText.value = question
  sendMessage()
}

function handleSelectMessage(msg: MessageWithResponse) {
  if (msg.role === 'assistant' && msg.response) {
    selectedMessage.value = msg
  }
}

function handleLike(msg: MessageWithResponse) {
  submitFeedback({
    sessionId: msg.sessionId,
    messageId: msg.id,
    feedbackType: 'like',
    question: messages.value.find(m => m.role === 'user')?.content,
    sql: msg.response?.sql
  }).then(() => {
    ElMessage.success('感谢您的反馈！这个查询已记录为优秀案例')
  }).catch(() => {
    ElMessage.success('感谢您的反馈！')
  })
}

function handleDislike(msg: MessageWithResponse) {
  submitFeedback({
    sessionId: msg.sessionId,
    messageId: msg.id,
    feedbackType: 'dislike',
    question: messages.value.find(m => m.role === 'user')?.content,
    sql: msg.response?.sql
  }).then(() => {
    ElMessage.info('感谢您的反馈！我们会持续改进')
  }).catch(() => {
    ElMessage.info('感谢您的反馈！')
  })
}

function handleSqlExecuted(msg: MessageWithResponse, response: QueryResponse) {
  msg.response = response
  msg.generatedSql = response.sql
}

function handleRequery() {
  ElMessage.info('重新查询功能即将上线')
}

async function copySQL(sql: string) {
  try {
    await navigator.clipboard.writeText(sql)
    ElMessage.success('SQL 已复制到剪贴板')
  } catch {
    ElMessage.warning('复制失败，请手动复制')
  }
}

function handleKeyDown(e: KeyboardEvent) {
  if (e.key === 'Enter' && !e.shiftKey) {
    e.preventDefault()
    sendMessage()
  }
}

function scrollToBottom() {
  nextTick(() => {
    if (messagesRef.value) {
      messagesRef.value.scrollTop = messagesRef.value.scrollHeight
    }
  })
}

function goToLogin() {
  router.push('/login')
}

async function handleLogout() {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    clearAuthData()
    ElMessage.success('已退出登录')
    router.push('/login')
  } catch {
    // 用户取消
  }
}
</script>

<style scoped>
.chat-layout {
  display: flex;
  height: 100vh;
  background: #f9fafb;
  overflow: hidden;
}

.chat-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
  background: #f9fafb;
}

.chat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 24px;
  background: #ffffff;
  border-bottom: 1px solid #e5e7eb;
}

.header-left {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.session-title {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
}

.session-desc {
  font-size: 12px;
  color: #9ca3af;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

.user-menu-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 4px 8px;
  border-radius: 6px;
}

.user-menu-btn:hover {
  background: #f3f4f6;
}

.user-name {
  font-size: 14px;
  color: #374151;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.welcome-section {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}

.welcome-content {
  text-align: center;
  max-width: 520px;
}

.welcome-icon {
  font-size: 56px;
  margin-bottom: 16px;
}

.welcome-content h2 {
  margin: 0 0 8px;
  font-size: 22px;
  color: #1f2937;
}

.welcome-content p {
  margin: 0 0 24px;
  color: #6b7280;
  font-size: 14px;
}

.example-questions {
  text-align: left;
}

.example-label {
  display: block;
  font-size: 12px;
  font-weight: 600;
  color: #6b7280;
  margin-bottom: 12px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.example-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.example-tag {
  cursor: pointer;
  transition: all 0.15s ease;
}

.example-tag:hover {
  background: #3b82f6;
  border-color: #3b82f6;
  color: #ffffff;
}

.message-wrapper {
  cursor: pointer;
}

.chat-input-section {
  padding: 16px 24px;
  background: #ffffff;
  border-top: 1px solid #e5e7eb;
}

.input-container {
  max-width: 900px;
  margin: 0 auto;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  background: #ffffff;
  overflow: hidden;
  transition: all 0.15s ease;
}

.input-container:focus-within {
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.input-container :deep(.el-textarea__inner) {
  border: none;
  box-shadow: none;
  padding: 12px 16px;
  resize: none;
}

.input-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 12px;
  border-top: 1px solid #f3f4f6;
  background: #f9fafb;
}
</style>
