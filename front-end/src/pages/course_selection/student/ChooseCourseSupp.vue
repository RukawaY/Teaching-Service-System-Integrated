<template>
  <div class="choose-course-supp container">
    <el-card class="form-card">
      <template #header>
        <div class="card-header-center">
          <h2>课程查询与补选</h2>
        </div>
      </template>

      <el-form :model="searchFormData" label-width="120px" class="input-form">
        <el-form-item label="课程名称">
          <el-input v-model="searchFormData.courseName" placeholder="请输入课程名称" />
        </el-form-item>
        <el-form-item label="教师姓名">
          <el-input v-model="searchFormData.teacherName" placeholder="请输入教师姓名" />
        </el-form-item>
        <el-form-item label="课程ID">
          <el-input v-model.number="searchFormData.courseId" placeholder="请输入课程ID" type="number" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="searchCourses" :loading="loading">
            查询课程
          </el-button>
          <el-button @click="resetForm">
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card v-if="courseList.length > 0" class="result-card">
      <template #header>
        <div class="card-header-center">
          <h2>查询结果 (共 {{ courseList.length }} 门)</h2>
        </div>
      </template>

      <el-table :data="courseList" style="width: 100%">
        <el-table-column prop="course_id" label="课程ID" width="90" />
        <el-table-column prop="section_id" label="教学班ID" width="90" />
        <el-table-column prop="course_name" label="课程名称" />
        <el-table-column prop="teacher_name" label="授课教师" />
        <el-table-column prop="credit" label="学分" width="80" />
        <el-table-column prop="class_time" label="上课时间">
          <template #default="scope">
            {{ reflectTime(scope.row.class_time) }}
          </template>
        </el-table-column>
        <el-table-column prop="classroom" label="上课教室" />
        <el-table-column label="课程容量" width="120">
          <template #default="scope">
            {{ scope.row.available_capacity }} / {{ scope.row.total_capacity }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="scope">
            <el-tag v-if="selectedCourseIds.has(scope.row.section_id)" type="success">已选</el-tag>
            <el-button
                v-else
                type="primary"
                size="small"
                @click="handleApply(scope.row)"
                :loading="submitting"
            >
              补选
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-empty v-if="noResults" description="未找到符合条件的课程" />

    <el-card class="applications-card">
      <template #header>
        <div class="card-header-flex">
          <h2>补选申请记录</h2>
          <el-button type="primary" @click="refreshApplications" :loading="refreshing">
            刷新补选状态
          </el-button>
        </div>
      </template>

      <el-table v-if="applications.length > 0" :data="applications" style="width: 100%">
        <el-table-column prop="section_id" label="教学班ID" width="120" />
        <el-table-column prop="course_name" label="课程名称" />
        <el-table-column prop="teacher_name" label="教师姓名" />
        <el-table-column prop="class_time" label="上课时间">
          <template #default="scope">
            {{ reflectTime(scope.row.class_time) }}
          </template>
        </el-table-column>
        <el-table-column prop="classroom" label="上课地点" />
        <el-table-column prop="credit" label="学分数" />
        <el-table-column prop="result" label="补选状态" width="120">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.result)">{{ scope.row.result }}</el-tag>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="applications.length === 0 && !refreshing" description="暂无补选申请记录" />

    </el-card>

  </div>
</template>

<script setup>
import { ref, reactive, inject, onMounted, computed } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { chooseCourseSupp, getSuppResult, searchCourse, getStudentSelectedCourses } from '../../../api/course_selection/student';

const studentId = inject('user_id');

// --- Search and Selection State ---
const searchFormData = reactive({
  courseName: '',
  teacherName: '',
  courseId: ''
});
const loading = ref(false);
const courseList = ref([]);
const searchPerformed = ref(false);
const selectedCourseIds = ref(new Set());
const noResults = computed(() => searchPerformed.value && courseList.value.length === 0);

const submitting = ref(false);
const refreshing = ref(false);
const applications = ref([]);


// --- Search and Selection Logic ---
const searchCourses = async () => {
  if (!searchFormData.courseName && !searchFormData.teacherName && !searchFormData.courseId) {
    ElMessage.warning('请至少输入一个查询条件');
    return;
  }

  loading.value = true;
  searchPerformed.value = true;

  try {
    // Fetch selected courses to mark them in results
    const selectedResponse = await getStudentSelectedCourses(studentId.value);
    if (selectedResponse.code === '200' && selectedResponse.data.course_list) {
      selectedCourseIds.value = new Set(selectedResponse.data.course_list.map(c => c.course_id));
    } else if (selectedResponse.code !== '200') {
      ElMessage.error('获取已选课程列表失败');
      selectedCourseIds.value.clear();
    }

    const params = {
      course_name: searchFormData.courseName || undefined,
      teacher_name: searchFormData.teacherName || undefined,
      course_id: searchFormData.courseId || undefined
    };

    const searchResponse = await searchCourse(params);
    if (searchResponse.code === '200') {
      courseList.value = searchResponse.data.course_list;
      if (courseList.value.length === 0) {
        ElMessage.info('未找到符合条件的课程');
      } else {
        ElMessage.success(`共找到 ${courseList.value.length} 门课程`);
      }
    } else {
      ElMessage.error(searchResponse.message || '查询失败');
    }
  } catch (error) {
    ElMessage.error('查询课程失败');
    console.error(error);
  } finally {
    loading.value = false;
  }
};

