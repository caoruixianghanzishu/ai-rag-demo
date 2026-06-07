<template>
  <el-container class="app-layout">
    <el-header class="app-header">
      <div class="logo">
        <el-icon :size="24"><ChatDotRound /></el-icon>
        <span>AI 智能知识库问答</span>
      </div>
      <el-menu
        mode="horizontal"
        :default-active="activeMenu"
        router
        class="nav-menu"
        :ellipsis="false"
      >
        <el-menu-item index="/chat">
          <el-icon><ChatLineRound /></el-icon>
          智能问答
        </el-menu-item>
        <el-menu-item index="/knowledge">
          <el-icon><Collection /></el-icon>
          知识库管理
        </el-menu-item>
      </el-menu>
    </el-header>
    <el-main class="app-main">
      <router-view />
    </el-main>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const activeMenu = computed(() => route.path)
</script>

<style scoped>
.app-layout {
  height: 100%;
}

.app-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  border-bottom: 1px solid #e4e7ed;
  padding: 0 24px;
  height: 60px;
}

.logo {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.nav-menu {
  border-bottom: none;
  background: transparent;
}

.app-main {
  padding: 16px;
  height: calc(100% - 60px);
  overflow: hidden;
}
</style>
