<template>
  <div class="metadata-container">
    <header class="page-header">
      <h2>元数据管理</h2>
      <el-button type="primary" @click="handleSync" :loading="syncing">
        同步表结构
      </el-button>
    </header>

    <el-tabs v-model="activeTab" class="metadata-tabs">
      <!-- Tab 1: 表结构 -->
      <el-tab-pane label="表结构" name="tables">
        <!-- 标签筛选栏 -->
        <div class="filter-bar">
          <el-tag 
            v-for="tag in tags" 
            :key="tag.id"
            :type="filterTagId === tag.id ? '' : 'info'"
            @click="filterTagId = filterTagId === tag.id ? 0 : tag.id"
            style="cursor: pointer; margin-right: 8px;"
          >
            {{ tag.name }}
          </el-tag>
          <el-button type="text" @click="filterTagId = 0" v-if="filterTagId !== 0">清除筛选</el-button>
        </div>

        <div v-for="table in filteredTables" :key="table.id" class="table-card">
          <div class="table-header">
            <div class="table-info">
              <strong>{{ table.tableName }}</strong>
              <el-tag v-if="table.displayName !== table.tableName" size="small" type="info">
                {{ table.displayName }}
              </el-tag>
            </div>
            <div class="table-actions">
              <el-button size="small" text type="primary" @click="editTable(table)">编辑</el-button>
              <el-button size="small" text @click="editTableTags(table)">标签</el-button>
            </div>
          </div>
          <div class="table-tags">
            <el-tag v-for="tag in table.tags" :key="tag.id" size="small" :color="tag.color" style="margin-right: 4px;">
              {{ tag.name }}
            </el-tag>
          </div>
          <p v-if="table.description" class="table-desc">{{ table.description }}</p>

          <el-table :data="table.columns" size="small" stripe border style="width: 100%">
            <el-table-column prop="columnName" label="字段名" width="160" />
            <el-table-column prop="displayName" label="业务名称" width="140">
              <template #default="{ row }">
                <el-input v-model="row.displayName" size="small" placeholder="业务名称"
                  @blur="saveColumn(row)" />
              </template>
            </el-table-column>
            <el-table-column prop="dataType" label="类型" width="130" />
            <el-table-column label="角色" width="140">
              <template #default="{ row }">
                <el-select v-model="row.role" size="small" @change="saveColumn(row)" style="width: 100%">
                  <el-option label="无" value="none" />
                  <el-option label="维度" value="dimension" />
                  <el-option label="指标" value="metric" />
                </el-select>
              </template>
            </el-table-column>
            <el-table-column label="描述" min-width="200">
              <template #default="{ row }">
                <el-input v-model="row.description" size="small" placeholder="字段描述"
                  @blur="saveColumn(row)" />
              </template>
            </el-table-column>
            <el-table-column label="枚举值" width="180">
              <template #default="{ row }">
                <el-input v-model="row.enumValues" size="small" placeholder="逗号分隔"
                  @blur="saveColumn(row)" />
              </template>
            </el-table-column>
          </el-table>
        </div>
        <el-empty v-if="filteredTables.length === 0 && !syncing" description="暂无表数据，请先点击同步表结构" />
      </el-tab-pane>

      <!-- Tab 2: 指标 -->
      <el-tab-pane label="指标管理" name="metrics">
        <div class="tab-toolbar">
          <el-button type="primary" size="small" @click="showMetricDialog()">新增指标</el-button>
        </div>
        <el-table :data="metrics" stripe border size="small">
          <el-table-column prop="name" label="名称" width="140" />
          <el-table-column prop="description" label="描述" min-width="200" />
          <el-table-column prop="expression" label="表达式" width="200" />
          <el-table-column prop="aggFunction" label="聚合方式" width="100" />
          <el-table-column prop="unit" label="单位" width="80" />
          <el-table-column label="操作" width="80" fixed="right">
            <template #default="{ row }">
              <el-popconfirm title="确认删除此指标？" @confirm="deleteMetric(row.id)">
                <template #reference>
                  <el-button type="danger" size="small" text>删除</el-button>
                </template>
              </el-popconfirm>
            </template>
          </el-table-column>
        </el-table>
        <el-empty v-if="metrics.length === 0" description="暂无指标定义" />
      </el-tab-pane>

      <!-- Tab 3: 维度 -->
      <el-tab-pane label="维度管理" name="dimensions">
        <div class="tab-toolbar">
          <el-button type="primary" size="small" @click="showDimensionDialog()">新增维度</el-button>
        </div>
        <el-table :data="dimensions" stripe border size="small">
          <el-table-column prop="name" label="名称" width="140" />
          <el-table-column prop="description" label="描述" min-width="200" />
          <el-table-column prop="columnName" label="关联字段" width="150" />
          <el-table-column prop="enumValues" label="枚举值" min-width="200" />
          <el-table-column label="操作" width="80" fixed="right">
            <template #default="{ row }">
              <el-popconfirm title="确认删除此维度？" @confirm="deleteDimension(row.id)">
                <template #reference>
                  <el-button type="danger" size="small" text>删除</el-button>
                </template>
              </el-popconfirm>
            </template>
          </el-table-column>
        </el-table>
        <el-empty v-if="dimensions.length === 0" description="暂无维度定义" />
      </el-tab-pane>

      <!-- Tab 4: 标签管理 -->
      <el-tab-pane label="标签管理" name="tags">
        <div class="tab-toolbar">
          <el-button type="primary" size="small" @click="showTagDialog()">新增标签</el-button>
        </div>
        <el-table :data="tags" stripe border size="small">
          <el-table-column prop="name" label="名称" width="140" />
          <el-table-column label="颜色" width="100">
            <template #default="{ row }">
              <div class="color-preview" :style="{ backgroundColor: row.color }"></div>
            </template>
          </el-table-column>
          <el-table-column prop="description" label="描述" min-width="200" />
          <el-table-column label="操作" width="150" fixed="right">
            <template #default="{ row }">
              <el-button type="primary" size="small" text @click="editTag(row)">编辑</el-button>
              <el-popconfirm title="确认删除此标签？" @confirm="handleDeleteTag(row.id)">
                <template #reference>
                  <el-button type="danger" size="small" text>删除</el-button>
                </template>
              </el-popconfirm>
            </template>
          </el-table-column>
        </el-table>
        <el-empty v-if="tags.length === 0" description="暂无标签定义" />
      </el-tab-pane>
    </el-tabs>

    <!-- 编辑表弹窗 -->
    <el-dialog v-model="tableDialogVisible" title="编辑表信息" width="480px">
      <el-form :model="editingTable" label-width="80px">
        <el-form-item label="表名">
          <el-input :model-value="editingTable.tableName" disabled />
        </el-form-item>
        <el-form-item label="业务名称">
          <el-input v-model="editingTable.displayName" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="editingTable.description" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="tableDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveTable">保存</el-button>
      </template>
    </el-dialog>

    <!-- 编辑表标签弹窗 -->
    <el-dialog v-model="tableTagDialogVisible" title="设置表标签" width="400px">
      <el-checkbox-group v-model="selectedTagIds">
        <el-checkbox v-for="tag in tags" :key="tag.id" :label="tag.id" style="display: block; margin-bottom: 8px;">
          <el-tag :color="tag.color">{{ tag.name }}</el-tag>
          <span v-if="tag.description" style="margin-left: 8px; color: #999;">{{ tag.description }}</span>
        </el-checkbox>
      </el-checkbox-group>
      <template #footer>
        <el-button @click="tableTagDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveTableTags">保存</el-button>
      </template>
    </el-dialog>

    <!-- 标签编辑弹窗 -->
    <el-dialog v-model="tagDialogVisible" :title="editingTag.id ? '编辑标签' : '新增标签'" width="400px">
      <el-form :model="editingTag" label-width="80px">
        <el-form-item label="名称" required>
          <el-input v-model="editingTag.name" placeholder="标签名称" />
        </el-form-item>
        <el-form-item label="颜色">
          <el-color-picker v-model="editingTag.color" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="editingTag.description" type="textarea" :rows="2" placeholder="标签描述" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="tagDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveTag">保存</el-button>
      </template>
    </el-dialog>

    <!-- 新增指标弹窗 -->
    <el-dialog v-model="metricDialogVisible" title="新增指标" width="520px">
      <el-form :model="newMetric" label-width="80px">
        <el-form-item label="名称" required>
          <el-input v-model="newMetric.name" placeholder="如：销售额" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="newMetric.description" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="表达式" required>
          <el-input v-model="newMetric.expression" placeholder="如：sale_amount * sale_quantity" />
        </el-form-item>
        <el-form-item label="聚合方式" required>
          <el-select v-model="newMetric.aggFunction" style="width: 100%">
            <el-option v-for="f in aggFunctions" :key="f" :label="f" :value="f" />
          </el-select>
        </el-form-item>
        <el-form-item label="单位">
          <el-input v-model="newMetric.unit" placeholder="如：元、件、笔" />
        </el-form-item>
        <el-form-item label="所属表" required>
          <el-select v-model="newMetric.tableId" style="width: 100%">
            <el-option v-for="t in tables" :key="t.id" :label="t.tableName" :value="t.id" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="metricDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveMetric">保存</el-button>
      </template>
    </el-dialog>

    <!-- 新增维度弹窗 -->
    <el-dialog v-model="dimensionDialogVisible" title="新增维度" width="520px">
      <el-form :model="newDimension" label-width="80px">
        <el-form-item label="名称" required>
          <el-input v-model="newDimension.name" placeholder="如：产品类别" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="newDimension.description" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="关联字段" required>
          <el-select v-model="newDimension.columnName" filterable style="width: 100%">
            <el-option-group v-for="t in tables" :key="t.id" :label="t.tableName">
              <el-option v-for="c in t.columns" :key="c.id" :label="`${c.columnName} (${c.displayName})`"
                :value="c.columnName" />
            </el-option-group>
          </el-select>
        </el-form-item>
        <el-form-item label="枚举值">
          <el-input v-model="newDimension.enumValues" placeholder="逗号分隔，如：手机,电脑,平板" />
        </el-form-item>
        <el-form-item label="所属表" required>
          <el-select v-model="newDimension.tableId" style="width: 100%">
            <el-option v-for="t in tables" :key="t.id" :label="t.tableName" :value="t.id" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dimensionDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveDimension">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  getTables, updateTable, updateColumn, syncMetadata,
  getMetrics, addMetric, removeMetric,
  getDimensions, addDimension, removeDimension,
  type TableMetadata, type ColumnMetadata,
  type MetricDefinition, type DimensionDefinition
} from '../api/metadataApi'
import {
  getTags, createTag, updateTag, deleteTag, setTagsForTable, getTablesByTag,
  type TagDefinition
} from '../api/tagApi'

