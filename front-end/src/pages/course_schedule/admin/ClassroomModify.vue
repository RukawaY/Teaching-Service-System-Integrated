<template>
  <div class="classroom-modify container">
    <el-card class="form-card">
      <template #header>
        <div class="card-header">
          <h2>修改教室信息</h2>
          <p>请输入筛选条件查找要修改的教室</p>
        </div>
      </template>

      <el-form :model="filterForm" label-width="120px" class="input-form">
        <el-form-item label="教室 ID">
          <el-input v-model="filterForm.id" placeholder="请输入教室 ID" />
        </el-form-item>
        <el-form-item label="位置">
          <el-input v-model="filterForm.location" placeholder="请输入教室位置" />
        </el-form-item>
        <el-form-item label="容量">
          <el-input v-model.number="filterForm.capacity" placeholder="请输入教室容量" type="number" />
        </el-form-item>
        <el-form-item label="类别">
          <el-select v-model="filterForm.category" placeholder="请选择教室类别" clearable>
            <el-option label="普通" value="普通" />
            <el-option label="实验" value="实验" />
            <el-option label="体育" value="体育" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleFilter" :loading="filterLoading">
            筛选教室
          </el-button>
          <el-button @click="resetFilterForm">
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="classroom-card">
      <template #header>
        <div class="card-header">
          <h2 v-if="classroomList.length > 0">
            共查询到 {{ classroomList.length }} 间教室
          </h2>
          <h2 v-else-if="hasSearched">
            未查询到符合条件的教室
          </h2>
          <h2 v-else>
            请输入查询条件
          </h2>
        </div>
      </template>

      <el-table :data="classroomList" style="font-size: 15px;" empty-text="暂无教室数据">
        <el-table-column prop="id" label="教室 ID" />
        <el-table-column prop="location" label="位置" />
        <el-table-column prop="capacity" label="容量" />
        <el-table-column prop="category" label="类别" />
        <el-table-column label="操作" width="120">
          <template #default="scope">
            <el-button type="primary" size="small" @click="openEditDialog(scope.row)">
              修改
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog
      v-model="editDialogVisible"
      :title="`修改教室 - ${editFormData.id}`"
      width="600px"
    >
      <el-form :model="editFormData" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="教室 ID">
              <el-input v-model="editFormData.id" :disabled="true" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="位置">
              <el-input v-model="editFormData.location" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="容量">
              <el-input-number v-model="editFormData.capacity" :min="1" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="类别">
              <el-select v-model="editFormData.category" placeholder="请选择教室类别">
                <el-option label="普通" value="普通" />
                <el-option label="实验" value="实验" />
                <el-option label="体育" value="体育" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>

      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveEdit" :loading="editLoading">保存修改</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue';
import { ElMessage } from 'element-plus';
import axios from 'axios';

const filterForm = reactive({
  id: '',
  location: '',
  capacity: '',
  category: '',
});

const filterLoading = ref(false);
const hasSearched = ref(false);
const classroomList = ref([]);

// 编辑弹窗相关
const editDialogVisible = ref(false);
const editFormData = reactive({
  id: '',
  location: '',
  capacity: null,
  category: '',
});
const editLoading = ref(false);

// 获取教室信息 (用于筛选)
const handleFilter = async () => {
  filterLoading.value = true;
  hasSearched.value = true;
  classroomList.value = [];

  try {
    const queryParams = {};
    for (const key in filterForm) {
      if (filterForm[key] !== '') {
        queryParams[key] = filterForm[key];
      }
    }


    const response = await axios.get('/arrange/api/classrooms/query', { // 假设存在查询接口
      params: queryParams,
    });

    if (response.data) {
      classroomList.value = response.data; // 假设接口直接返回教室数组
      if (classroomList.value.length === 0) {
        ElMessage.info('未查询到符合条件的教室');
      }
    } else {
      ElMessage.error('获取教室列表失败');
    }
  } catch (error) {
    ElMessage.error('网络错误或服务器不可用');
    console.error('筛选教室失败:', error);
  } finally {
    filterLoading.value = false;
  }
};

// 重置筛选条件
const resetFilterForm = () => {
  Object.keys(filterForm).forEach((key) => {
    filterForm[key] = '';
  });
  classroomList.value = [];
  hasSearched.value = false;
  ElMessage.success('筛选条件已重置');
};

// 打开编辑弹窗
const openEditDialog = (row) => {
  Object.assign(editFormData, { ...row }); // 复制对象属性
  editDialogVisible.value = true;
};

// 保存编辑信息
const saveEdit = async () => {
  editLoading.value = true;
  try {
    const response = await axios.put(`/arrange/api/classrooms/${editFormData.id}`, {
      location: editFormData.location,
      capacity: editFormData.capacity,
      category: editFormData.category,
    });

    if (response.status === 200) { // 假设成功返回 200
      ElMessage.success(`教室 ID ${editFormData.id} 信息修改成功`);
      editDialogVisible.value = false;
      // 更新前端列表
      const index = classroomList.value.findIndex(
        item => item.id === editFormData.id
      );
      if (index !== -1) {
        classroomList.value[index] = { ...editFormData };
      }
    } else {
      ElMessage.error(`修改教室 ID ${editFormData.id} 信息失败`);
    }
  } catch (error) {
    ElMessage.error('网络错误或服务器不可用');
    console.error('修改教室信息失败:', error);
  } finally {
    editLoading.value = false;
  }
};
</script>

<style scoped>
.classroom-modify {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.container {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.form-card {
  width: 95%;
  margin: 0px auto 20px auto;
}

.card-header {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.input-form {
  max-width: 500px;
  margin: 0 auto;
}

.classroom-card {
  width: 95%;
  margin: 0px auto 20px auto;
}
</style>