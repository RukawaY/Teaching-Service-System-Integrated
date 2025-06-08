<template>
  <div class="show-result-teacher container">
    <el-card class="result-card">
      <template #header>
        <div class="card-header">
          <h2>教师选课情况</h2>
        </div>
      </template>

      <p v-if="!loading && tableData.length > 0 && currentFetchedTeacherId" style="text-align: center; margin-bottom: 15px;">
        以下是您的所授课程学生选课情况 </p>

      <el-table :data="tableData" style="font-size: 15px;" v-loading="loading">
        <el-table-column prop="course_name" label="课程名称" />
        <el-table-column prop="course_time" label="上课时间">
          <template #default="scope">
            {{ reflectTime(scope.row.course_time) }}
          </template>
        </el-table-column>
        <el-table-column prop="course_classroom" label="上课教室" />
        <el-table-column label="学生名单">
          <template #default="scope">
            <div v-if="scope.row.student_list && scope.row.student_list.length" style="display: flex; flex-wrap: wrap; gap: 5px;">
              <el-tag v-for="stu in scope.row.student_list" :key="stu.ID">
                {{ stu.name }}
              </el-tag>
            </div>
            <div v-else>暂无学生</div>
          </template>
        </el-table-column>
      </el-table>

      <div v-if="hasFetched && tableData.length > 0 && !loading" style="text-align: center; margin-top: 20px; margin-bottom: 10px;">
        <el-button type="success" @click="openTimetableDialog">
          查看/打印课表
        </el-button>
      </div>

      <el-empty v-if="!loading && tableData.length === 0 && hasFetched && currentFetchedTeacherId" description="该教师暂无课程选课数据或ID对应教师不存在" />
      <el-empty v-if="!loading && !currentFetchedTeacherId && !hasFetched" description="正在等待教师信息加载..." />
    </el-card>

    <el-dialog v-model="timetableDialogVisible" title="教师课表" width="85%" top="5vh">
      <div id="timetable-to-print" style="padding: 10px;">
        <h3 style="text-align: center; margin-bottom: 15px;"> {{ user_name }}的课表 </h3>
        <el-table
          :data="timetableForDisplay"
          border
          style="width: 100%; font-size: 14px;"
          :span-method="tableSpanMethod"
        >
          <el-table-column prop="timeLabel" label="时间" width="110" align="center" header-align="center"/>
          <el-table-column
            v-for="day in daysOfWeekForTable"
            :key="day"
            :label="day"
            min-width="100"
            align="center"
            header-align="center"
          >
            <template #default="scope">
              <div v-if="scope.row[day] && scope.row[day].isStartSlot"
                style="min-height: 30px; display: flex; flex-direction: column; align-items: center; justify-content: center; word-break: break-all; padding: 2px; line-height: 1.4;">
                <span style="font-size: 1.2em; font-weight: bold;">{{ scope.row[day].name }}</span>
                <span v-if="scope.row[day].classroom" style="font-size: 1em; color: #555;">{{ scope.row[day].classroom }}</span>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="timetableDialogVisible = false">关闭</el-button>
          <el-button type="primary" @click="printTimetable" :loading="printingTimetable">
            下载课表 (PDF)
          </el-button>
        </span>
      </template>
    </el-dialog>

  </div>
</template>

<script setup>
import { ref, inject, watch } from 'vue';
import { ElMessage } from 'element-plus';
import { teacherAPI } from '../../../api/course_selection/teacher';
import jsPDF from 'jspdf';
import html2canvas from 'html2canvas';

const injectedTeacherId = inject('user_id');
const user = inject('user');
const user_name = inject('user_name');

const currentFetchedTeacherId = ref('');
const tableData = ref([]);
const loading = ref(false);
const hasFetched = ref(false);
const timetableDialogVisible = ref(false);
const printingTimetable = ref(false);
const timetableGrid = ref({});
const timetableForDisplay = ref([]);

const daysOfWeekForTable = ['周一', '周二', '周三', '周四', '周五', '周六', '周日'];
const timeSlotLabels = [
  ...Array(5).fill(0).map((_, i) => `上午第${i+1}节`),
  ...Array(5).fill(0).map((_, i) => `下午第${i+1}节`),
  ...Array(3).fill(0).map((_, i) => `晚上第${i+1}节`),
];

