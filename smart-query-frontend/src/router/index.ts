import { createRouter, createWebHistory } from 'vue-router'
import { isAuthenticated } from '../api/authApi'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: () => import('../views/LoginView.vue'),
      meta: { title: '登录', requiresAuth: false }
    },
    {
      path: '/',
      redirect: '/chat'
    },
    {
      path: '/chat',
      name: 'chat',
      component: () => import('../views/ChatView.vue'),
      meta: { title: '智能问答', requiresAuth: true }
    },
    {
      path: '/metadata',
      name: 'metadata',
      component: () => import('../views/MetadataView.vue'),
      meta: { title: '元数据管理', requiresAuth: true }
    },
    {
      path: '/llm-providers',
      name: 'llm-providers',
      component: () => import('../views/LlmProvidersView.vue'),
      meta: { title: '模型管理', requiresAuth: true }
    }
  ]
})

// 路由守卫 - 检查认证
router.beforeEach((to, from, next) => {
  if (to.meta.requiresAuth && !isAuthenticated()) {
    next('/login')
  } else if (to.path === '/login' && isAuthenticated()) {
    next('/chat')
  } else {
    next()
  }
})

export default router
