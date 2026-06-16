<template>
  <div class="message-card">
    <!-- 消息头部 -->
    <div class="message-header">
      <div class="avatar" :class="isUser ? 'user-avatar' : 'ai-avatar'">
        <span v-if="isUser">👤</span>
        <span v-else>🔍</span>
      </div>
      <div class="header-info">
        <span class="name">{{ isUser ? '我' : '智能助手' }}</span>
        <span class="time">{{ formatTime(message.createdAt) }}</span>
      </div>
    </div>

    <!-- 消息内容 -->
    <div class="message-content">
      <!-- 用户消息 -->
      <template v-if="isUser">
        <p class="user-text">{{ message.content }}</p>
      </template>

      <!-- AI 消息 -->
      <template v-else>
        <!-- 加载状态 -->
        <div v-if="loading" class="loading-state">
          <div class="loading-icon">
            <span class="spinner"></span>
          </div>
          <span>正在分析您的问题，请稍候...</span>
        </div>

        <!-- 有结果时 -->
        <template v-else-if="hasData">
          <!-- 解释说明 -->
          <p class="explanation" v-if="response?.explanation">{{ response.explanation }}</p>

          <!-- 多标签页切换 -->
          <el-tabs v-model="activeTab" class="message-tabs" type="border-card">
            <!-- 图表视图 -->
            <el-tab-pane label="📊 图表" name="chart">
              <div class="tab-content">
                <ChartComponent
                  v-if="response?.columns?.length && response?.data?.length"
                  :columns="response.columns"
                  :data="response.data"
                />
                <el-empty v-else description="无数据可可视化" :image-size="60" />
              </div>
            </el-tab-pane>

            <!-- 表格视图 -->
            <el-tab-pane label="📋 数据" name="table">
              <div class="tab-content">
                <div v-if="response?.data?.length" class="table-wrapper">
                  <el-table
                    :data="response.data"
                    stripe
                    size="small"
                    max-height="300"
                    style="width: 100%"
                  >
                    <el-table-column
                      v-for="col in response.columns"
                      :key="col"
                      :prop="col"
                      :label="col"
                      min-width="120"
                      show-overflow-tooltip
                    />
                  </el-table>
                  <p class="row-count">共 {{ response.data.length }} 条记录</p>
                </div>
                <el-empty v-else description="查询无结果" :image-size="60" />
              </div>
            </el-tab-pane>

            <!-- SQL 视图 -->
            <el-tab-pane label="📝 SQL" name="sql">
              <div class="tab-content">
                <div class="sql-block">
                  <div class="sql-header">
                    <span>生成的 SQL</span>
                    <div class="sql-actions">
                      <el-button size="small" text @click="editSQL">
                        ✏️ 编辑
                      </el-button>
                      <el-button size="small" text @click="copySQL">
                        📋 复制
                      </el-button>
                    </div>
                  </div>
                  <pre class="sql-code"><code>{{ response?.sql || '' }}</code></pre>
                </div>
              </div>
            </el-tab-pane>
          </el-tabs>

          <!-- 底部操作按钮 -->
          <div class="message-actions">
            <el-button size="small" text @click="handleLike" :class="{ 'action-active': liked }">
              👍 有用
            </el-button>
            <el-button size="small" text @click="handleDislike" :class="{ 'action-active': disliked }">
              👎 没用
            </el-button>
            <el-button size="small" text @click="copySQL">
              📋 复制
            </el-button>
          </div>
        </template>

        <!-- 错误状态 -->
        <template v-else-if="hasError">
          <el-alert
            :title="response?.errorMessage || '查询失败'"
            type="error"
            :closable="false"
            show-icon
          />
        </template>
      </template>
    </div>
  </div>

  <!-- SQL 编辑弹窗 -->
  <el-dialog
    v-model="showSqlEditor"
    title="编辑 SQL"
    width="700px"
    :close-on-click-modal="false"
  >
    <div class="sql-editor">
      <el-input
        v-model="editingSql"
        type="textarea"
        :rows="10"
        placeholder="在此编辑 SQL..."
        class="sql-textarea"
      />
    </div>
    <template #footer>
      <el-button @click="showSqlEditor = false">取消</el-button>
      <el-button type="primary" @click="executeEditedSql" :loading="executingSql">
        执行查询
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import ChartComponent from './ChartComponent.vue'
import { executeSql } from '../api/queryApi'
import type { QueryResponse } from '../api/queryApi'
import type { ChatMessage } from '../api/chatApi'

interface Props {
  message: ChatMessage
  response?: QueryResponse
  loading?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  loading: false
})