const mapEnglishDayToChinese = (classTime) => {
  if (!classTime || typeof classTime !== 'string') {
      return classTime;
  }
  const mapping = {
    "Monday": "周一",
    "Tuesday": "周二",
    "Wednesday": "周三",
    "Thursday": "周四",
    "Friday": "周五",
    "Saturday": "周六",
    "Sunday": "周日"
  };

  const entries = classTime.split(';');
  const chineseEntries = entries.map(entry => {
      const parts = entry.trim().match(/^([A-Za-z]+)\s*(.*)$/);
      if (parts && parts.length === 3) {
          const dayEng = parts[1];
          const slots = parts[2];
          const normalizedDayEng = dayEng.charAt(0).toUpperCase() + dayEng.slice(1).toLowerCase();
          const dayChinese = mapping[normalizedDayEng];
          return dayChinese ? `${dayChinese} ${slots}` : entry;
      }
      return entry;
  });
  return chineseEntries.join('; ');
};

const reflectTime = (time) => {
  if (!time) return '';
  const timeArray = time.split(';');
  if (timeArray.length === 0) return '';
  const firstTime = timeArray[0].trim();
  const lastTime = timeArray[timeArray.length - 1].trim();

  const day = firstTime.split(' ')[0];
  const firstSlot = firstTime.split(' ')[1];
  const lastSlot = lastTime.split(' ')[1];

  if (firstSlot && lastSlot && firstSlot !== lastSlot) {
    return `${day} ${firstSlot}-${lastSlot}节`;
  }
  return `${day} ${firstSlot}节`;
}

const initializeTimetableGrid = () => {
  const newGrid = {};
  daysOfWeekForTable.forEach(day => {
    newGrid[day] = {
      '上午': Array(5).fill(null),
      '下午': Array(5).fill(null),
      '晚上': Array(3).fill(null)
    };
  });
  timetableGrid.value = newGrid;
};

