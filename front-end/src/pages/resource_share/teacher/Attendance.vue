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

              <!-- 考勤比例设置 -->
              <div class="attendance-weight">
                <span class="weight-label">考勤成绩比例（1-100）：</span>
                <el-input-number
                  v-model="course.attendance_weight"
                  :min="1"
                  :max="100"
                  placeholder="输入考勤比例"
                />
              </div>
  
              <!-- 考勤记录表格（学生ID+成绩输入+删除按钮） -->
              <el-table :data="course.students" style="width: 100%">
                <el-table-column label="学生ID" width="150">
                  <template #default="{ row }">
                    <el-input v-model="row.student_id" placeholder="输入学生ID" />
                  </template>
                </el-table-column>
                <el-table-column label="考勤成绩" width="150">
                  <template #default="{ row }">
                    <el-input-number v-model="row.score" :min="0" :max="100" placeholder="输入成绩（0-100）" />
                  </template>
                </el-table-column>
                <!-- 新增：操作列（删除按钮） -->
                <el-table-column label="操作" width="100">
                  <template #default="{ row }">
                    <el-button 
                      type="danger" 
                      size="small" 
                      @click="deleteStudent(course, row)"
                    >
                      删除
                    </el-button>
                  </template>
                </el-table-column>
              </el-table>

              <el-button type="primary" @click="addStudent(course)" style="margin-top: 20px;">
                添加学生
              </el-button>

              <el-button type="success" @click="submitAttendance(course)" style="margin-top: 20px;">
                提交考勤记录
              </el-button>
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
  const activeNames = ref(['0'])  // 默认展开第一个课程
  const courseList = ref([])

  const userId = inject('user_id')  // 从顶层注入教师ID
  
  // 获取课程列表
  const getCourseList = async () => {
      loading.value = true
      try {
          const res = await teacherAPI.getTeacherCourses(Number(userId.value))
          if (res.code === '200') {
              // 初始化课程数据结构
              courseList.value = res.data.map(courseName => ({
                  course_name: courseName,
                  attendance_weight: 20,  // 初始比例（1-100）
                  students: []  // 直接存储学生列表
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

  // 新增：添加学生行方法
  const addStudent = (course) => {
    course.students.push({
      student_id: '',  // 初始空值（手动输入）
      score: null      // 初始空值（手动输入）
    })
    ElMessage.success('已添加新学生记录')
  }
  
  // 提交考勤记录
  const submitAttendance = async (course) => {
    if (course.students.length === 0) {
      ElMessage.warning('请至少添加一条考勤记录');
      return;
    }
    
    try {
      for (const student of course.students) {
        if (!student.student_id || !student.score) {
          throw new Error('请完善所有学生ID和成绩');
        }

        const res = await teacherAPI.processAttendanceRecord({
          studentId: Number(student.student_id),
          courseName: course.course_name,
          attendanceScore: student.score,  
          attendanceRatio: course.attendance_weight
        });
        if (!res.data) throw new Error('处理失败');
      }
      ElMessage.success('考勤记录处理成功');
    } catch (error) {
      ElMessage.error(`提交失败：${error.message}`);
    }
  };
  
  // 生命周期钩子
  onMounted(() => {
    if (userId.value) getCourseList()  // 确保有教师ID时加载
    else ElMessage.warning('未获取到教师信息')
  })
  
  // 新增：删除学生行方法
  const deleteStudent = (course, student) => {
    const index = course.students.findIndex(item => item === student);
    if (index !== -1) {
      course.students.splice(index, 1);  // 从数组中移除该行数据
      ElMessage.success('已删除该学生记录');
    }
  };
  </script>
  
  <style scoped>
  .attendance-container {
    padding: 20px;
    max-width: 1500px;
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
