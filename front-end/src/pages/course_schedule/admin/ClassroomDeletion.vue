<template>
  <div class="classroom-browse-admin container">
    <el-card class="form-card">
      <template #header>
        <div class="card-header">
          <h2>删除教室</h2>
          <p>请输入筛选条件查找要删除的教室</p>
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
            请输入筛选条件
          </h2>
        </div>
      </template>

      <el-table :data="classroomList" style="font-size: 15px;" empty-text="暂无教室数据">
        <el-table-column prop="id" label="教室 ID" />
        <el-table-column prop="location" label="位置" />
        <el-table-column prop="capacity" label="容量" />
        <el-table-column prop="category" label="类别" />
        <el-table-column label="操作">
          <template #default="scope">
            <el-button
              type="danger"
              @click="handleDelete(scope.$index, scope.row.id)"
              :loading="deleteLoading === scope.row.id"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import axios from 'axios';

const filterForm = reactive({
  id: '',
  location: '',
  capacity: '',
  category: '',
});

const filterLoading = ref(false);
const classroomList = ref([]);
const hasSearched = ref(false);
const deleteLoading = ref(null);

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

    if (Object.keys(queryParams).length === 0) {
      ElMessage.warning('请输入至少一个筛选条件');
      filterLoading.value = false;
      return;
    }

    const response = await axios.get('/arrange/api/classrooms/query', { // 假设存在查询接口
      params: queryParams,
    });

    if (response.data) { // 假设查询接口直接返回教室数组
      classroomList.value = response.data;
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

const resetFilterForm = () => {
  Object.keys(filterForm).forEach((key) => {
    filterForm[key] = '';
  });
  classroomList.value = [];
  hasSearched.value = false;
  ElMessage.success('筛选条件已重置');
};

const handleDelete = async (index, classroomId) => {
  ElMessageBox.confirm(
    `确定要删除教室 ID 为 ${classroomId} 的教室吗?`,
    '警告',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }
  )
    .then(async () => {
      deleteLoading.value = classroomId;
      try {
        await axios.delete(`/arrange/api/classrooms/${classroomId}`);
        ElMessage.success(`教室 ID ${classroomId} 删除成功`);
        classroomList.value.splice(index, 1); // 从列表中移除删除的教室
      } catch (error) {
        ElMessage.error(`删除教室 ID ${classroomId} 失败`);
        console.error('删除教室失败:', error);
      } finally {
        deleteLoading.value = null;
      }
    })
    .catch(() => {
      ElMessage({
        type: 'info',
        message: '已取消删除',
      });
    });
};
</script>

<style scoped>
.classroom-browse-admin {
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