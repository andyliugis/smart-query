<template>
  <div class="app-layout">
    <!-- 只有在非 chat 和 login 路由时显示左侧导航栏 -->
    <aside v-if="showSidebar" class="sidebar">
      <div class="logo">
        <span class="logo-icon">🔍</span>
        <span class="logo-text">智能问数</span>
      </div>
      <el-menu
        :default-active="activeMenu"
        router
        class="sidebar-menu"
      >
        <el-menu-item index="/chat">
          <el-icon><ChatDotRound /></el-icon>
          <span>智能问答</span>
        </el-menu-item>
        <el-menu-item index="/metadata">
          <el-icon><Setting /></el-icon>
          <span>元数据管理</span>
        </el-menu-item>
        <el-menu-item index="/llm-providers">
          <el-icon><DataLine /></el-icon>
          <span>模型管理</span>
        </el-menu-item>
        <el-menu-item index="/datasources">
          <el-icon><Coin /></el-icon>
          <span>数据源管理</span>
        </el-menu-item>
      </el-menu>

      <!-- 用户信息 -->
      <div class="user-info">
        <div class="user-avatar">
          <el-avatar :size="36" :icon="UserFilled" />
        </div>
        <div class="user-details">
          <div class="user-name">{{ userInfo?.nickname || userInfo?.username }}</div>
          <div class="user-role">{{ userInfo?.username }}</div>
        </div>
        <el-button type="link" @click="handleLogout" style="color: #909399">
          <el-icon><SwitchButton /></el-icon>
        </el-button>
      </div>
    </aside>

    <!-- 右侧内容区 -->
    <main :class="['content', { 'full-width': !showSidebar }]">
      <router-view />
    </main>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ChatDotRound, Setting, UserFilled, SwitchButton, DataLine, Coin } from '@element-plus/icons-vue'
import { getUserInfo, clearAuthData, isAuthenticated } from './api/authApi'

const route = useRoute()
const router = useRouter()
const activeMenu = computed(() => route.path)
const userInfo = ref(getUserInfo())

// 判断是否显示侧边栏（登录页和聊天页不显示）
const showSidebar = computed(() => {
  const path = route.path
  return path !== '/login' && path !== '/chat' && isAuthenticated()
})

onMounted(() => {
  if (!isAuthenticated()) {
    router.push('/login')
  }
})

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

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html, body, #app {
  height: 100%;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
  background-color: #f5f7fa;
}
</style>

<style scoped>
.app-layout {
  display: flex;
  height: 100vh;
}

.sidebar {
  width: 200px;
  background: #fff;
  border-right: 1px solid #e8e8e8;
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
}

.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  border-bottom: 1px solid #e8e8e8;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
}

.logo-icon {
  font-size: 24px;
}

.logo-text {
  font-size: 18px;
  font-weight: 600;
}

.sidebar-menu {
  border-right: none;
  flex: 1;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  border-top: 1px solid #e8e8e8;
  background: #fafafa;
}

.user-details {
  flex: 1;
  min-width: 0;
}

.user-name {
  font-size: 14px;
  font-weight: 500;
  color: #333;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.user-role {
  font-size: 12px;
  color: #909399;
}

.content {
  flex: 1;
  overflow: hidden;
  background: #f5f7fa;
}

.content.full-width {
  flex: 1;
  width: 100%;
}
</style>
