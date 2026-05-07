<template>
  <div class="booking-form">

    <div class="form-section">
      <h3 class="section-title">
        <span class="icon-box orange">📝</span>
        <span>活动详情</span>
      </h3>

      <div class="form-item">
        <label class="form-label">活动名称 <span class="required">*</span></label>
        <input v-model="modelValue.activityName" type="text" class="form-input" placeholder="请输入活动主题" @blur="handleBlur('activityName')" @input="handleInput('activityName')" />
        <span class="error-msg" v-if="touched.activityName && errors.activityName">{{ errors.activityName }}</span>
      </div>

      <div class="grid-row">
        <div class="form-item">
          <label class="form-label">申请部门 <span class="required">*</span></label>
          <input v-model="modelValue.department" type="text" class="form-input" placeholder="班级/社团/部门" @blur="handleBlur('department')" @input="handleInput('department')" />
          <span class="error-msg" v-if="touched.department && errors.department">{{ errors.department }}</span>
        </div>

        <div class="form-item">
          <label class="form-label">活动人数 <span class="required">*</span></label>
          <div class="stepper-input">
            <button class="stepper-btn minus" @click="updateAttendees(-1)" :disabled="modelValue.attendees <= 1">
              <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="5" y1="12" x2="19" y2="12"></line></svg>
            </button>
            <input v-model.number="modelValue.attendees" type="tel" class="form-input number-input" @blur="handleBlur('attendees')" @input="handleInput('attendees')" />
            <button class="stepper-btn plus" @click="updateAttendees(1)">
              <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="12" y1="5" x2="12" y2="19"></line><line x1="5" y1="12" x2="19" y2="12"></line></svg>
            </button>
          </div>
          <span class="error-msg" v-if="touched.attendees && errors.attendees">{{ errors.attendees }}</span>
        </div>
      </div>

      <div class="form-item">
        <div class="toggle-card" :class="{ active: modelValue.needProjection }" @click="toggleProjection">
          <div class="toggle-left-group">
            <div class="toggle-icon">
              <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="2" y="3" width="20" height="14" rx="2" ry="2"></rect><line x1="8" y1="21" x2="16" y2="21"></line><line x1="12" y1="17" x2="12" y2="21"></line></svg>
            </div>
            <div class="toggle-content">
              <div class="toggle-title">多媒体投屏</div>
              <div class="toggle-desc">是否使用投影仪/电子屏</div>
            </div>
          </div>
          <div class="toggle-switch">
            <div class="switch-handle"></div>
          </div>
        </div>
      </div>

      <div class="grid-row">
        <div class="form-item">
          <label class="form-label">指导老师</label>
          <input v-model="modelValue.teacherName" type="text" class="form-input" placeholder="选填" />
        </div>
        <div class="form-item">
          <label class="form-label">老师电话</label>
          <input v-model="modelValue.teacherContact" type="tel" class="form-input" placeholder="选填" maxlength="11" />
        </div>
      </div>
      <div class="form-item">
        <label class="form-label">其他需求</label>
        <textarea v-model="modelValue.otherRequirements" class="form-textarea" placeholder="如有其他设备需求请在此说明..." rows="3"></textarea>
      </div>
    </div>

    <div class="divider"></div>

    <div class="form-section">
      <h3 class="section-title">
        <span class="icon-box blue">👤</span>
        <span>个人信息</span>
      </h3>

      <div class="grid-row">
        <div class="form-item">
          <label class="form-label">姓名 <span class="required">*</span></label>
          <div class="input-wrapper">
            <input v-model="modelValue.userName" type="text" class="form-input" placeholder="您的姓名" @blur="handleBlur('userName')" @input="handleInput('userName')" />
          </div>
          <span class="error-msg" v-if="touched.userName && errors.userName">{{ errors.userName }}</span>
        </div>

        <div class="form-item">
          <label class="form-label">学号 <span class="required">*</span></label>
          <div class="input-wrapper">
            <input v-model="modelValue.studentId" type="text" class="form-input" placeholder="您的学号" @blur="handleBlur('studentId')" @input="handleInput('studentId')" />
          </div>
          <span class="error-msg" v-if="touched.studentId && errors.studentId">{{ errors.studentId }}</span>
        </div>
      </div>

      <div class="grid-row">
        <div class="form-item">
          <label class="form-label">所属学院 <span class="required">*</span></label>
          <div class="select-wrapper">
            <select
                v-model="modelValue.college"
                class="form-input form-select"
                :class="{ 'placeholder-color': !modelValue.college }"
                @blur="handleBlur('college')"
                @change="handleInput('college')"
            >
              <option disabled value="">请选择学院</option>
              <option v-for="opt in collegeOptions" :key="opt.value" :value="opt.value">
                {{ opt.label }}
              </option>
            </select>
            <div class="select-arrow">
              <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="6 9 12 15 18 9"></polyline></svg>
            </div>
          </div>
          <span class="error-msg" v-if="touched.college && errors.college">{{ errors.college }}</span>
        </div>

        <div class="form-item">
          <label class="form-label">年级专业 <span class="required">*</span></label>
          <input
              v-model="modelValue.major"
              type="text"
              class="form-input"
              placeholder="如：22级软件工程"
              @blur="handleBlur('major')"
              @input="handleInput('major')"
          />
          <span class="error-msg" v-if="touched.major && errors.major">{{ errors.major }}</span>
        </div>
      </div>

      <div class="form-item">
        <label class="form-label">联系方式 <span class="required">*</span></label>
        <div class="input-wrapper">
          <input v-model="modelValue.contact" type="tel" class="form-input" placeholder="请输入手机号码" maxlength="11" @blur="handleBlur('contact')" @input="handleInput('contact')" />
        </div>
        <span class="error-msg" v-if="touched.contact && errors.contact">{{ errors.contact }}</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, watch, ref } from 'vue'; // 🟢 引入 ref

