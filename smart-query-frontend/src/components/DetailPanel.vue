<template>
  <div class="detail-panel">
    <div class="panel-header">
      <span class="panel-title">详情</span>
      <div class="panel-actions">
        <el-button size="small" circle text @click="$emit('close')">
          <span style="font-size: 16px;">✕</span>
        </el-button>
      </div>
    </div>

    <div v-if="!selectedMessage" class="empty-state">
      <div class="empty-content">
        <span class="empty-icon">💡</span>
        <p class="empty-text">选择一条消息查看详情</p>
      </div>
    </div>

    <div v-else class="panel-content">
      <!-- 标签页 -->
      <el-tabs v-model="activeTab" class="detail-tabs" type="border-card">
        <!-- SQL 详情 -->
        <el-tab-pane label="📝 SQL" name="sql">
          <div class="tab-content">
            <div class="info-item">
              <span class="info-label">SQL 语句</span>
              <div class="sql-preview">
                <pre><code>{{ selectedMessage.response?.sql || '-' }}</code></pre>
              </div>
            </div>
            <div class="info-item">
              <span class="info-label">状态</span>
              <el-tag :type="selectedMessage.response?.success ? 'success' : 'danger'" size="small">
                {{ selectedMessage.response?.success ? '成功' : '失败' }}
              </el-tag>
            </div>
          </div>
        </el-tab-pane>

        <!-- 执行计划 -->
        <el-tab-pane label="⚙️ 执行计划" name="plan">
          <div class="tab-content">
            <div v-if="hasExecutionPlan" class="execution-plan">
              <el-table
                :data="selectedMessage.response?.executionPlan"
                border
                size="small"
                max-height="400"
                style="width: 100%"
              >
                <el-table-column
                  v-for="(value, key) in selectedMessage.response?.executionPlan[0]"
                  :key="key"
                  :prop="key"
                  :label="key"
                  min-width="100"
                  show-overflow-tooltip
                />
              </el-table>
            </div>
            <el-empty v-else description="暂无执行计划" :image-size="60" />
          </div>
        </el-tab-pane>

        <!-- 元数据 -->
        <el-tab-pane label="📊 元数据" name="metadata">
          <div class="tab-content">
            <div class="info-item">
              <span class="info-label">查询时间</span>
              <span class="info-value">{{ selectedMessage.createdAt }}</span>
            </div>
            <div v-if="selectedMessage.response" class="info-item">
              <span class="info-label">返回条数</span>
              <span class="info-value">{{ selectedMessage.response.data?.length || 0 }} 条</span>
            </div>
            <div v-if="selectedMessage.response?.columns" class="info-item">
              <span class="info-label">返回字段</span>
              <div class="columns-list">
                <el-tag
                  v-for="col in selectedMessage.response.columns"
                  :key="col"
                  size="small"
                  type="info"
                  style="margin-right: 4px; margin-bottom: 4px"
                >
                  {{ col }}
                </el-tag>
              </div>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>

      <!-- 快捷操作 -->
      <div class="quick-actions">
        <el-button type="primary" size="small" @click="handleCopySQL" style="width: 100%;">
          📋 复制 SQL
        </el-button>
        <el-button size="small" @click="handleRequery" style="width: 100%;">
          🔄 重新查询
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import type { QueryResponse } from '../api/queryApi'
import type { ChatMessage } from '../api/chatApi'

interface MessageWithResponse extends ChatMessage {
  response?: QueryResponse
}

const props = defineProps<{
  selectedMessage?: MessageWithResponse
}>()

const emit = defineEmits<{
  (e: 'close'): void
  (e: 'copy-sql', sql: string): void
  (e: 'requery'): void
}>()

const activeTab = ref('sql')

const hasExecutionPlan = computed(() => {
  return props.selectedMessage?.response?.executionPlan && 
         props.selectedMessage.response.executionPlan.length > 0
})

function handleCopySQL() {
  if (props.selectedMessage?.response?.sql) {
    emit('copy-sql', props.selectedMessage.response.sql)
  }
}

function handleRequery() {
  emit('requery')
}
</script>

<style scoped>
.detail-panel {
  width: 340px;
  height: 100vh;
  background: #ffffff;
  border-left: 1px solid #e5e7eb;
  display: flex;
  flex-direction: column;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid #e5e7eb;
  background: #f9fafb;
}

.panel-title {
  font-weight: 600;
  font-size: 14px;
  color: #1f2937;
}

.empty-state {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}

.empty-content {
  text-align: center;
  color: #9ca3af;
}

.empty-icon {
  font-size: 48px;
  display: block;
  margin-bottom: 12px;
}

.empty-text {
  margin: 0;
  font-size: 14px;
}

.panel-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.detail-tabs {
  flex: 1;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.detail-tabs :deep(.el-tabs__content) {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
}

.detail-tabs :deep(.el-tab-pane) {
  height: 100%;
}

.tab-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.info-label {
  font-size: 12px;
  font-weight: 600;
  color: #6b7280;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.info-value {
  font-size: 14px;
  color: #1f2937;
}

.sql-preview {
  background: #f9fafb;
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  padding: 12px;
  overflow-x: auto;
}

.sql-preview pre {
  margin: 0;
  font-family: 'SF Mono', 'Monaco', 'Inconsolata', 'Roboto Mono', monospace;
  font-size: 12px;
  color: #374151;
  line-height: 1.5;
  white-space: pre-wrap;
  word-break: break-all;
}

.columns-list {
  display: flex;
  flex-wrap: wrap;
}

.execution-plan {
  border-radius: 6px;
  overflow: hidden;
}

.quick-actions {
  padding: 16px;
  border-top: 1px solid #e5e7eb;
  display: flex;
  flex-direction: column;
  gap: 8px;
}
</style>
