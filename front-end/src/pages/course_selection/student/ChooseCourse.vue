<template>
    <div class="choose-course container">
      <el-card class="form-card">
        <template #header>
          <div class="card-header">
            <h2>选择课程</h2>
            <p>请选择您要修读的课程</p>
          </div>
        </template>

        <div class="search-bar">
          <el-input
            v-model="searchQuery.course_name"
            placeholder="课程名称 (可筛选方案内课程)"
            clearable
            class="search-input"
          />
          <el-input
            v-model="searchQuery.teacher_name"
            placeholder="教师姓名"
            clearable
            class="search-input"
          />
          <el-input
            v-model="searchQuery.course_id"
            placeholder="课程ID (用于搜索)"
            clearable
            class="search-input"
          />
          <el-checkbox v-model="searchQuery.need_available">
            仅显示有余量课程
          </el-checkbox>
          <el-button type="primary" @click="handleSearch" :loading="loading">搜索</el-button>
          <el-button @click="resetSearch" :disabled="loading">重置</el-button>
        </div>

        <el-table :data="courseList" style="font-size: 15px;" v-loading="loading">
          <el-table-column prop="course_id" label="课程ID" width="80" />
          <el-table-column prop="section_id" label="教学班ID" width="90" />
          <el-table-column prop="course_name" label="课程名称" />
          <el-table-column prop="teacher_name" label="授课教师" />
          <el-table-column prop="credit" label="学分" width="80" />
          <el-table-column prop="class_time" label="上课时间" />
          <el-table-column prop="classroom" label="上课教室" />
          <el-table-column label="课程容量">
            <template #default="scope">
              {{ scope.row.available_capacity }} / {{ scope.row.total_capacity }}
            </template>
          </el-table-column>
          <el-table-column label="选择" width="120">
            <template #default="scope">
              <el-checkbox
                v-model="selectedCourses[scope.row.section_id]" :disabled="scope.row.available_capacity <= 0"
              />
            </template>
          </el-table-column>
        </el-table>

        <div class="submit-area">
          <el-button
            type="primary"
            @click="submitCourses"
            :loading="submitting"
            :disabled="!canSubmit || loading"
          >
            提交选课
          </el-button>
        </div>
      </el-card>

      <el-empty v-if="showEmpty" description="暂无可选课程或未搜索到结果（请确认培养方案中是否有课程）" />
    </div>
  </template>

  <script setup>
  import { ref, computed, onMounted, inject } from 'vue';
  import { ElMessage, ElMessageBox } from 'element-plus';
  import { searchCourse, submitStudentCourseSelection } from '../../../api/course_selection/student';

  const courseList = ref([]); //
  const selectedCourses = ref({}); // 
  const submitting = ref(false);
  const loading = ref(false);

  const searchQuery = ref({
    course_name: '',
    teacher_name: '',
    course_id: '',
    need_available: false,
  });

  const studentId = inject('user_id'); //

  /// Monday 1; Monday 2 ==> 周一1-2节
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

  const fetchCourses = async () => {
    if (!studentId.value) {
      ElMessage.warning('学生ID无效，请确保您已登录或ID已正确注入。');
      courseList.value = []; // 
      loading.value = false;
      return;
    }
    loading.value = true;
    try {
      const paramsToApi = {}; // 
      if (searchQuery.value.course_name) paramsToApi.course_name = searchQuery.value.course_name; // 
      if (searchQuery.value.teacher_name) paramsToApi.teacher_name = searchQuery.value.teacher_name; // 
      if (searchQuery.value.course_id) paramsToApi.course_id = Number(searchQuery.value.course_id); // 
      if (searchQuery.value.need_available) paramsToApi.need_available = searchQuery.value.need_available; // 
      paramsToApi.student_id = Number(studentId.value); // 

      const response = await searchCourse(paramsToApi); // 
      if (response && response.code === '200' && response.data && response.data.course_list) { // 
        // 在这里应用转换函数
        courseList.value = response.data.course_list.map(course => ({
          ...course,
          class_time: reflectTime(course.class_time) // 转换上课时间
        }));
        if (courseList.value.length === 0) { //
          ElMessage.info('培养方案中未找到符合当前搜索条件的课程，或培养方案为空。'); // 
        }
      } else {
        ElMessage.error(response.message || '获取课程列表失败'); // 
        courseList.value = []; // 
      }
    } catch (error) {
      ElMessage.error(error.message || '请求课程列表时发生网络错误'); // 
      console.error('Error fetching courses:', error); // 
      courseList.value = []; // 
    } finally {
      loading.value = false; // 
    }
  };

  onMounted(() => { // 
    if (studentId.value) {
        fetchCourses(); // 
    }
  });

  const handleSearch = () => { // 
    fetchCourses(); // 
  };

  const resetSearch = () => { // 
    searchQuery.value = { // 
      course_name: '',
      teacher_name: '',
      course_id: '',
      need_available: false,
    };
    fetchCourses(); // 
  };

  const showEmpty = computed(() => !loading.value && courseList.value.length === 0); // 
  const canSubmit = computed(() => { // 
    return Object.values(selectedCourses.value).some((isSelected) => isSelected); // 
  });

  const submitCourses = async () => { // 
    if (!studentId.value) {
      ElMessage.error('学生ID无效，无法提交选课！'); // 
      return; // 
    }
    if (!canSubmit.value) { // 
      ElMessage.warning('请至少选择一门课程'); // 
      return; // 
    }

    try {
      await ElMessageBox.confirm('确定要提交选课结果吗？', '提示', { // 
        confirmButtonText: '确定', // 
        cancelButtonText: '取消', // 
        type: 'warning', // 
      });
      submitting.value = true; // 
      let allSubmittedSuccessfully = true; // 
      let errorMessages = []; // 
      const selectedSectionIds = Object.keys(selectedCourses.value).filter( // 
        (sectionId) => selectedCourses.value[sectionId] // 
      );
      for (const sectionId of selectedSectionIds) { // 
        try {
          const response = await submitStudentCourseSelection({ // 
            student_id: Number(studentId.value), // 
            course_id: Number(sectionId), // 
          });
          if (response && response.code !== '200' && response.code !== 'success') { // 
            allSubmittedSuccessfully = false; // 
            const courseWithError = courseList.value.find(c => c.section_id === Number(sectionId)); // 
            const courseNameForError = courseWithError ? courseWithError.course_name : `教学班ID ${sectionId}`; // 
            errorMessages.push(`${courseNameForError} (ID ${sectionId}): ${response.message || '选课失败'}`); // 
          }
        } catch (err) {
          allSubmittedSuccessfully = false; // 
          const courseWithError = courseList.value.find(c => c.section_id === Number(sectionId)); // 
          const courseNameForError = courseWithError ? courseWithError.course_name : `教学班ID ${sectionId}`; // 
          errorMessages.push(`${courseNameForError} (ID ${sectionId}): ${err.message || '提交时发生网络错误'}`); // 
          console.error(`Error submitting section ${sectionId}:`, err); // 
        }
      }

      if (allSubmittedSuccessfully) { // 
        ElMessage.success('所有选课提交成功！'); // 
        selectedCourses.value = {}; // 
        fetchCourses(); // 
      } else {
        ElMessageBox.alert(`部分课程选课失败或发生错误：<br/>${errorMessages.join('<br/>')}`, '提交结果', { // 
          dangerouslyUseHTMLString: true, // 
          confirmButtonText: '我知道了' // 
        });
      }

    } catch (error) {
      if (error !== 'cancel' && error !== 'close') { // 
        ElMessage.error(error.message || '选课提交操作失败'); // 
        console.error('Error in submitCourses process:', error); // 
      }
    } finally {
      submitting.value = false; // 
    }
  };
  </script>

  <style scoped>
  .choose-course {
    max-width: 1200px; /* */ /*  */
    margin: 0 auto; /* */ /*  */
    padding: 20px; /* */ /*  */
  }
  .container {
    display: flex; /* */ /*  */
    flex-direction: column; /* */ /*  */
    gap: 20px; /* */ /*  */
  }
  .form-card {
    width: 95%; /* */ /*  */
    margin: 0 auto 20px auto; /* */ /*  */
  }
  .card-header {
    display: flex; /* */ /*  */
    flex-direction: column; /* */ /*  */
    align-items: center; /* */ /*  */
  }
  .search-bar {
    display: flex; /* */ /*  */
    flex-wrap: wrap; /* */ /*  */
    gap: 10px; /* */ /*  */
    margin-bottom: 20px; /* */ /*  */
    align-items: center; /* */ /*  */
  }
  .search-input {
    width: 180px; /* */ /*  */
  }
  .submit-area {
    margin-top: 20px; /* */ /*  */
    display: flex; /* */ /*  */
    justify-content: center; /* */ /*  */
  }
  </style>