const props = defineProps({
  modelValue: { type: Object, required: true, default: () => ({}) },
  userInfo: { type: Object, default: () => ({}) }
});

const emit = defineEmits(['update:modelValue', 'form-validity-change']);

// 🟢 学院选项数据
const collegeOptions = ref([
  { value: '信息工程学院', label: '信息工程学院' },
  { value: '教育学院', label: '教育学院' },
  { value: '数理学院', label: '数理学院' },
  { value: '生物科学与工程学院', label: '生物科学与工程学院' },
  { value: '海洋学院', label: '海洋学院' },
  { value: '新能源与材料学院', label: '新能源与材料学院' },
  { value: '机电工程学院', label: '机电工程学院' },
  { value: '经济管理学院', label: '经济管理学院' },
  { value: '旅游管理学院', label: '旅游管理学院' },
  { value: '语言文化学院', label: '语言文化学院' },
  { value: '马克思主义学院', label: '马克思主义学院' },
  { value: '体育学院', label: '体育学院' },
  { value: '医学院', label: '医学院' }
]);

// 🟢 更新 errors 对象，增加 college 和 major
const errors = reactive({
  userName: '', studentId: '', contact: '', activityName: '', department: '', attendees: '',
  college: '', major: '' // 新增
});

// 🟢 更新 touched 对象
const touched = reactive({
  userName: false, studentId: false, contact: false, activityName: false, department: false, attendees: false,
  college: false, major: false // 新增
});

// 自动填充逻辑
watch(() => props.userInfo, (newVal) => {
  if (newVal && Object.keys(newVal).length > 0) {
    if (!props.modelValue.userName && newVal.userName) props.modelValue.userName = newVal.userName;
    if (!props.modelValue.studentId && newVal.studentId) props.modelValue.studentId = newVal.studentId;
    if (!props.modelValue.college && newVal.college) props.modelValue.college = newVal.college;
    if (!props.modelValue.major && newVal.major) props.modelValue.major = newVal.major;
    const phone = newVal.contact || newVal.phonenumber;
    if (!props.modelValue.contact && phone) props.modelValue.contact = phone;
  }
}, { immediate: true, deep: true });

