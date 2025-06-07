import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080';  // 端口改为8081

const api = axios.create({
    baseURL: API_BASE_URL,
    headers: {
        'Content-Type': 'application/json',
    },
});

// 请求拦截器
api.interceptors.request.use(
    (config) => {
        return config;
    },
    (error) => {
        return Promise.reject(error);
    }
);

// 响应拦截器
api.interceptors.response.use(
    (response) => {
        return response;
    },
    (error) => {
        return Promise.reject(error);
    }
);

// 通用响应接口
interface ApiResponse<T = any> {
    code: string;
    message: string;
    data?: T;
}

// 作业相关接口
interface Homework {
    homework_id: number;
    course_id: number;
    title: string;
    description: string;
    deadline: string;
    isSubmitted?: boolean;
    selectedFile?: File | null;
}

interface HomeworkSubmission {
    homework_id: number;
    student_id: number;
    file: File;
}

interface HomeworkListResponse {
    code: string;
    message: string;
    data: {
        homework_list: Homework[];
    }
}

// 资源相关接口
interface Resource {
    resource_id: number;
    student_id: number;
    course_id: number;
    resource_name: string;
    resource_description: string;
    upload_time: string;
    file_url: string;
}

interface ResourceQuery {
    course_id?: number;
    student_id?: number;
}

interface ResourceListResponse {
    code: string;
    message: string;
    data: {
        resource_list: Resource[];
    }
}

interface UploadResourceParams {
    student_id: number;
    course_id: number;
    resource_name: string;
    resource_description: string;
    file: File;
}

// 课程相关接口
interface Course {
    course_id: number;
    course_name: string;
    course_description: string;
    teacher_name: string;
    class_time: string;
    classroom: string;
    available_capacity: number;
    total_capacity: number;
    credit: number;
}

interface CourseQuery {
    course_name?: string;
    teacher_name?: string;
    course_id?: number;
    student_id?: number;
    need_available?: boolean;
}

interface CourseListResponse {
    code: string;
    message: string;
    data: {
        course_list: Course[];
    }
}

// 错误处理函数
const handleError = (error: unknown) => {
    if (axios.isAxiosError(error)) {
        console.error('API Error:', error.response?.data || error.message);
        throw new Error(error.response?.data?.message || 'Network error');
    } else {
        console.error('Unexpected Error:', error);
        throw new Error('An unexpected error occurred');
    }
}

// 作业相关函数
export const getHomeworkList = async (courseId: number): Promise<HomeworkListResponse> => {
    try {
        const response = await api.get('/api/student/get_homework_list', {
            params: { course_id: courseId }
        });
        return response.data;
    } catch (error) {
        handleError(error);
        throw error;
    }
}

export const submitHomework = async (submission: HomeworkSubmission): Promise<ApiResponse> => {
    try {
        const formData = new FormData();
        formData.append('homework_id', submission.homework_id.toString());
        formData.append('student_id', submission.student_id.toString());
        formData.append('file', submission.file);

        const response = await api.post('/api/student/submit_homework', formData, {
            headers: {
                'Content-Type': 'multipart/form-data',
            },
        });
        return response.data;
    } catch (error) {
        handleError(error);
        throw error;
    }
}

// 资源相关函数
export const uploadResource = async (params: UploadResourceParams): Promise<ApiResponse> => {
    try {
        const formData = new FormData();
        formData.append('student_id', params.student_id.toString());
        formData.append('course_id', params.course_id.toString());
        formData.append('resource_name', params.resource_name);
        formData.append('resource_description', params.resource_description);
        formData.append('file', params.file);

        const response = await api.post('/api/student/upload_resource', formData, {
            headers: {
                'Content-Type': 'multipart/form-data',
            },
        });
        return response.data;
    } catch (error) {
        handleError(error);
        throw error;
    }
}

export const getResources = async (params: ResourceQuery): Promise<ResourceListResponse> => {
    try {
        const response = await api.get('/api/student/get_resources', { params });
        return response.data;
    } catch (error) {
        handleError(error);
        throw error;
    }
}

export const downloadResource = async (resourceId: number): Promise<Blob> => {
    try {
        const response = await api.get(`/api/student/download_resource/${resourceId}`, {
            responseType: 'blob'
        });
        return response.data;
    } catch (error) {
        handleError(error);
        throw error;
    }
}

// 新增：资源目录类型定义（匹配后端 ResourceDirectory 实体）
interface ResourceDirectory {
    directoryId: number;
    parentId: number;
    directoryName: string;
    ownerId: number;
    courseId: number;
    createTime: string;
    children?: ResourceDirectory[];  // 可选子目录字段（根据后端可能的嵌套结构）
}

// 获取课程资源目录
export const getResourceDirectory = async (courseId: number): Promise<ApiResponse<ResourceDirectory[]>> => {
    try {
        const response = await api.get(`/resource/directory/${courseId}`);
        return response.data;
    } catch (error) {
        return handleError(error) as ApiResponse<ResourceDirectory[]>;  // 复用统一错误处理
    }
};

// 创建资源目录
export const createResourceDirectory = async (params: {
    parentId: number;
    directoryName: string;
    ownerId: number;
    courseId: number;
}): Promise<ApiResponse<ResourceDirectory>> => {
    try {
        const response = await api.post('/resource/directory', params);
        return response.data;
    } catch (error) {
        return handleError(error) as ApiResponse<ResourceDirectory>;
    }
};

// 上传资源文件
export const uploadResourceFile = async (formData: FormData): Promise<ApiResponse> => {
    try {
        const response = await api.post('/resource/upload', formData, {
            headers: { 'Content-Type': 'multipart/form-data' }
        });
        return response.data;
    } catch (error) {
        return handleError(error) as ApiResponse;
    }
};

// 下载资源文件
export const downloadResourceFile = async (resourceId: number): Promise<Blob> => {
    try {
        const response = await api.get(`/resource/download/${resourceId}`, {
            responseType: 'blob'
        });
        return response.data;
    } catch (error) {
        handleError(error);
        throw error;
    }
};

// 课程相关函数
export const getCourseList = async (params: CourseQuery): Promise<CourseListResponse> => {
    try {
        const response = await api.get('/api/student/get_course_list', { params });
        return response.data;
    } catch (error) {
        handleError(error);
        throw error;
    }
}

export const getCourseDetail = async (courseId: number): Promise<ApiResponse<Course>> => {
    try {
        const response = await api.get(`/api/student/get_course_detail/${courseId}`);
        return response.data;
    } catch (error) {
        handleError(error);
        throw error;
    }
}

// 导出所有 API 函数
export const studentAPI = {
    // 作业相关
    getHomeworkList,
    submitHomework,
    
    // 资源相关
    uploadResource,
    getResources,
    downloadResource,
    getResourceDirectory,
    createResourceDirectory,
    uploadResourceFile,
    downloadResourceFile,

    // 课程相关
    getCourseList,
    getCourseDetail
}

export default studentAPI;

