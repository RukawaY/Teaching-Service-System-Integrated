<template>
    <div class="attendance-container">
      <el-card class="attendance-card">
        <template #header>
          <div class="card-header">
            <h2>考勤管理</h2>
          </div>
        </template>
  
        <!-- 课程列表 -->
        <div v-loading="loading">
          <el-collapse v-model="activeNames">
            <el-collapse-item 
              v-for="(course, index) in courseList" 
              :key="index"
              :title="course.course_name"
              :name="index.toString()"
            >

              <!-- 日期选择器 & 提交按钮 -->
              <div class="date-selector">
                <el-date-picker
                  v-model="course.selectedDate"
                  type="date"
                  placeholder="选择日期"
                  format="YYYY-MM-DD"
                  value-format="YYYY-MM-DD"
                  :disabled-date="disableFutureDates"
                  @change="(date) => handleDateChange(course, date)"
                />
                <el-button type="primary" @click="addAttendanceDate(course)">
                  添加考勤日期
                </el-button>
                <el-button type="success" @click="submitAttendance(course)">
                  提交考勤记录
                </el-button>
              </div>
  
              <!-- 考勤日期列表 -->
              <el-tabs v-model="course.activeDate" type="card" class="attendance-tabs">
                <el-tab-pane 
                  v-for="date in course.attendanceDates" 
                  :key="date.date"
                  :label="date.date"
                  :name="date.date"
                >
                  <!-- 考勤记录表格（学生姓名+成绩输入） -->
                  <el-table :data="date.students" style="width: 100%">
                    <el-table-column label="学生ID" width="150">  <!-- 修改标签为学生ID -->
                      <template #default="{ row }">
                        <el-input v-model="row.student_id" placeholder="输入学生ID" />  <!-- 绑定student_id -->
                      </template>
                    </el-table-column>
                    <el-table-column label="考勤成绩" width="150">
                      <template #default="{ row }">
                        <el-input-number v-model="row.score" :min="0" :max="100" placeholder="输入成绩（0-100）" />
                      </template>
                    </el-table-column>
                  </el-table>
                </el-tab-pane>
              </el-tabs>
            </el-collapse-item>
          </el-collapse>
  
          <el-empty v-if="!loading && courseList.length === 0" description="暂无课程" />
        </div>
      </el-card>
    </div>
  </template>
  
  <script setup>
  import { ref, onMounted, inject } from 'vue'
  import { ElMessage } from 'element-plus'
  import teacherAPI from '../../../api/resource_share/teacher'  // 导入接口
  
  // 状态管理
  const loading = ref(false)
  const exporting = ref(false)
  const activeNames = ref(['0'])  // 默认展开第一个课程
  const courseList = ref([])
  const userName = inject('user_name')  // 从顶层注入教师姓名（需确保已登录）
  
  // 方法
  const getCourseList = async () => {
    loading.value = true
    try {
      // 调用接口获取教师课程列表
      const res = await teacherAPI.getTeacherCourses(userName.value)
      if (res.code === '200') {
        // 初始化课程数据结构
        courseList.value = res.data.map(courseName => ({
          course_name: courseName,
          attendance_weight: 0.2,  // 初始比例（可从后端获取）
          selectedDate: '',
          activeDate: '',
          attendanceDates: []  // 考勤日期列表
        }))
      } else {
        ElMessage.error(`获取课程失败：${res.message}`)
      }
    } catch (error) {
      ElMessage.error(`获取课程列表失败：${error.message || '未知错误'}`)
    } finally {
      loading.value = false
    }
  }
  
  const disableFutureDates = (time) => {
    return time.getTime() > Date.now()
  }
  
  const handleDateChange = (course, date) => {
    course.selectedDate = date
  }
  
  const addAttendanceDate = (course) => {
    if (!course.selectedDate) {
      ElMessage.warning('请先选择日期')
      return
    }
    if (course.attendanceDates.some(d => d.date === course.selectedDate)) {
      ElMessage.warning('该日期已存在')
      return
    }
    // 初始化新日期的学生列表（可根据实际需求从后端获取学生）
    course.attendanceDates.push({
      date: course.selectedDate,
      students: []  // 空列表，教师手动输入学生
    })
    course.activeDate = course.selectedDate
    course.selectedDate = ''
    ElMessage.success('添加日期成功')
  }
  
  // 提交考勤记录
  // 移除原比例设置方法
  // const handleWeightChange = async (course, value) => { ... }
  
  // 修改提交考勤记录方法
  // 提交考勤记录时传递student_id
  const submitAttendance = async (course) => {
    if (!course.activeDate) {
      ElMessage.warning('请选择考勤日期');
      return;
    }
    const currentDate = course.attendanceDates.find(d => d.date === course.activeDate);
    if (currentDate.students.length === 0) {
      ElMessage.warning('请至少添加一条考勤记录');
      return;
    }
    
    try {
      for (const student of currentDate.students) {
        if (!student.student_id || !student.score) {  // 检查student_id是否存在
          throw new Error('请完善所有学生ID和成绩');
        }
        // 调用接口时传递studentId（注意类型转换，假设ID是数字）
        const res = await teacherAPI.processAttendanceRecord({
          studentId: Number(student.student_id),  // 转换为数字类型
          courseName: course.course_name,
          score: student.score,
          ratio: course.attendance_weight * 100 
        });
        if (!res.data) throw new Error('处理失败');
      }
      ElMessage.success('考勤记录处理成功');
    } catch (error) {
      ElMessage.error(`提交失败：${error.message}`);
    }
  };
  
  // 设置考勤比例
  const handleWeightChange = async (course, value) => {
    try {
      const res = await teacherAPI.setAttendanceWeightApi({
        courseName: course.course_name,
        weight: value
      })
      if (res.code === '200' && res.data) {
        ElMessage.success('比例设置成功')
      } else {
        throw new Error(res.message || '设置失败')
      }
    } catch (error) {
      ElMessage.error(`设置失败：${error.message}`)
      // 回滚比例
      course.attendance_weight = course.attendance_weight  // 实际应保存原始值
    }
  }
  
  // 生命周期钩子
  onMounted(() => {
    if (userName.value) getCourseList()  // 确保有教师姓名时加载
    else ElMessage.warning('未获取到教师信息')
  })
  </script>
  
  <style scoped>
  .attendance-container {
    padding: 20px;
    max-width: 1200px;
    margin: 0 auto;
  }
  
  .attendance-card {
    margin-top: 20px;
  }
  
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
  
  .card-header h2 {
    margin: 0;
    font-size: 1.5em;
    color: #303133;
  }
  
  .attendance-weight {
    margin-bottom: 20px;
    display: flex;
    align-items: center;
    gap: 10px;
  }
  
  .weight-label {
    font-size: 1.1em;
    color: #606266;
  }
  
  .date-selector {
    margin-bottom: 20px;
    display: flex;
    gap: 10px;
    align-items: center;
  }
  
  .attendance-tabs {
    margin-top: 20px;
  }
  
  :deep(.el-collapse-item__header) {
    font-size: 1.1em;
    font-weight: bold;
  }
  
  :deep(.el-input-number) {
    width: 150px;
  }
  
  :deep(.el-select) {
    width: 120px;
  }
  
  :deep(.el-tabs__item) {
    font-size: 1em;
  }
  </style>
