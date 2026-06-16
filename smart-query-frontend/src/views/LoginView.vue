<template>
  <div class="login-container">
    <div class="login-card">
      <div class="login-header">
        <span class="logo-icon">🔍</span>
        <h1>智能问数</h1>
        <p>用自然语言查询数据，AI 帮你写 SQL</p>
      </div>

      <el-tabs v-model="activeTab" class="login-tabs">
        <el-tab-pane label="登录" name="login">
          <el-form :model="loginForm" label-position="top" @submit.prevent="handleLogin">
            <el-form-item label="用户名">
              <el-input
                v-model="loginForm.username"
                placeholder="请输入用户名"
                size="large"
                clearable
              />
            </el-form-item>
            <el-form-item label="密码">
              <el-input
                v-model="loginForm.password"
                type="password"
                placeholder="请输入密码"
                size="large"
                show-password
                @keyup.enter="handleLogin"
              />
            </el-form-item>
            <el-form-item>
              <el-button
                type="primary"
                size="large"
                style="width: 100%"
                :loading="loading"
                @click="handleLogin"
              >
                登录
              </el-button>
            </el-form-item>
          </el-form>
          <div class="demo-tip">
            <el-tag type="info" size="small">演示账号：admin / admin123</el-tag>
          </div>
        </el-tab-pane>

        <el-tab-pane label="注册" name="register">
          <el-form :model="registerForm" label-position="top" @submit.prevent="handleRegister">
            <el-form-item label="用户名">
              <el-input
                v-model="registerForm.username"
                placeholder="请输入用户名"
                size="large"
                clearable
              />
            </el-form-item>
            <el-form-item label="密码">
              <el-input
                v-model="registerForm.password"
                type="password"
                placeholder="请输入密码"
                size="large"
                show-password
              />
            </el-form-item>
            <el-form-item label="昵称">
              <el-input
                v-model="registerForm.nickname"
                placeholder="请输入昵称（可选）"
                size="large"
                clearable
              />
            </el-form-item>
            <el-form-item label="邮箱">
              <el-input
                v-model="registerForm.email"
                type="email"
                placeholder="请输入邮箱（可选）"
                size="large"
                clearable
              />
            </el-form-item>
            <el-form-item>
              <el-button
                type="primary"
                size="large"
                style="width: 100%"
                :loading="loading"
                @click="handleRegister"
              >
                注册
              </el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login, register, saveAuthData, type LoginRequest, type RegisterRequest } from '../api/authApi'

const router = useRouter()
const activeTab = ref('login')
const loading = ref(false)

const loginForm = ref<LoginRequest>({
  username: '',
  password: ''
})

const registerForm = ref<RegisterRequest>({
  username: '',
  password: '',
  nickname: '',
  email: ''
})

async function handleLogin() {
  if (!loginForm.value.username || !loginForm.value.password) {
    ElMessage.warning('请输入用户名和密码')
    return
  }

  loading.value = true
  try {
    const auth = await login(loginForm.value)
    saveAuthData(auth)
    ElMessage.success('登录成功')
    router.push('/chat')
  } catch (e) {
    ElMessage.error('登录失败，请检查用户名和密码')
  } finally {
    loading.value = false
  }
}

async function handleRegister() {
  if (!registerForm.value.username || !registerForm.value.password) {
    ElMessage.warning('请输入用户名和密码')
    return
  }

  loading.value = true
  try {
    const auth = await register(registerForm.value)
    saveAuthData(auth)
    ElMessage.success('注册成功')
    router.push('/chat')
  } catch (e) {
    ElMessage.error('注册失败，用户名可能已存在')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}

.login-card {
  background: #fff;
  border-radius: 16px;
  padding: 40px;
  width: 100%;
  max-width: 420px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.2);
}

.login-header {
  text-align: center;
  margin-bottom: 32px;
}

.logo-icon {
  font-size: 48px;
  display: block;
  margin-bottom: 16px;
}

.login-header h1 {
  font-size: 28px;
  color: #333;
  margin: 0 0 8px 0;
}

.login-header p {
  color: #666;
  margin: 0;
  font-size: 14px;
}

.login-tabs {
  margin-top: 24px;
}

.demo-tip {
  text-align: center;
  margin-top: 16px;
}
</style>