const activeTab = ref('tables')
const syncing = ref(false)
const tables = ref<TableMetadata[]>([])
const metrics = ref<MetricDefinition[]>([])
const dimensions = ref<DimensionDefinition[]>([])
const tags = ref<TagDefinition[]>([])
const filterTagId = ref(0)

const filteredTables = computed(() => {
  if (filterTagId.value === 0) return tables.value
  return tables.value.filter(t => t.tags?.some(tag => tag.id === filterTagId.value))
})

const tableDialogVisible = ref(false)
const tableTagDialogVisible = ref(false)
const tagDialogVisible = ref(false)
const metricDialogVisible = ref(false)
const dimensionDialogVisible = ref(false)

const editingTable = ref<Partial<TableMetadata>>({})
const editingTag = ref<Partial<TagDefinition>>({})
const newMetric = ref<Partial<MetricDefinition>>({})
const newDimension = ref<Partial<DimensionDefinition>>({})
const selectedTagIds = ref<number[]>([])
const currentTableForTags = ref<TableMetadata | null>(null)

const aggFunctions = ['SUM', 'AVG', 'COUNT', 'MAX', 'MIN']

async function loadData() {
  tables.value = await getTables()
  metrics.value = await getMetrics()
  dimensions.value = await getDimensions()
  tags.value = await getTags()
}

