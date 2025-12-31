<template>
  <div class="booking-form">
    <div class="form-section">
      <h3 class="section-title"><span class="title-icon">📝</span>填写预约信息</h3>

      <div class="form-item">
        <label class="form-label">活动名称 <span class="required">*</span></label>
        <input
            v-model="modelValue.activityName"
            type="text"
            class="form-input"
            placeholder="请输入活动名称"
            @blur="handleBlur('activityName')"
            @input="handleInput('activityName')"
        />
        <span class="error-msg" v-if="touched.activityName && errors.activityName">{{ errors.activityName }}</span>
      </div>

      <div class="form-group-row">
        <div class="form-item half">
          <label class="form-label">申请部门 <span class="required">*</span></label>
          <input
              v-model="modelValue.department"
              type="text"
              class="form-input"
              placeholder="申请部门"
              @blur="handleBlur('department')"
              @input="handleInput('department')"
          />
          <span class="error-msg" v-if="touched.department && errors.department">{{ errors.department }}</span>
        </div>

        <div class="form-item half">
          <label class="form-label">活动人数 <span class="required">*</span></label>
          <div class="stepper-input">
            <button class="stepper-btn minus" @click="updateAttendees(-1)" :disabled="modelValue.attendees <= 1">
              <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="5" y1="12" x2="19" y2="12"></line></svg>
            </button>
            <input
                v-model.number="modelValue.attendees"
                type="tel"
                class="form-input number-input"
                placeholder="人数"
                @blur="handleBlur('attendees')"
                @input="handleInput('attendees')"
            />
            <button class="stepper-btn plus" @click="updateAttendees(1)">
              <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="12" y1="5" x2="12" y2="19"></line><line x1="5" y1="12" x2="19" y2="12"></line></svg>
            </button>
          </div>
          <span class="error-msg" v-if="touched.attendees && errors.attendees">{{ errors.attendees }}</span>
        </div>
      </div>

      <div class="form-item">
        <div class="toggle-card" :class="{ active: modelValue.needProjection }" @click="toggleProjection">
          <div class="toggle-icon">
            <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <rect x="2" y="3" width="20" height="14" rx="2" ry="2"></rect>
              <line x1="8" y1="21" x2="16" y2="21"></line>
              <line x1="12" y1="17" x2="12" y2="21"></line>
            </svg>
          </div>
          <div class="toggle-content">
            <div class="toggle-title">多媒体投屏</div>
            <div class="toggle-desc">是否需要多媒体投屏</div>
          </div>
          <div class="toggle-switch">
            <div class="switch-handle"></div>
          </div>
        </div>
      </div>

      <div class="form-item">
        <label class="form-label">指导老师</label>
        <input v-model="modelValue.teacherName" type="text" class="form-input" placeholder="选填" />
      </div>

      <div class="form-item">
        <label class="form-label">老师联系方式</label>
        <input v-model="modelValue.teacherContact" type="tel" class="form-input" placeholder="选填" maxlength="11" />
      </div>

      <div class="form-item">
        <label class="form-label">其他需求</label>
        <textarea
            v-model="modelValue.otherRequirements"
            class="form-textarea"
            placeholder="如有其他设备需求请在此说明..."
            rows="3"
        ></textarea>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, watch } from 'vue';

const props = defineProps({
  modelValue: {
    type: Object,
    required: true,
    default: () => ({
      activityName: '',
      department: '',
      attendees: 1,
      needProjection: false,
      teacherName: '',
      teacherContact: '',
      otherRequirements: ''
    })
  }
});

const emit = defineEmits(['update:modelValue', 'form-validity-change']);

// 错误信息对象
const errors = reactive({
  activityName: '',
  department: '',
  attendees: ''
});

// 🟢 关键修复：记录字段是否被用户“碰过”
const touched = reactive({
  activityName: false,
  department: false,
  attendees: false
});

// 单字段校验逻辑
const validateField = (field) => {
  let isValid = true;
  if (field === 'activityName') {
    if (!props.modelValue.activityName) {
      errors.activityName = '请输入活动名称';
      isValid = false;
    } else {
      errors.activityName = '';
    }
  }
  if (field === 'department') {
    if (!props.modelValue.department) {
      errors.department = '请输入申请部门';
      isValid = false;
    } else {
      errors.department = '';
    }
  }
  if (field === 'attendees') {
    if (!props.modelValue.attendees || props.modelValue.attendees < 1) {
      errors.attendees = '人数至少为1';
      isValid = false;
    } else {
      errors.attendees = '';
    }
  }
  return isValid;
};

// 🟢 用户输入时：只标记当前字段为 touched，并静默检查全局状态
const handleInput = (field) => {
  touched[field] = true;
  validateField(field);
  checkFormValidity(false); // false 表示不强制显示其他字段的错误
};

// 🟢 失去焦点时：标记当前字段为 touched
const handleBlur = (field) => {
  touched[field] = true;
  validateField(field);
  checkFormValidity(false);
};

