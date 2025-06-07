<template>
  <el-drawer
    :model-value="isSidebarVisible.get()"
    @update:model-value="isSidebarVisible.set"
    title="选择模块"
    direction="ltr"
    :with-header="true"
    v-if="user !== 'invalid'"
    size="250px"
    header-class="custom-drawer-header"
  >
  <el-divider />
    <el-menu :default-active="activeModule.get()" @select="handleSelect">
        <el-menu-item index="favorite">
        <el-icon><Star /></el-icon>
        <span>我的收藏</span>
      </el-menu-item>
      <el-menu-item index="infoManagement">
        <el-icon><Setting /></el-icon>
        <span>信息管理</span>
      </el-menu-item>
      <el-menu-item index="courseArrangement" v-if="user !== 'student'">
        <el-icon><Calendar /></el-icon>
        <span>课程安排</span>
      </el-menu-item>
      <el-menu-item index="courseSelection">
        <el-icon><School /></el-icon>
        <span>课程选择</span>
      </el-menu-item>
      <el-menu-item index="onlineQuiz" v-if="user !== 'admin'">
        <el-icon><EditPen /></el-icon>
        <span>在线测验</span>
      </el-menu-item>
      <el-menu-item index="resourceSharing" v-if="user !== 'admin'">
        <el-icon><Share /></el-icon>
        <span>课程资源共享</span>
      </el-menu-item>
    </el-menu>
  </el-drawer>
</template>

<script setup>
import { inject } from 'vue';
import { Setting, Calendar, School, EditPen, Share } from '@element-plus/icons-vue';
import { useRouter } from 'vue-router'; 

const isSidebarVisible = inject('is_sidebar_visible');
const activeModule = inject('activeModule');
const is_homepage = inject('is_homepage');
const router = useRouter();
const user = inject('user');

const handleSelect = (index) => {
  activeModule.set(index);
  router.push('/home');
  is_homepage.set(true); // 确保在主页模块间切换
  isSidebarVisible.toggle(); // 选择后关闭侧边栏
};
</script>

<style scoped>
.el-menu {
  border-right: none;
}

.custom-drawer-header {
  font-size: 20px;
}

.el-menu-item {
  font-size: 16px;
  margin: 10px;
}

.el-menu-item .el-icon {
  margin-right: 10px;
}
</style>