onMounted(() => loadData())

async function handleSync() {
  syncing.value = true
  try {
    const result = await syncMetadata()
    ElMessage.success(`同步完成，同步了 ${result.syncedTables} 张表`)
    await loadData()
  } catch (e) {
    ElMessage.error('同步失败')
  } finally {
    syncing.value = false
  }
}

async function saveColumn(col: ColumnMetadata) {
  try {
    await updateColumn(col.id, col)
  } catch (e) {
    ElMessage.error('保存字段失败')
  }
}

function editTable(table: TableMetadata) {
  editingTable.value = { ...table }
  tableDialogVisible.value = true
}

async function saveTable() {
  if (!editingTable.value.id) return
  try {
    await updateTable(editingTable.value.id, editingTable.value)
    ElMessage.success('保存成功')
    tableDialogVisible.value = false
    await loadData()
  } catch (e) {
    ElMessage.error('保存失败')
  }
}

async function editTableTags(table: TableMetadata) {
  currentTableForTags.value = table
  selectedTagIds.value = table.tags?.map(t => t.id) || []
  tableTagDialogVisible.value = true
}

async function saveTableTags() {
  if (!currentTableForTags.value) return
  try {
    await setTagsForTable(currentTableForTags.value.id, selectedTagIds.value)
    ElMessage.success('标签保存成功')
    tableTagDialogVisible.value = false
    await loadData()
  } catch (e) {
    ElMessage.error('标签保存失败')
  }
}