const resetForm = () => {
  searchFormData.courseName = '';
  searchFormData.teacherName = '';
  searchFormData.courseId = '';
  searchPerformed.value = false;
  courseList.value = [];
};

// 提交补选申请
const handleApply = async (course) => {
  ElMessageBox.confirm(
      `确定要补选课程 "${course.course_name}" 吗?`,
      '补选确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
  ).then(async () => {
    submitting.value = true;
    try {
      const params = {
        student_id: studentId.value,
        section_id: course.section_id,
      };

      const response = await chooseCourseSupp(params);

      if (response.code === '200') {
        ElMessage.success('补选申请提交成功');
        await refreshApplications(); // Refresh the history list
      } else {
        ElMessage.error(response.message || '提交失败');
      }
    } catch (error) {
      ElMessage.error('提交补选申请失败');
      console.error(error);
    } finally {
      submitting.value = false;
    }
  }).catch(() => {
    ElMessage.info('已取消补选');
  });
};

// Monday 1; Monday 2 ==> 周一1-2节
// Tuesday 1; Tuesday 2; Tuesday 3 ==> 周二1-3节
const reflectTime = (time) => {
  const timeArray = time.split(';');
  const firstTime = timeArray[0].trim();
  const lastTime = timeArray[timeArray.length - 1].trim();

  const chineseDay = {
    'Monday': '周一',
    'Tuesday': '周二',
    'Wednesday': '周三',
    'Thursday': '周四',
    'Friday': '周五',
    'Saturday': '周六',
    'Sunday': '周日'
  }

  const day = chineseDay[firstTime.split(' ')[0]];
  const period = `${firstTime.split(' ')[1]}-${lastTime.split(' ')[1]}`;
  return `${day} ${period}节`;
}


// 刷新补选申请状态
const refreshApplications = async () => {
  if (!studentId.value) {
    // No warning message here, as this can be called on mount before ID is available
    // or if the user is not logged in.
    // applications.value will remain empty, showing the empty state.
    applications.value = [];
    return;
  }

  try {
    refreshing.value = true;
    const response = await getSuppResult(studentId.value);
    
    if (response.code === '200') {
      if (response.data && response.data.result_list) {
        applications.value = response.data.result_list.map(item => ({
          student_id: studentId.value,
          section_id: item.course_id,
          course_name: item.course_name,
          teacher_name: item.teacher_name,
          class_time: item.class_time,
          classroom: item.classroom,
          credit: item.credit,
          result: reflectResult(item.result)
        }));
        if (applications.value.length > 0) {
          ElMessage.success('刷新补选状态成功');
        } else {
          ElMessage.info('暂无补选申请记录');
        }
      } else {
        applications.value = [];
        ElMessage.info('暂无补选申请数据');
      }
    } else {
      applications.value = [];
      ElMessage.error(response.message || '刷新补选状态失败');
    }
  } catch (error) {
    applications.value = [];
    ElMessage.error('刷新补选状态失败');
    console.error(error);
  } finally {
    refreshing.value = false;
  }
};

const reflectResult = (result) => {
  switch (result) {
    case '0':
      return '待审核';
    case '1':
      return '补选成功';
    case '2':
      return '补选失败';
    default:
      return '未知';
  }
}

// 获取状态对应的标签类型
const getStatusType = (status) => {
  switch (status) {
    case '补选成功':
      return 'success';
    case '补选失败':
      return 'danger';
    case '待审核':
    default:
      return 'info';
  }
};

onMounted(() => {
  refreshApplications();
});

</script>

<style scoped>
.choose-course-supp {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.container {
  display: flex;
  flex-direction: column;
  align-items: center; /* Center cards */
  gap: 20px;
}

.form-card {
  width: 95%;
  margin: 0px auto 20px auto;
}

.result-card {
  width: 95%;
  margin: 0px auto 20px auto;
}

.applications-card {
  width: 95%;
  margin: 0 auto;
}

.card-header-center {
  display: flex;
  justify-content: center; /* Center title */
  align-items: center;
}

.card-header-flex {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header-flex h2 {
  margin: 0;
}

.input-form {
  max-width: 400px; /* Adjusted for better centering within card */
  margin: 0 auto;
}

/* .empty-container is not used in the provided template, can be removed if not needed elsewhere */
</style>