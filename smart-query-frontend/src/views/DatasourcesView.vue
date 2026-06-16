<template>
  <div class="datasources-container">
    <header class="page-header">
      <h2>数据源管理</h2>
      <el-button type="primary" @click="handleAdd">
        <el-icon><Plus /></el-icon> 添加数据源
      </el-button>
    </header>

    <div class="datasource-list">
      <div v-if="datasources.length === 0" class="empty-state">
        <el-empty description="暂无数据源">
          <el-button type="primary" @click="handleAdd">添加第一个数据源</el-button>
        </el-empty>
      </div>

      <div v-for="ds in datasources" :key="ds.id" class="datasource-card">
        <div class="card-header">
          <div class="ds-info">
            <h3>{{ ds.name }}</h3>
            <el-tag :type="ds.status === 'active' ? 'success' : 'info'" size="small">
              {{ ds.status === 'active' ? '已启用' : '已停用' }}
            </el-tag>
            <el-tag size="small" type="info">{{ ds.dbType }}</el-tag>
          </div>
          <div class="ds-actions">
            <el-button size="small" text type="primary" @click="handleEdit(ds)">编辑</el-button>
            <el-button size="small" text type="danger" @click="handleDelete(ds)">删除</el-button>
          </div>
        </div>
        <div class="card-body">
          <div class="detail-row">
            <span class="label">JDBC URL:</span>
            <span class="value mono">{{ ds.jdbcUrl }}</span>
          </div>
          <div class="detail-row" v-if="ds.username">
            <span class="label">用户名:</span>
            <span class="value">{{ ds.username }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- Add/Edit Dialog -->
    <el-dialog 
      v-model="dialogVisible" 
      :title="editingDs ? '编辑数据源' : '添加数据源'"
      width="550px"
    >
      <el-form :model="form" label-width="90px" :rules="rules" ref="formRef">
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" placeholder="如: 生产数据库" />
        </el-form-item>
        <el-form-item label="数据库类型" prop="dbType">
          <el-select v-model="form.dbType" placeholder="选择类型">
            <el-option label="H2" value="H2" />
            <el-option label="MySQL" value="MYSQL" />
            <el-option label="PostgreSQL" value="POSTGRESQL" />
            <el-option label="ClickHouse" value="CLICKHOUSE" />
          </el-select>
        </el-form-item>
        <el-form-item label="JDBC URL" prop="jdbcUrl">
          <el-input v-model="form.jdbcUrl" placeholder="jdbc:h2:mem:testdb" />
        </el-form-item>
        <el-form-item label="用户名">
          <el-input v-model="form.username" placeholder="可选" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" show-password placeholder="可选" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.status" active-value="active" inactive-value="inactive" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="handleTestConnection" :loading="testing">测试连接</el-button>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave" :loading="saving">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { 
  getDatasources, createDatasource, updateDatasource, deleteDatasource, testConnection,
  type DatasourceConfig 
} from '../api/datasourceApi'

const datasources = ref<DatasourceConfig[]>([])
const loading = ref(false)
const saving = ref(false)
const testing = ref(false)
const dialogVisible = ref(false)
const editingDs = ref<DatasourceConfig | null>(null)
const formRef = ref()

const form = reactive<DatasourceConfig>({
  name: '',
  dbType: 'H2',
  jdbcUrl: '',
  username: '',
  password: '',
  status: 'active'
})

const rules = {
  name: [{ required: true, message: '请输入名称', trigger: 'blur' }],
  dbType: [{ required: true, message: '请选择数据库类型', trigger: 'change' }],
  jdbcUrl: [{ required: true, message: '请输入JDBC URL', trigger: 'blur' }]
}

onMounted(() => {
  loadDatasources()
})

async function loadDatasources() {
  loading.value = true
  try {
    datasources.value = await getDatasources()
  } catch (error) {
    ElMessage.error('加载数据源列表失败')
  } finally {
    loading.value = false
  }
}

function handleAdd() {
  editingDs.value = null
  Object.assign(form, {
    name: '',
    dbType: 'H2',
    jdbcUrl: '',
    username: '',
    password: '',
    status: 'active'
  })
  dialogVisible.value = true
}

function handleEdit(ds: DatasourceConfig) {
  editingDs.value = ds
  Object.assign(form, {
    ...ds,
    password: ''
  })
  dialogVisible.value = true
}

async function handleDelete(ds: DatasourceConfig) {
  try {
    await ElMessageBox.confirm(`确定要删除数据源"${ds.name}"吗？`, '提示', {
      type: 'warning'
    })
    await deleteDatasource(ds.id!)
    ElMessage.success('已删除')
    await loadDatasources()
  } catch {
    // cancelled
  }
}

async function handleTestConnection() {
  testing.value = true
  try {
    const result = await testConnection(form)
    if (result.success) {
      ElMessage.success(`连接成功！耗时 ${result.elapsedMs}ms`)
    } else {
      ElMessage.error(result.message)
    }
  } catch (error: any) {
    ElMessage.error('测试连接失败: ' + (error.message || '未知错误'))
  } finally {
    testing.value = false
  }
}

async function handleSave() {
  try {
    await formRef.value?.validate()
  } catch {
    return
  }
  
  saving.value = true
  try {
    if (editingDs.value?.id) {
      await updateDatasource(editingDs.value.id, form)
    } else {
      await createDatasource(form)
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    await loadDatasources()
  } catch (error) {
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}
</script>

<style scoped>
.datasources-container {
  padding: 24px;
  max-width: 1000px;
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

.empty-state {
  padding: 60px 0;
}

.datasource-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.datasource-card {
  background: white;
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid #f3f4f6;
}

.ds-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.ds-info h3 {
  margin: 0;
  font-size: 16px;
  color: #1f2937;
}

.ds-actions {
  display: flex;
  gap: 4px;
}

.card-body {
  padding: 16px 20px;
}

.detail-row {
  display: flex;
  margin-bottom: 8px;
}

.detail-row .label {
  width: 80px;
  color: #6b7280;
  font-size: 13px;
}

.detail-row .value {
  color: #374151;
  font-size: 13px;
}

.detail-row .value.mono {
  font-family: monospace;
  word-break: break-all;
}
</style>