// 🟢 全局校验方法
// showErrors 参数：
// true = 点击提交按钮时调用，强制把所有必填项标红
// false = 页面加载或输入中调用，只检查逻辑，不标红未碰触的字段
const checkFormValidity = (showErrors = true) => {
  const v1 = validateField('activityName');
  const v2 = validateField('department');
  const v3 = validateField('attendees');

  const isValid = v1 && v2 && v3;

  if (showErrors) {
    touched.activityName = true;
    touched.department = true;
    touched.attendees = true;
  }

  emit('form-validity-change', isValid);
  return isValid;
};

// 暴露给父组件
defineExpose({
  checkFormValidity,
  touched,
  errors
});

// 人数步进器逻辑
const updateAttendees = (delta) => {
  let newVal = (parseInt(props.modelValue.attendees) || 0) + delta;
  if (newVal < 1) newVal = 1;
  props.modelValue.attendees = newVal;
  // 按钮操作也算作用户交互，需要触发校验
  handleInput('attendees');
};

// 切换多媒体投屏
const toggleProjection = () => {
  props.modelValue.needProjection = !props.modelValue.needProjection;
};

// 监听值变化 (用于父组件可能重置表单的情况)
watch(() => props.modelValue, () => {
  // 这里不主动触发校验，防止循环或意外红字，完全依赖 handleInput
}, { deep: true });

</script>

<style scoped>
.booking-form {
  background: white;
  border-radius: 16px;
  padding: 24px 20px;
  margin-bottom: 24px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
}

.section-title {
  font-size: 18px;
  font-weight: 700;
  color: #2d3748;
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.title-icon { font-size: 20px; }

.form-item {
  margin-bottom: 20px;
}

.form-item:last-child {
  margin-bottom: 0;
}

.form-label {
  display: block;
  font-size: 14px;
  font-weight: 600;
  color: #4a5568;
  margin-bottom: 8px;
}

.required {
  color: #e53e3e;
  margin-left: 4px;
}

.form-input, .form-textarea {
  width: 100%;
  padding: 12px 14px;
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  font-size: 15px;
  color: #2d3748;
  background: #f8fafc;
  transition: all 0.2s ease;
  box-sizing: border-box;
}

.form-input:focus, .form-textarea:focus {
  outline: none;
  border-color: #667eea;
  background: white;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.form-input::placeholder, .form-textarea::placeholder {
  color: #a0aec0;
}

.error-msg {
  display: block;
  font-size: 12px;
  color: #e53e3e;
  margin-top: 6px;
}

/* 布局：一行两列 */
.form-group-row {
  display: flex;
  gap: 16px;
  margin-bottom: 20px;
}

.form-item.half {
  flex: 1;
  margin-bottom: 0;
}

/* 步进器输入框 */
.stepper-input {
  display: flex;
  align-items: center;
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  background: #f8fafc;
  overflow: hidden;
  width: 100%;
}

.stepper-btn {
  width: 40px;
  height: 42px;
  border: none;
  background: transparent;
  color: #667eea;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: background 0.2s;
  flex-shrink: 0; /* 🟢 防止被压缩 */
}

.stepper-btn:active {
  background: #e2e8f0;
}

.stepper-btn:disabled {
  color: #cbd5e0;
  cursor: not-allowed;
}

.number-input {
  flex: 1;
  border: none;
  background: transparent;
  text-align: center;
  padding: 0;
  height: 42px;
  font-weight: 600;
  border-radius: 0;
  box-shadow: none !important;
}

/* 多媒体投屏卡片开关 */
.toggle-card {
  display: flex;
  align-items: center;
  padding: 16px;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.toggle-card.active {
  background: #eff6ff; /* 激活时的淡蓝色背景 */
  border-color: #bfdbfe;
}

.toggle-icon {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  background: white;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #a0aec0;
  margin-right: 12px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.05);
  transition: all 0.3s;
}

.toggle-card.active .toggle-icon {
  background: #3b82f6;
  color: white;
}

.toggle-content {
  flex: 1;
}

.toggle-title {
  font-size: 15px;
  font-weight: 600;
  color: #2d3748;
  margin-bottom: 2px;
}

.toggle-desc {
  font-size: 12px;
  color: #718096;
}

/* 模拟 iOS 开关 */
.toggle-switch {
  width: 44px;
  height: 24px;
  background: #cbd5e0;
  border-radius: 12px;
  position: relative;
  transition: background 0.3s;
}

.toggle-card.active .toggle-switch {
  background: #3b82f6;
}

.switch-handle {
  width: 20px;
  height: 20px;
  background: white;
  border-radius: 50%;
  position: absolute;
  top: 2px;
  left: 2px;
  transition: transform 0.3s cubic-bezier(0.4, 0.0, 0.2, 1);
  box-shadow: 0 1px 2px rgba(0,0,0,0.2);
}

.toggle-card.active .switch-handle {
  transform: translateX(20px);
}

</style>
