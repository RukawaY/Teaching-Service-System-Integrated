<template>
  <div class="classroom-schedule">
    <el-card class="filter-card">
      <template #header>
        <div class="card-header">
          <h2>教室课表查询</h2>
        </div>
      </template>

      <div class="filter-form">
        <el-form :model="filterForm">
          <div class="filter-row">
            <el-form-item label="教室" class="filter-item same-size">
              <el-autocomplete
                v-model="filterForm.classroomId"
                :fetch-suggestions="queryClassrooms"
                placeholder="请输入教室位置或类型"
                @select="handleClassroomSelect"
                :trigger-on-focus="true"
                class="classroom-select"
              >
                <template #default="{ item }">
                  <div class="classroom-option">
                    <span>{{ item.label }}</span>
                  </div>
                </template>
              </el-autocomplete>
            </el-form-item>
            <el-form-item label="学期" class="filter-item same-size">
              <el-select v-model="filterForm.semester" placeholder="请选择学期">
                <el-option label="春夏" value="春夏" />
                <el-option label="秋冬" value="秋冬" />
              </el-select>
            </el-form-item>
            <el-form-item label="学年" class="filter-item same-size">
              <el-input v-model="filterForm.sec_year" placeholder="请输入学年" />
            </el-form-item>
            <el-button type="primary" @click="handleQuery" :loading="loading">
              查询
            </el-button>
          </div>
        </el-form>
      </div>
    </el-card>

    <el-card class="schedule-card">
      <div class="schedule-container">
        <table class="schedule-table">
          <thead>
            <tr>
              <th>时间</th>
              <th v-for="day in weekDays" :key="day.value">{{ day.label }}</th>
            </tr>
          </thead>
          <tbody>
            <template v-for="timeSlot in timeSlots" :key="timeSlot.time">
              <tr>
                <td class="time-slot">{{ timeSlot.time }}</td>
                <td
                  v-for="day in weekDays"
                  :key="day.value"
                  :rowspan="getCourseRowspan(day.value, timeSlot)"
                  :class="{
                    'course-cell': true,
                    'occupied': isCourseStart(day.value, timeSlot),
                    'empty': !isCourseStart(day.value, timeSlot)
                  }"
                  v-show="isCourseStart(day.value, timeSlot) || !getCourse(day.value, timeSlot)"
                >
                  <div v-if="isCourseStart(day.value, timeSlot)" class="course-info">
                    <div class="course-name">{{ getCourse(day.value, timeSlot).courseName }}</div>
                    <div class="teacher">{{ getCourse(day.value, timeSlot).teacher || '未指定教师' }}</div>
                    <div class="time-range">{{ getCourseTimeRange(getCourse(day.value, timeSlot)) }}</div>
                  </div>
                </td>
              </tr>
            </template>
          </tbody>
        </table>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import axios from 'axios';

const filterForm = reactive({
  classroomId: '', // 默认教室ID
  semester: '春夏', // 默认学期
  sec_year: new Date().getFullYear().toString(), // 默认当前学年
});

const loading = ref(false);

const queryClassrooms = async (query, callback) => {
  try {
    const response = await axios.get('/arrange/api/classrooms/query', {
      params: { query: query }
    });
    if (response.data) {
      callback(response.data.map(item => ({
        value: item.id.toString(),
        label: `${item.location} (${item.category}教室, 容量:${item.capacity})`
      })));
    } else {
      callback([]);
    }
  } catch (error) {
    console.error('获取教室列表失败:', error);
    callback([]);
  }
};

const handleClassroomSelect = (item) => {
  filterForm.classroomId = item.value;
};

const weekDays = ref([
  { value: 'Monday', label: '周一' },
  { value: 'Tuesday', label: '周二' },
  { value: 'Wednesday', label: '周三' },
  { value: 'Thursday', label: '周四' },
  { value: 'Friday', label: '周五' },
  { value: 'Saturday', label: '周六' },
  { value: 'Sunday', label: '周日' }
]);

const timeSlots = ref([
  { time: '08:00-08:45', start: 1, end: 1 },
  { time: '08:50-09:35', start: 2, end: 2 },
  { time: '10:00-10:45', start: 3, end: 3 },
  { time: '10:50-11:35', start: 4, end: 4 },
  { time: '11:40-12:25', start: 5, end: 5 },
  { time: '13:25-14:10', start: 6, end: 6 },
  { time: '14:15-15:00', start: 7, end: 7 },
  { time: '15:05-15:50', start: 8, end: 8 },
  { time: '16:15-17:00', start: 9, end: 9 },
  { time: '17:05-17:50', start: 10, end: 10 },
  { time: '18:50-19:35', start: 11, end: 11 },
  { time: '19:40-20:25', start: 12, end: 12 },
  { time: '20:30-21:15', start: 13, end: 13 }
]);

const scheduleData = ref([]);

const getCourse = (day, timeSlot) => {
  return scheduleData.value.find(course =>
    course.day === day &&
    course.timeSlot.start <= timeSlot.start &&
    course.timeSlot.end >= timeSlot.end &&
    course.classroomId === parseInt(filterForm.classroomId)
  );
};

const isCourseStart = (day, timeSlot) => {
  const course = getCourse(day, timeSlot);
  if (!course) return false;
  return course.timeSlot.start === timeSlot.start;
};

