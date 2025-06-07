<template>
  <div class="search-test-container">
    <h2>考试查询</h2>
    <p>请前往考试查询页面进行查询</p>
    <el-button type="primary" @click="goToQueryTest">前往考试查询</el-button>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { useuserLoginStore } from '../../../store/userLoginStore'

const router = useRouter()
const userStore = useuserLoginStore()

const goToQueryTest = () => {
  router.push('/online_test/queryTest')
}
</script>

<style scoped>
.search-test-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 100px 0;
}

h2 {
  margin-bottom: 20px;
}

p {
  margin-bottom: 30px;
  color: #606266;
}
</style>