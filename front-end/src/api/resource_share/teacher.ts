import axios, { AxiosResponse } from 'axios'

// 配置axios
const request = axios.create({
  baseURL: 'http://localhost:8080',  // 端口改为8080
  timeout: 5000
})

// 请求拦截器
request.interceptors.request.use(
  config => {
    // 从localStorage获取token
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  (response: AxiosResponse) => {
    // 如果是blob类型，直接返回
    if (response.config.responseType === 'blob') {
      return response
    }
    
    // 构造符合ApiResponse接口的响应数据
    const apiResponse = {
      ...response,
      data: {
        code: response.data.code || '200',
        message: response.data.message || 'success',
        data: response.data.data || response.data
      }
    }
    return apiResponse
  },
  error => {
    return Promise.reject(error)
  }
)

// 通用响应接口
interface ApiResponse<T> {
  code: string
  message: string
  data: T
}

// 作业相关接口
interface Homework {
  homework_id: string
  course_id: string
  title: string
  description: string
  deadline: string
  weight: number
  requirements: string
}

// 作业提交相关接口
interface HomeworkSubmission {
  submission_id: number
  homework_id: number
  student_id: string
  student_name: string
  submit_time: string
  file_name: string
  file_url: string
  score: number | null
  comment: string | null
}

// 课程作业列表接口
interface CourseHomework {
  course_id: number
  course_name: string
  homework_list: Homework[]
}

// 评分提交参数接口
interface GradingParams {
  submission_id: number
  score: number
  comment: string
}

// 作业成绩比例接口
interface HomeworkWeight {
  homework_id: number
  weight: number  // 比例，例如 0.3 表示占 30%
}


// 获取所有课程的作业列表
export const getHomeworkList = async (): Promise<ApiResponse<CourseHomework[]>> => {
  try {
    const response = await request.get('/api/teacher/homework_list')
    return response.data
  } catch (error) {
    throw new Error('获取作业列表失败')
  }
}

// 获取特定作业的提交列表
export const getHomeworkSubmissions = async (homeworkId: number): Promise<ApiResponse<HomeworkSubmission[]>> => {
  try {
    const response = await request.get(`/api/teacher/homework/${homeworkId}/submissions`)
    return response.data
  } catch (error) {
    throw new Error('获取作业提交列表失败')
  }
}

// 下载作业文件
export const downloadHomework = async (submissionId: number): Promise<Blob> => {
  try {
    const response = await request.get(`/api/teacher/submission/${submissionId}/download`, {
      responseType: 'blob'
    })
    return response.data
  } catch (error) {
    throw new Error('下载作业失败')
  }
}

// 提交评分
export const submitGrading = async (params: GradingParams): Promise<ApiResponse<null>> => {
  try {
    const response = await request.post('/api/teacher/submit_grading', params)
    return response.data
  } catch (error) {
    throw new Error('提交评分失败')
  }
}

// 获取作业统计信息
export const getHomeworkStats = async (homeworkId: number): Promise<ApiResponse<{
  submitted_count: number
  total_count: number
  average_score: number
  score_distribution: {
    [key: string]: number
  }
}>> => {
  try {
    const response = await request.get(`/api/teacher/homework/${homeworkId}/stats`)
    return response.data
  } catch (error) {
    throw new Error('获取作业统计信息失败')
  }
}

// 批量导出成绩
export const exportGrades = async (homeworkId: number): Promise<Blob> => {
  try {
    const response = await request.get(`/api/teacher/homework/${homeworkId}/export_grades`, {
      responseType: 'blob'
    })
    return response.data
  } catch (error) {
    throw new Error('导出成绩失败')
  }
}

// 布置新作业
export const assignHomework = async (params: Homework): Promise<ApiResponse<null>> => {
  try {
    const response = await request.post('/api/teacher/homework', params)
    return response.data
  } catch (error) {
    throw new Error('布置作业失败1')
  }
}
// 获取作业成绩比例
export const getHomeworkWeight = async (homeworkId: number): Promise<ApiResponse<number>> => {
  try {
    const response = await request.get(`/api/teacher/homework/${homeworkId}/weight`)
    return response.data
  } catch (error) {
    throw new Error('获取作业成绩比例失败')
  }
}


// 新增：处理考勤成绩接口参数（与后端AttendanceComponentDTO完全匹配）
interface ProcessAttendanceParams {
  studentId: number;          // 对应后端student_id
  courseName: string;         // 对应后端course_name
  attendanceScore: number;    // 新增：对应后端attendance_score（考勤得分）
  attendanceRatio: number;    // 原weight改为attendanceRatio，对应后端attendance_ratio（考勤比例）
}

// 新增：获取教师课程列表接口（保持原逻辑，路径与后端@GetMapping("/courses/teacher")匹配）
export const getTeacherCourses = async (teacherId: number): Promise<ApiResponse<string[]>> => {
  try {
    const response = await request.get(`/api/attendance/courses/teacher?teacherId=${teacherId}`);
    return response.data;
  } catch (error) {
    throw new Error('获取教师课程列表失败');
  }
};

// 处理考勤成绩接口（修正参数，与后端POST /api/attendance/process匹配）
export const processAttendanceRecord = async (params: ProcessAttendanceParams): Promise<ApiResponse<boolean>> => {
  try {
    const response = await request.post('/api/attendance/process', params);  // 直接传递完整参数对象
    return response.data;
  } catch (error) {
    throw new Error('处理考勤记录失败');
  }
};

// 调整导出接口（移除旧接口，添加新接口）
export const teacherAPI = {
  getHomeworkList,
  getHomeworkSubmissions,
  downloadHomework,
  submitGrading,
  getHomeworkStats,
  exportGrades,
  assignHomework,
  getHomeworkWeight,
  getTeacherCourses,
  processAttendanceRecord,  
};

export default teacherAPI