function showTagDialog(tag?: TagDefinition) {
  if (tag) {
    editingTag.value = { ...tag }
  } else {
    editingTag.value = { name: '', color: '#3b82f6', description: '' }
  }
  tagDialogVisible.value = true
}

async function saveTag() {
  try {
    if (editingTag.value.id) {
      await updateTag(editingTag.value.id, editingTag.value)
    } else {
      await createTag(editingTag.value)
    }
    ElMessage.success('保存成功')
    tagDialogVisible.value = false
    await loadData()
  } catch (e) {
    ElMessage.error('保存失败')
  }
}

function editTag(tag: TagDefinition) {
  showTagDialog(tag)
}

async function handleDeleteTag(id: number) {
  try {
    await deleteTag(id)
    ElMessage.success('删除成功')
    await loadData()
  } catch (e) {
    ElMessage.error('删除失败')
  }
}

function showMetricDialog() {
  newMetric.value = { name: '', description: '', expression: '', aggFunction: 'SUM', unit: '', tableId: tables.value[0]?.id }
  metricDialogVisible.value = true
}

async function saveMetric() {
  try {
    await addMetric(newMetric.value as any)
    ElMessage.success('保存成功')
    metricDialogVisible.value = false
    await loadData()
  } catch (e) {
    ElMessage.error('保存失败')
  }
}

async function deleteMetric(id: number) {
  try {
    await removeMetric(id)
    ElMessage.success('删除成功')
    await loadData()
  } catch (e) {
    ElMessage.error('删除失败')
  }
}

function showDimensionDialog() {
  newDimension.value = { name: '', description: '', columnName: '', enumValues: '', tableId: tables.value[0]?.id }
  dimensionDialogVisible.value = true
}

async function saveDimension() {
  try {
    await addDimension(newDimension.value as any)
    ElMessage.success('保存成功')
    dimensionDialogVisible.value = false
    await loadData()
  } catch (e) {
    ElMessage.error('保存失败')
  }
}

async function deleteDimension(id: number) {
  try {
    await removeDimension(id)
    ElMessage.success('删除成功')
    await loadData()
  } catch (e) {
    ElMessage.error('删除失败')
  }
}
</script>

<style scoped>
.metadata-container {
  padding: 20px;
  height: 100%;
  overflow-y: auto;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.table-card {
  background: white;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 16px;
  border: 1px solid #e8e8e8;
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.table-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.table-actions {
  display: flex;
  gap: 8px;
}

.table-tags {
  margin-bottom: 8px;
}

.table-desc {
  color: #666;
  margin-bottom: 12px;
}

.filter-bar {
  margin-bottom: 16px;
  padding: 12px;
  background: white;
  border-radius: 8px;
  border: 1px solid #e8e8e8;
}

.tab-toolbar {
  margin-bottom: 12px;
}

.color-preview {
  width: 60px;
  height: 24px;
  border-radius: 4px;
  border: 1px solid #ddd;
}
</style>