const getCourseRowspan = (day, timeSlot) => {
  const course = getCourse(day, timeSlot);
  if (!course) return 1;
  return course.timeSlot.end - course.timeSlot.start + 1;
};

const getCourseTimeRange = (course) => {
  if (!course) return '';
  const startSlot = timeSlots.value.find(slot => slot.start === course.timeSlot.start);
  const endSlot = timeSlots.value.find(slot => slot.end === course.timeSlot.end);
  return `${startSlot.time.split('-')[0]}~${endSlot.time.split('-')[1]}`;
};

const handleQuery = async () => {
  if (!filterForm.classroomId) {
    ElMessage.warning('请选择教室');
    return;
  }

  loading.value = true;
  scheduleData.value = [];
  try {
    const response = await axios.get('/arrange/api/sections/query', {
      params: {
        classroom_id: filterForm.classroomId,
        semester: filterForm.semester,
        year: filterForm.sec_year
      }
    });
    if (response.data && response.data.length > 0) {
      const filteredData = response.data.filter(item =>
        item.classroomId.toString() === filterForm.classroomId
      );

      if (filteredData.length === 0) {
        ElMessage.info('该教室没有课程安排');
        return;
      }

      scheduleData.value = filteredData.map(item => {
        const processedData = [];
        const times = item.secTime.split(';').map(t => t.trim());
        const timeGroups = {};
        
        // 处理每个时间段
        times.forEach(time => {
          const [dayOfWeekStr, timeNum] = time.split(' ');
          if (!timeGroups[dayOfWeekStr]) {
            timeGroups[dayOfWeekStr] = [];
          }
          timeGroups[dayOfWeekStr].push(parseInt(timeNum));
        });

        // 对每个工作日的时间段进行处理
        Object.entries(timeGroups).forEach(([dayOfWeekStr, timeNums]) => {
          const day = getDayOfWeekFromString(dayOfWeekStr);
          
          // 对时间数字进行排序
          timeNums.sort((a, b) => a - b);
          
          // 识别连续的时间段
          let start = timeNums[0];
          let end = timeNums[0];
          
          for (let i = 1; i < timeNums.length; i++) {
            // 检查是否是午休时间（第5节和第6节之间）或晚上休息时间（第10节和第11节之间）
            if (timeNums[i] === end + 1 && 
                !((end === 5 && timeNums[i] === 6) || (end === 10 && timeNums[i] === 11))) {
              end = timeNums[i];
            } else {
              // 当前时间段结束，创建新的课程记录
              processedData.push({
                id: item.id,
                courseId: item.courseId,
                classroomId: item.classroomId,
                day: day,
                timeSlot: { start, end },
                courseName: item.courseName,
                teacher: item.teacherName || '未指定教师',
              });
              
              // 开始新的时间段
              start = timeNums[i];
              end = timeNums[i];
            }
          }
          
          // 添加最后一个时间段
          processedData.push({
            id: item.id,
            courseId: item.courseId,
            classroomId: item.classroomId,
            day: day,
            timeSlot: { start, end },
            courseName: item.courseName,
            teacher: item.teacherName || '未指定教师',
          });
        });
        
        return processedData;
      }).flat();

      ElMessage.success('查询成功');
    } else {
      ElMessage.info('没有课程安排数据');
    }
  } catch (error) {
    ElMessage.error('获取教室课表失败:' + error);
    console.error('获取教室课表失败:', error);
  } finally {
    loading.value = false;
  }
};

const getDayOfWeekFromString = (dayStr) => {
  const dayMap = {
    'Monday': 'Monday',
    'Tuesday': 'Tuesday',
    'Wednesday': 'Wednesday',
    'Thursday': 'Thursday',
    'Friday': 'Friday',
    'Saturday': 'Saturday',
    'Sunday': 'Sunday'
  };
  return dayMap[dayStr] || '';
};

onMounted(() => {
  // 页面加载时不进行自动查询，等待用户选择教室后点击查询
});
</script>

<style scoped>
.same-size {
  width: 200px;
}

.classroom-schedule {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.filter-card {
  margin-bottom: 20px;
}

.card-header {
  text-align: center;
}

.filter-form {
  display: flex;
  justify-content: center;
}

.filter-row {
  display: flex;
  align-items: center;
  gap: 20px;
}

.filter-item {
  margin-bottom: 0;
}

.schedule-container {
  overflow-x: auto;
}

.schedule-table {
  width: 100%;
  border-collapse: collapse;
}

.schedule-table th,
.schedule-table td {
  border: 1px solid #e0e0e0;
  padding: 8px;
  text-align: center;
}

.schedule-table th {
  background-color: #f5f5f5;
}

.time-slot {
  background-color: #f5f5f5;
  width: 100px;
}

.course-cell {
  height: 80px;
  vertical-align: middle;
}

.empty {
  background-color: #f9f9f9;
}

.occupied {
  background-color: #e3f2fd;
}

.course-info {
  display: flex;
  flex-direction: column;
  justify-content: center;
  height: 100%;
}

.course-name {
  font-weight: bold;
  margin-bottom: 4px;
}

.teacher {
  font-size: 0.9em;
  color: #666;
  margin-bottom: 4px;
}

.time-range {
  font-size: 0.8em;
  color: #888;
}

.classroom-select {
  width: 100%;
}

.classroom-option {
  padding: 4px 0;
}

.classroom-option span {
  font-size: 14px;
}
</style>