const parseAndPlaceCourses = () => {
  initializeTimetableGrid();
  if (!tableData.value || tableData.value.length === 0) return;

  const _placeCourseSegmentOnGrid = (dayOfWeek, startSlotNum, duration, courseInfo) => {
    const courseData = {
      name: courseInfo.course_name,
      classroom: courseInfo.course_classroom,
      isStartSlot: true,
      duration: duration
    };
    const subsequentCourseData = {
      name: courseInfo.course_name,
      classroom: courseInfo.course_classroom,
      isStartSlot: false,
      duration: 0
    };
    for (let i = 0; i < duration; i++) {
      const slotNum = startSlotNum + i;
      let periodKey, slotIndexInPeriod;

      if (slotNum >= 1 && slotNum <= 5) {
        periodKey = '上午';
        slotIndexInPeriod = slotNum - 1;
      } else if (slotNum >= 6 && slotNum <= 10) {
        periodKey = '下午';
        slotIndexInPeriod = slotNum - 6;
      } else if (slotNum >= 11 && slotNum <= 13) {
        periodKey = '晚上';
        slotIndexInPeriod = slotNum - 11;
      } else {
        console.error(`Invalid slot number: ${slotNum} for course ${courseInfo.course_name} on ${dayOfWeek}`);
        continue;
      }

      if (timetableGrid.value[dayOfWeek] && timetableGrid.value[dayOfWeek][periodKey]) {
        if (timetableGrid.value[dayOfWeek][periodKey][slotIndexInPeriod] !== null) {
          console.warn(`课程冲突: ${dayOfWeek} ${periodKey} 第${slotIndexInPeriod+1}节已有课程`);
        }
        if (i === 0) {
          timetableGrid.value[dayOfWeek][periodKey][slotIndexInPeriod] = courseData;
        } else {
          timetableGrid.value[dayOfWeek][periodKey][slotIndexInPeriod] = subsequentCourseData;
        }
      } else {
        console.error(`Timetable grid slot not found for ${dayOfWeek}, ${periodKey}, slotIndex ${slotIndexInPeriod}`);
      }
    }
  };

  tableData.value.forEach(course => {
    if (!course.course_time || typeof course.course_time !== 'string' || course.course_time.trim() === '') {
      return;
    }

    const slotsByDay = {};
    const timeEntries = course.course_time.split(';');

    timeEntries.forEach(entryStr => {
      entryStr = entryStr.trim();
      if (!entryStr) return;

      const parts = entryStr.split(/\s+/);
      if (parts.length === 2) {
        const dayEngOrChinese = parts[0];
        const slotStr = parts[1];
        const slotNum = parseInt(slotStr);
        
        const dayChinese = mapEnglishDayToChinese(dayEngOrChinese).split(' ')[0];

        if (dayChinese && daysOfWeekForTable.includes(dayChinese) && !isNaN(slotNum) && slotNum >= 1 && slotNum <= 13) {
          if (!slotsByDay[dayChinese]) {
            slotsByDay[dayChinese] = [];
          }
          slotsByDay[dayChinese].push(slotNum);
        } else {
          console.error(`Failed to parse entry: "${entryStr}". Day: ${dayEngOrChinese} (mapped to ${dayChinese}), Slot: ${slotStr} (parsed to ${slotNum})`);
        }
      } else {
        console.error(`Invalid format for time entry: "${entryStr}". Expected format "Day Slot".`);
      }
    });

    for (const dayOfWeek in slotsByDay) {
      const individualSlots = slotsByDay[dayOfWeek].sort((a, b) => a - b);
      if (individualSlots.length === 0) continue;

      let currentDuration = 0;
      let lastSlot = -1;
      let firstSlotInSequence = -1;
      individualSlots.forEach((slotNum, index) => {
        if (firstSlotInSequence === -1) {
          firstSlotInSequence = slotNum;
          currentDuration = 1;
        } else if (slotNum === lastSlot + 1) {
          currentDuration++;
        } else {
          _placeCourseSegmentOnGrid(dayOfWeek, firstSlotInSequence, currentDuration, course);
          firstSlotInSequence = slotNum;
          currentDuration = 1;
        }
        lastSlot = slotNum;

        if (index === individualSlots.length - 1) {
          _placeCourseSegmentOnGrid(dayOfWeek, firstSlotInSequence, currentDuration, course);
        }
      });
    }
  });
};

const prepareTimetableForDisplay = () => {
  const displayData = [];
  const slotPeriods = ['上午', '下午', '晚上'];
  const slotsInPeriodDefinition = [5, 5, 3];
  let labelIndex = 0;
  slotPeriods.forEach((periodKey, pIdx) => {
    for (let i = 0; i < slotsInPeriodDefinition[pIdx]; i++) {
      const row = { timeLabel: timeSlotLabels[labelIndex++] };
      daysOfWeekForTable.forEach(day => {
        row[day] = timetableGrid.value[day]?.[periodKey]?.[i] || null;
      });
      displayData.push(row);
    }
  });
  timetableForDisplay.value = displayData;
};

const tableSpanMethod = ({ row, column, rowIndex, columnIndex }) => {
  if (columnIndex === 0) {
    return [1, 1];
  }
  const dayKey = daysOfWeekForTable[columnIndex - 1];
  const cellData = row[dayKey];
  if (cellData) {
    if (cellData.isStartSlot) {
      return { rowspan: cellData.duration, colspan: 1 };
    }
    return { rowspan: 0, colspan: 0 };
  }
  return [1, 1];
};

const openTimetableDialog = () => {
  if (!hasFetched.value || tableData.value.length === 0) {
    ElMessage.info('没有可用于生成课表的课程数据。');
    return;
  }
  parseAndPlaceCourses();
  prepareTimetableForDisplay();
  timetableDialogVisible.value = true;
};

