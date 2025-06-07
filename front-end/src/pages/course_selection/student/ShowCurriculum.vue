<template>
  <div class="show-curriculum container">
    <el-card v-if="curriculumData" class="curriculum-card">
      <template #header>
        <div class="card-header">
          <h2>{{ curriculumData.major_name }} 专业个人培养方案</h2>
          <p>您所选择的课程如下</p>
        </div>
      </template>

      <div v-for="(section, sectionIndex) in curriculumData.sections" :key="sectionIndex" class="section-container">
        <div class="section-header">
          <h4>{{ sectionIndex + 1 }}. {{ section.section_name }}</h4>
          <el-tag type="info" style="font-size: 15px;">模块要求学分: {{ section.section_credit }}</el-tag>
          <el-tag type="success" style="font-size: 15px;">已选学分: {{ getSectionTotalCredits(section) }}</el-tag>
        </div>

        <el-table v-if="section.course_list && section.course_list.length > 0" :data="section.course_list" style="font-size: 15px;">
          <el-table-column prop="course_name" label="课程名称" />
          <el-table-column prop="credit" label="学分" width="120" />
        </el-table>
        
        <el-empty v-else description="该模块暂无选择课程" />
      </div>
    </el-card>

    <el-empty v-if="showEmpty" description="您还没有制定个人培养方案" />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, inject } from 'vue';
import { ElMessage } from 'element-plus';
import { getPersonalCurriculum } from '../../../api/course_selection/student';

const studentId = inject('user_id');
const loading = ref(false);
const curriculumData = ref(null); // Initialize with null
const showEmpty = computed(() => !loading.value && !curriculumData.value);

// 计算每个部分的总学分
const getSectionTotalCredits = (section) => {
  if (!section.course_list || section.course_list.length === 0) return 0;
  
  return section.course_list.reduce((total, course) => {
    return total + Number(course.credit);
  }, 0);
};

// 获取个人培养方案
const fetchPersonalCurriculum = async () => {
  if (!studentId.value) {
    ElMessage.warning('学生ID无效，请重新登录');
    curriculumData.value = null; // Ensure curriculumData is null if studentId is missing
    loading.value = false; // Stop loading indicator
    return;
  }
  
  try {
    loading.value = true;
    const response = await getPersonalCurriculum(studentId.value);
    
    if (response.code === '200') {
      if (response.data && Object.keys(response.data).length > 0) {
        curriculumData.value = response.data;
        ElMessage.success('获取个人培养方案成功');
      } else {
        curriculumData.value = null; // Set to null if data is empty or not as expected
        ElMessage.info('您可能还没有制定个人培养方案');
      }
    } else {
      ElMessage.error(response.message || '获取失败');
      curriculumData.value = null;
    }
  } catch (error) {
    ElMessage.error('获取个人培养方案失败');
    curriculumData.value = null; 
    console.error(error);
  } finally {
    loading.value = false;
  }
};

onMounted(() => {
  fetchPersonalCurriculum();
});
</script>

<style scoped>
.show-curriculum {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.container {
  display: flex;
  flex-direction: column;
  gap: 20px;
  align-items: center; /* Center content when only empty message is shown */
}

.card-header {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.section-container {
  margin-bottom: 30px;
  width: 90%;
  margin: 0 auto;
}

.section-header {
  display: flex;
  align-items: center;
  margin-bottom: 15px;
  margin-top: 18px;
  gap: 15px;
}

.section-header h4 {
  margin: 0;
  flex-grow: 1;
}

.curriculum-card {
  width: 65%; /* Or adjust as needed */
  margin: 20px auto; /* Add some margin */
}
</style>