// 🟢 更新验证逻辑
const validateField = (field) => {
  let isValid = true;
  const phoneRegex = /^1[3-9]\d{9}$/;

  switch (field) {
    case 'userName':
      if (!props.modelValue.userName) { errors.userName = '请填写真实姓名'; isValid = false; } else { errors.userName = ''; }
      break;
    case 'studentId':
      if (!props.modelValue.studentId) { errors.studentId = '请填写学号'; isValid = false; } else { errors.studentId = ''; }
      break;
    case 'college': // 新增校验
      if (!props.modelValue.college) { errors.college = '请选择所属学院'; isValid = false; } else { errors.college = ''; }
      break;
    case 'major': // 新增校验
      if (!props.modelValue.major) { errors.major = '请填写年级专业'; isValid = false; } else { errors.major = ''; }
      break;
    case 'contact':
      if (!props.modelValue.contact) { errors.contact = '请填写联系电话'; isValid = false; } else if (!phoneRegex.test(props.modelValue.contact)) { errors.contact = '手机号格式不正确'; isValid = false; } else { errors.contact = ''; }
      break;
    case 'activityName':
      if (!props.modelValue.activityName) { errors.activityName = '请输入活动名称'; isValid = false; } else { errors.activityName = ''; }
      break;
    case 'department':
      if (!props.modelValue.department) { errors.department = '请输入申请部门'; isValid = false; } else { errors.department = ''; }
      break;
    case 'attendees':
      if (!props.modelValue.attendees || props.modelValue.attendees < 1) { errors.attendees = '人数至少为1'; isValid = false; } else { errors.attendees = ''; }
      break;
  }
  return isValid;
};

// ... handleInput, handleBlur 保持不变 ...
const handleInput = (field) => { touched[field] = true; validateField(field); checkFormValidity(false); };
const handleBlur = (field) => { touched[field] = true; validateField(field); checkFormValidity(false); };

// 🟢 更新 checkFormValidity 包含新字段
const checkFormValidity = (showErrors = true) => {
  const fields = ['userName', 'studentId', 'college', 'major', 'contact', 'activityName', 'department', 'attendees']; // 增加了 college, major
  let isValid = true;
  fields.forEach(field => {
    if (!validateField(field)) isValid = false;
    if (showErrors) touched[field] = true;
  });
  emit('form-validity-change', isValid);
  return isValid;
};

defineExpose({ checkFormValidity, touched, errors });
const updateAttendees = (delta) => {
  let newVal = (parseInt(props.modelValue.attendees) || 0) + delta;
  if (newVal < 1) newVal = 1;
  props.modelValue.attendees = newVal;
  handleInput('attendees');
};
const toggleProjection = () => { props.modelValue.needProjection = !props.modelValue.needProjection; };
</script>