const printTimetable = async () => {
  if (printingTimetable.value) return;
  printingTimetable.value = true;
  try {
    const timetableElement = document.getElementById('timetable-to-print');
    if (!timetableElement) {
      ElMessage.error('找不到课表元素进行打印。');
      printingTimetable.value = false;
      return;
    }
    const canvas = await html2canvas(timetableElement, {
      scale: 2.5,
      useCORS: true,
      logging: false,
      backgroundColor: '#ffffff',
      onclone: (documentClone) => {
        const clonedTable = documentClone.getElementById('timetable-to-print');
        if (clonedTable) {
           const tables = clonedTable.querySelectorAll('table');
            tables.forEach(table => {
                table.style.borderCollapse = 'collapse';
                const cells = table.querySelectorAll('td, th');
                cells.forEach(cell => {
                   cell.style.border = '1px solid #ebeef5';
                });
            });
        }
      }
    });

    const imgData = canvas.toDataURL('image/png');
    const pdf = new jsPDF({
      orientation: 'landscape',
      unit: 'pt',
      format: 'a4'
    });
    const imgProps = pdf.getImageProperties(imgData);
    const pdfWidth = pdf.internal.pageSize.getWidth();
    const pdfHeight = pdf.internal.pageSize.getHeight();

    const margin = 20;
    const availableWidth = pdfWidth - 2 * margin;
    const availableHeight = pdfHeight - 2 * margin;

    let imgWidth = imgProps.width;
    let imgHeight = imgProps.height;
    const ratio = imgWidth / imgHeight;
    if (imgWidth > availableWidth) {
      imgWidth = availableWidth;
      imgHeight = imgWidth / ratio;
    }
    if (imgHeight > availableHeight) {
      imgHeight = availableHeight;
      imgWidth = imgHeight * ratio;
    }

    const xOffset = margin + (availableWidth - imgWidth) / 2;
    const yOffset = margin + (availableHeight - imgHeight) / 2;

    pdf.addImage(imgData, 'PNG', xOffset, yOffset, imgWidth, imgHeight);
    pdf.save(`课表-${user_name.value}-${currentFetchedTeacherId.value || '未知'}.pdf`);
    ElMessage.success('课表PDF已开始下载');
  } catch (error) {
    console.error('打印课表失败:', error);
    ElMessage.error('打印课表失败，详情请查看控制台。');
  } finally {
    printingTimetable.value = false;
  }
};

const fetchResult = async () => {
  if (!injectedTeacherId.value) {
    ElMessage.warning('教师ID无效，请确保已登录或ID已正确注入。');
    tableData.value = [];
    hasFetched.value = false;
    currentFetchedTeacherId.value = '';
    return;
  }
  loading.value = true;
  hasFetched.value = true;
  currentFetchedTeacherId.value = injectedTeacherId.value;

  try {
    const response = await teacherAPI.getTeacherCourseResults(Number(injectedTeacherId.value));
    if (response.code === '200' || response.code === 200 || response.code === 'success') {
      if (response.data && response.data.course_list) {
        tableData.value = response.data.course_list.map(item => {
          let studentListArray = [];
          if (item.student_list) {
            if (Array.isArray(item.student_list)) {
               studentListArray = item.student_list;
            } else {
              studentListArray = [item.student_list];
            }
          }
          return {
            ...item,
            course_time: mapEnglishDayToChinese(item.course_time),
            student_list: studentListArray
          };
        });
      } else {
        tableData.value = [];
      }
    } else {
      ElMessage.error(response.message || '获取选课数据失败');
      tableData.value = [];
    }
  } catch (error) {
    ElMessage.error(error.message || '网络错误，无法获取选课数据');
    tableData.value = [];
    console.error('获取教师选课数据时出错:', error);
  } finally {
    loading.value = false;
    if (tableData.value.length === 0) {
        timetableDialogVisible.value = false;
    }
  }
};

watch(injectedTeacherId, (newId) => {
  if (newId) {
    fetchResult();
  } else {
    tableData.value = [];
    currentFetchedTeacherId.value = '';
    hasFetched.value = false;
    timetableDialogVisible.value = false;
  }
}, { immediate: true });

</script>

<style scoped>
.show-result-teacher {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}
.container {
  display: flex;
  flex-direction: column;
  gap: 20px;
}
.result-card {
  width: 95%;
  margin: 0px auto 20px auto;
}
.card-header {
  display: flex;
  flex-direction: column;
  align-items: center;
}
::v-deep .el-dialog__body {
  padding: 10px 20px;
}
</style>