const emit = defineEmits<{
  (e: 'like'): void
  (e: 'dislike'): void
  (e: 'sql-executed', response: QueryResponse): void
}>()

const activeTab = ref('table')
const liked = ref(false)
const disliked = ref(false)
const showSqlEditor = ref(false)
const editingSql = ref('')
const executingSql = ref(false)

const isUser = props.message.role === 'user'
const hasData = props.response?.success && (props.response.data?.length > 0 || props.response.sql)
const hasError = props.response && !props.response.success

function formatTime(timeStr?: string): string {
  if (!timeStr) return ''
  const date = new Date(timeStr)
  return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
}

async function copySQL() {
  if (!props.response?.sql) return
  try {
    await navigator.clipboard.writeText(props.response.sql)
    ElMessage.success('SQL 已复制')
  } catch {
    ElMessage.warning('复制失败')
  }
}

function editSQL() {
  editingSql.value = props.response?.sql || ''
  showSqlEditor.value = true
}

async function executeEditedSql() {
  if (!editingSql.value.trim()) {
    ElMessage.warning('SQL 不能为空')
    return
  }

  executingSql.value = true
  try {
    const response = await executeSql(editingSql.value)
    emit('sql-executed', response)
    showSqlEditor.value = false
    ElMessage.success('SQL 执行成功')
  } catch (error: any) {
    ElMessage.error(error.message || '执行失败')
  } finally {
    executingSql.value = false
  }
}

function handleLike() {
  liked.value = !liked.value
  if (liked.value) {
    disliked.value = false
  }
  emit('like')
}

function handleDislike() {
  disliked.value = !disliked.value
  if (disliked.value) {
    liked.value = false
  }
  emit('dislike')
}
</script>

<style scoped>
.message-card {
  padding: 16px 20px;
  background: #ffffff;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  transition: all 0.15s ease;
}

.message-card:hover {
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.message-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.avatar {
  width: 36px;
  height: 36px;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
}

.user-avatar {
  background: #f3f4f6;
}

.ai-avatar {
  background: #eff6ff;
}

.header-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.name {
  font-weight: 600;
  font-size: 14px;
  color: #1f2937;
}

.time {
  font-size: 12px;
  color: #9ca3af;
}

.message-content {
  padding-left: 48px;
}

.user-text {
  margin: 0;
  font-size: 15px;
  color: #1f2937;
  line-height: 1.6;
}

.loading-state {
  display: flex;
  align-items: center;
  gap: 12px;
  color: #374151;
  font-size: 14px;
  padding: 12px 0;
}

.loading-icon {
  display: flex;
  align-items: center;
  justify-content: center;
}

.spinner {
  width: 20px;
  height: 20px;
  border: 2px solid #e5e7eb;
  border-top: 2px solid #3b82f6;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.explanation {
  margin: 0 0 12px;
  font-size: 14px;
  color: #4b5563;
  line-height: 1.6;
}

.message-tabs {
  margin-bottom: 12px;
}

.message-tabs :deep(.el-tabs__header) {
  margin: 0 0 12px 0;
}

.message-tabs :deep(.el-tabs__content) {
  padding: 0;
}

.tab-content {
  min-height: 100px;
}

.table-wrapper {
  border-radius: 6px;
  overflow: hidden;
}

.row-count {
  margin: 8px 0 0;
  font-size: 12px;
  color: #9ca3af;
}

.sql-block {
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  overflow: hidden;
}

.sql-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 14px;
  background: #f9fafb;
  border-bottom: 1px solid #e5e7eb;
  color: #374151;
  font-size: 13px;
  font-weight: 500;
}

.sql-code {
  margin: 0;
  padding: 14px;
  overflow-x: auto;
  font-family: 'SF Mono', 'Monaco', 'Inconsolata', 'Roboto Mono', monospace;
  font-size: 13px;
  color: #374151;
  line-height: 1.6;
  background: #ffffff;
}

.message-actions {
  display: flex;
  gap: 4px;
  padding-top: 8px;
  border-top: 1px solid #f3f4f6;
}

.message-actions .el-button {
  padding: 6px 12px;
  color: #6b7280;
  border-radius: 4px;
}

.message-actions .el-button:hover {
  background: #f3f4f6;
  color: #1f2937;
}

.message-actions .el-button.action-active {
  background: #eff6ff;
  color: #3b82f6;
}

.sql-actions {
  display: flex;
  gap: 4px;
}

.sql-editor {
  margin-bottom: 16px;
}

.sql-textarea :deep(.el-textarea__inner) {
  font-family: 'SF Mono', 'Monaco', 'Inconsolata', 'Roboto Mono', monospace;
  font-size: 13px;
  line-height: 1.6;
}
</style>
