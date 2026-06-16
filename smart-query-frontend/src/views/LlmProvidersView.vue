<template>
  <div class="llm-providers-container">
    <header class="page-header">
      <h2>模型管理</h2>
      <div class="header-info">
        <el-tag type="success" v-if="activeProvider">当前活跃: {{ activeProvider }}</el-tag>
      </div>
    </header>

    <div class="providers-grid">
      <div 
        v-for="provider in providers" 
        :key="provider.name" 
        class="provider-card"
        :class="{ 'active': provider.isActive }"
      >
        <div class="provider-header">
          <div class="provider-info">
            <h3>{{ provider.displayName }}</h3>
            <el-tag :type="provider.isActive ? 'success' : 'info'" size="small">
              {{ provider.isActive ? '活跃' : '未启用' }}
            </el-tag>
          </div>
          <div class="provider-status">
            <el-tag :type="provider.available ? 'success' : 'danger'" size="small">
              {{ provider.available ? '可用' : '不可用' }}
            </el-tag>
          </div>
        </div>
        
        <div class="provider-details">
          <div class="detail-item">
            <span class="label">模型:</span>
            <span class="value">{{ provider.modelName }}</span>
          </div>
          <div class="detail-item">
            <span class="label">标识:</span>
            <span class="value">{{ provider.name }}</span>
          </div>
        </div>

        <div class="provider-actions">
          <el-button 
            v-if="!provider.isActive" 
            type="primary" 
            size="small" 
            @click="handleActivate(provider.name)"
            :disabled="!provider.available"
          >
            启用
          </el-button>
          <el-button 
            v-if="provider.isActive" 
            type="success" 
            size="small" 
            disabled
          >
            当前使用中
          </el-button>
          <el-button size="small" @click="handleConfigure(provider)">
            配置
          </el-button>
        </div>
      </div>
    </div>

    <!-- 配置对话框 -->
    <el-dialog 
      v-model="configDialogVisible" 
      :title="'配置 ' + (configForm.displayName || '')"
      width="500px"
    >
      <el-form :model="configForm" label-width="100px">
        <el-form-item label="显示名称">
          <el-input v-model="configForm.displayName" />
        </el-form-item>
        <el-form-item label="模型名称">
          <el-input v-model="configForm.modelName" />
        </el-form-item>
        <el-form-item label="API密钥">
          <el-input v-model="configForm.apiKey" type="password" show-password placeholder="留空则不修改" />
        </el-form-item>
        <el-form-item label="API地址">
          <el-input v-model="configForm.baseUrl" placeholder="留空则使用默认地址" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="configDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveConfig" :loading="saving">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { 
  getLlmProviders, 
  activateProvider, 
  updateProvider, 
  type LlmProvider, 
  type LlmProviderConfig 
} from '../api/llmProviderApi'

const providers = ref<LlmProvider[]>([])
const activeProvider = ref<string>('')
const loading = ref(false)
const saving = ref(false)

const configDialogVisible = ref(false)
const configForm = ref<LlmProviderConfig>({
  name: '',
  displayName: '',
  modelName: '',
  apiKey: '',
  baseUrl: '',
  isActive: false
})

onMounted(() => {
  loadProviders()
})

async function loadProviders() {
  loading.value = true
  try {
    providers.value = await getLlmProviders()
    const active = providers.value.find(p => p.isActive)
    activeProvider.value = active?.displayName || ''
  } catch (error) {
    ElMessage.error('加载模型列表失败')
  } finally {
    loading.value = false
  }
}

async function handleActivate(name: string) {
  try {
    await activateProvider(name)
    ElMessage.success('已切换活跃模型')
    await loadProviders()
  } catch (error) {
    ElMessage.error('切换失败')
  }
}

function handleConfigure(provider: LlmProvider) {
  configForm.value = {
    name: provider.name,
    displayName: provider.displayName,
    modelName: provider.modelName,
    apiKey: '',
    baseUrl: '',
    isActive: provider.isActive
  }
  configDialogVisible.value = true
}

async function handleSaveConfig() {
  saving.value = true
  try {
    await updateProvider(configForm.value.name, configForm.value)
    ElMessage.success('配置已保存')
    configDialogVisible.value = false
    await loadProviders()
  } catch (error) {
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}
</script>

<style scoped>
.llm-providers-container {
  padding: 24px;
  max-width: 1200px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.page-header h2 {
  margin: 0;
  font-size: 24px;
  color: #1f2937;
}

.header-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.providers-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 20px;
}

.provider-card {
  background: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  transition: all 0.2s;
  border: 2px solid transparent;
}

.provider-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.provider-card.active {
  border-color: #10b981;
  background: #f0fdf4;
}

.provider-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
}

.provider-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.provider-info h3 {
  margin: 0;
  font-size: 18px;
  color: #1f2937;
}

.provider-details {
  margin-bottom: 16px;
}

.detail-item {
  display: flex;
  margin-bottom: 8px;
}

.detail-item .label {
  width: 60px;
  color: #6b7280;
  font-size: 14px;
}

.detail-item .value {
  color: #1f2937;
  font-size: 14px;
  font-weight: 500;
}

.provider-actions {
  display: flex;
  gap: 8px;
  padding-top: 16px;
  border-top: 1px solid #e5e7eb;
}
</style>