<style scoped>
/* 原有样式保持不变 */
.booking-form { background: white; border-radius: 20px; padding: 24px 20px; margin-bottom: 24px; box-shadow: 0 8px 30px rgba(0, 0, 0, 0.04); }
.divider { height: 1px; background: #f1f5f9; margin: 24px -20px; }
.section-title { font-size: 17px; font-weight: 700; color: #1e293b; margin-bottom: 20px; display: flex; align-items: center; gap: 10px; }
.icon-box { width: 32px; height: 32px; border-radius: 8px; display: flex; align-items: center; justify-content: center; font-size: 18px; }
.icon-box.blue { background: #eff6ff; color: #3b82f6; }
.icon-box.orange { background: #fff7ed; color: #f97316; }
.sub-text { font-size: 12px; color: #94a3b8; font-weight: 400; margin-left: auto; }
.form-item { margin-bottom: 18px; }
.grid-row { display: grid; grid-template-columns: 1fr 1fr; gap: 12px; margin-bottom: 18px; }
.grid-row .form-item { margin-bottom: 0; }
.form-label { display: block; font-size: 13px; font-weight: 600; color: #64748b; margin-bottom: 8px; }
.required { color: #ef4444; margin-left: 2px; }
.form-input, .form-textarea { width: 100%; padding: 12px 14px; border: 1px solid #e2e8f0; border-radius: 12px; font-size: 15px; color: #1e293b; background: #f8fafc; transition: all 0.2s ease; box-sizing: border-box; }
.form-input:focus, .form-textarea:focus { outline: none; border-color: #3b82f6; background: white; box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1); }
.form-input::placeholder { color: #cbd5e1; font-size: 14px; }
.error-msg { display: block; font-size: 12px; color: #ef4444; margin-top: 6px; padding-left: 4px; }
.stepper-input { display: flex; align-items: center; border: 1px solid #e2e8f0; border-radius: 12px; background: white; overflow: hidden; height: 46px; }
.stepper-btn { width: 44px; height: 100%; border: none; background: #f8fafc; color: #64748b; display: flex; align-items: center; justify-content: center; cursor: pointer; transition: all 0.2s; }
.stepper-btn:active { background: #e2e8f0; }
.stepper-btn:disabled { color: #cbd5e0; cursor: not-allowed; }
.number-input { flex: 1; border: none; background: transparent; text-align: center; font-weight: 600; color: #3b82f6; padding: 0; height: 100%; box-shadow: none !important; }

/* 🟢 Toggle Card 修复样式 */
.toggle-card {
  display: flex;
  justify-content: space-between; /* 关键：让两端对齐 */
  align-items: center;
  padding: 16px;
  background: white;
  border: 1px solid #e2e8f0;
  border-radius: 14px;
  cursor: pointer;
  transition: all 0.2s;
  box-shadow: 0 2px 4px rgba(0,0,0,0.02);
}
.toggle-left-group {
  display: flex;
  align-items: center;
  flex: 1; /* 占据剩余空间 */
}
.toggle-icon { width: 42px; height: 42px; border-radius: 12px; background: #f1f5f9; display: flex; align-items: center; justify-content: center; color: #94a3b8; margin-right: 14px; transition: all 0.3s; flex-shrink: 0; }
.toggle-card.active .toggle-icon { background: #3b82f6; color: white; transform: scale(1.05); box-shadow: 0 4px 12px rgba(59, 130, 246, 0.3); }
.toggle-title { font-size: 15px; font-weight: 600; color: #334155; }
.toggle-desc { font-size: 12px; color: #94a3b8; margin-top: 2px; }
.toggle-switch {
  width: 48px;
  height: 26px;
  background: #cbd5e0;
  border-radius: 20px;
  position: relative;
  transition: background 0.3s;
  flex-shrink: 0; /* 防止被挤压 */
  margin-left: 10px;
}
.toggle-card.active { background: #eff6ff; border-color: #93c5fd; }
.toggle-card.active .toggle-switch { background: #3b82f6; }
.switch-handle { width: 22px; height: 22px; background: white; border-radius: 50%; position: absolute; top: 2px; left: 2px; transition: transform 0.3s cubic-bezier(0.4, 0.0, 0.2, 1); box-shadow: 0 2px 4px rgba(0,0,0,0.1); }
.toggle-card.active .switch-handle { transform: translateX(22px); }

/* 🟢 下拉框样式优化 */
.select-wrapper {
  position: relative;
  width: 100%;
}
.form-select {
  appearance: none; /* 去除原生默认样式 */
  -webkit-appearance: none;
  background-color: #f8fafc;
  cursor: pointer;
  padding-right: 30px; /* 给箭头留位置 */
}
.select-arrow {
  position: absolute;
  right: 12px;
  top: 50%;
  transform: translateY(-50%);
  pointer-events: none;
  color: #94a3b8;
  display: flex;
}
.placeholder-color {
  color: #cbd5e1; /* 这里的颜色要和 form-input::placeholder 一致 */
}

@media (max-width: 375px) { .grid-row { grid-template-columns: 1fr; gap: 18px; } }
</style>
