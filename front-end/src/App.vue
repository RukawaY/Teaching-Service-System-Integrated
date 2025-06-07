<script setup>
import { RouterView } from 'vue-router'
import { ref, provide, onMounted, watchEffect } from 'vue';
import TopBar from './components/TopBar.vue'
import BottomBar from './components/BottomBar.vue'
import SideBar from './components/SideBar.vue'
import { useuserLoginStore } from './store/userLoginStore';

// Initialize the store
const userLoginStore = useuserLoginStore();

// TODO: get user info from other modules
const user = ref('invalid');
const user_name = ref('未登录');
const user_avatar = ref('https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png');
const user_id = ref('null');

const is_homepage = ref(true);
const is_sidebar_visible = ref(false);
const activeModule = ref('infoManagement');

// Watch for changes in loginUser from the store
watchEffect(() => {
  const currentUser = userLoginStore.loginUser;
  if (currentUser && currentUser.user_id !== 'null') {
    is_homepage.value = true;
    user_name.value = currentUser.name;
    user_id.value = currentUser.user_id;
    user_avatar.value = currentUser.avatar_path && currentUser.avatar_path !== 'null' ? "http://localhost:8080" + currentUser.avatar_path : 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png';

    // Map role
    switch (currentUser.role) {
      case 's':
        user.value = 'student';
        break;
      case 't':
        user.value = 'teacher';
        break;
      case 'a':
        user.value = 'admin';
        break;
      default:
        user.value = 'guest'; // Or some default/fallback role for unknown roles
    }
  } else {
    // Reset to default if user logs out or info is not available
    user_name.value = '未登录';
    user_avatar.value = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png';
    user.value = 'invalid'; 
    user_id.value = 'null';
  }
});

// 从localStorage读取保存的状态
onMounted(() => {
  const savedHomepage = localStorage.getItem('is_homepage');
  if (savedHomepage !== null) {
    is_homepage.value = savedHomepage === 'true';
  }
  const savedActiveModule = localStorage.getItem('activeModule');
  if (savedActiveModule !== null) {
    activeModule.value = savedActiveModule;
  }
});

// 监听is_homepage变化并保存到localStorage
function updateIsHomepage(value) {
  is_homepage.value = value;
  localStorage.setItem('is_homepage', value);
}

// 新增：监听activeModule变化并保存到localStorage
function updateActiveModule(value) {
  activeModule.value = value;
  localStorage.setItem('activeModule', value);
}

// 新增：设置侧边栏可见性
function setSidebarVisible(value) {
  is_sidebar_visible.value = value;
}

// 新增：切换侧边栏显示状态
function toggleSidebar() {
  is_sidebar_visible.value = !is_sidebar_visible.value;
}

provide('user', user);
provide('user_name', user_name);
provide('user_avatar', user_avatar);
provide('user_id', user_id);
provide('is_homepage', { 
  get: () => is_homepage.value,
  set: updateIsHomepage
});
provide('is_sidebar_visible', {
  get: () => is_sidebar_visible.value,
  set: setSidebarVisible,
  toggle: toggleSidebar
});
provide('activeModule', {
  get: () => activeModule.value,
  set: updateActiveModule
});

</script>

<template>
  <div>
    <TopBar />
    <SideBar />
    <!-- 渲染当前路由的组件 -->
    <RouterView style="margin-top: 80px; padding-bottom: 70px;"/>
    <BottomBar />
  </div>
</template>