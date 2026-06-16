<template>
  <div class="session-sidebar">
    <!-- 顶部 Logo 和新建按钮 -->
    <div class="sidebar-header">
      <div class="logo-section">
        <span class="logo-icon">🔍</span>
        <span class="logo-text">智能问数</span>
      </div>
      <el-button type="primary" size="small" class="new-session-btn" @click="handleNewSession">
        <span class="btn-icon">+</span>
        新建会话
      </el-button>
    </div>

    <!-- 会话列表 -->
    <div class="sessions-container">
      <div class="section-title">我的会话</div>
      <div class="session-list" v-loading="loading">
        <div
          v-for="session in sessions"
          :key="session.id"
          :class="['session-item', { active: currentSessionId === session.id }]"
          @click="selectSession(session)"
        >
          <div class="session-icon">
            <span>📊</span>
          </div>
          <div class="session-info">
            <div class="session-title">{{ session.title || '新会话' }}</div>
            <div class="session-time">{{ formatTime(session.updatedAt) }}</div>
          </div>
          <div class="session-actions">
            <el-dropdown trigger="click" @command="(cmd: 'rename' | 'delete') => handleSessionAction(cmd, session)">
              <el-button text size="small" class="more-btn">
                <span class="more-icon">⋮</span>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="rename">重命名</el-dropdown-item>
                  <el-dropdown-item command="delete" divided>删除</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>
        <div v-if="sessions.length === 0 && !loading" class="empty-sessions">
          <el-empty description="暂无会话" :image-size="80" />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getSessions, createSession, type ChatSession } from '../api/chatApi'

const props = defineProps<{
  currentSessionId?: number
}>()

const emit = defineEmits<{
  (e: 'select', sessionId: number): void
  (e: 'create'): void
}>()

const sessions = ref<ChatSession[]>([])
const loading = ref(false)

async function loadSessions() {
  try {
    loading.value = true
    sessions.value = await getSessions()
  } catch (error) {
    console.error('加载会话失败', error)
  } finally {
    loading.value = false
  }
}

function selectSession(session: ChatSession) {
  emit('select', session.id)
}

async function handleNewSession() {
  try {
    const newSession = await createSession('新会话')
    sessions.value.unshift(newSession)
    emit('select', newSession.id)
    ElMessage.success('会话已创建')
  } catch (error) {
    ElMessage.error('创建会话失败')
  }
}

async function handleSessionAction(cmd: 'rename' | 'delete', session: ChatSession) {
  if (cmd === 'rename') {
    try {
      const newTitle = await ElMessageBox.prompt('请输入新的会话名称', '重命名', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        inputValue: session.title || '新会话'
      })
      session.title = newTitle.value
      ElMessage.success('已重命名')
    } catch {
    }
  } else if (cmd === 'delete') {
    try {
      await ElMessageBox.confirm('确定要删除这个会话吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
      const index = sessions.value.findIndex(s => s.id === session.id)
      if (index > -1) {
        sessions.value.splice(index, 1)
      }
      if (props.currentSessionId === session.id && sessions.value.length > 0) {
        emit('select', sessions.value[0].id)
      }
      ElMessage.success('会话已删除')
    } catch {
    }
  }
}

function formatTime(timeStr: string): string {
  const date = new Date(timeStr)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  const minutes = Math.floor(diff / 60000)
  const hours = Math.floor(diff / 3600000)
  const days = Math.floor(diff / 86400000)

  if (minutes < 1) return '刚刚'
  if (minutes < 60) return `${minutes} 分钟前`
  if (hours < 24) return `${hours} 小时前`
  if (days < 7) return `${days} 天前`
  
  return date.toLocaleDateString('zh-CN')
}

onMounted(() => {
  loadSessions()
})

defineExpose({
  loadSessions
})
</script>

<style scoped>
.session-sidebar {
  width: 260px;
  height: 100vh;
  background: #ffffff;
  border-right: 1px solid #e5e7eb;
  display: flex;
  flex-direction: column;
}

.sidebar-header {
  padding: 16px;
  border-bottom: 1px solid #e5e7eb;
}

.logo-section {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  font-size: 18px;
  color: #1f2937;
  margin-bottom: 16px;
}

.logo-icon {
  font-size: 24px;
}

.logo-text {
  font-weight: 700;
}

.new-session-btn {
  width: 100%;
  background: #3b82f6;
  border: none;
  font-weight: 500;
  height: 36px;
  border-radius: 6px;
}

.new-session-btn:hover {
  background: #2563eb;
}

.btn-icon {
  margin-right: 4px;
  font-size: 16px;
}

.sessions-container {
  flex: 1;
  overflow-y: auto;
  padding: 12px;
}

.section-title {
  font-size: 12px;
  font-weight: 600;
  color: #6b7280;
  margin-bottom: 8px;
  padding: 0 8px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.session-list {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.session-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.15s ease;
}

.session-item:hover {
  background: #f3f4f6;
}

.session-item.active {
  background: #eff6ff;
  border: 1px solid #dbeafe;
}

.session-icon {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f3f4f6;
  border-radius: 6px;
  font-size: 16px;
}

.session-item.active .session-icon {
  background: #3b82f6;
}

.session-info {
  flex: 1;
  min-width: 0;
}

.session-title {
  font-size: 14px;
  color: #1f2937;
  font-weight: 500;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.session-item.active .session-title {
  color: #1f2937;
}

.session-time {
  font-size: 12px;
  color: #9ca3af;
  margin-top: 2px;
}

.session-actions {
  opacity: 0;
  transition: opacity 0.15s;
}

.session-item:hover .session-actions {
  opacity: 1;
}

.more-btn {
  padding: 4px;
  border-radius: 4px;
}

.more-icon {
  font-size: 18px;
  color: #6b7280;
}

.empty-sessions {
  padding: 40px 0;
}
</style>
