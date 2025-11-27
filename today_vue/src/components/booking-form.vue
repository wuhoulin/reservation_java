<template>
  <div class="booking-form">
    <h2 class="form-title">预约信息</h2>

    <div class="form-group">
      <label class="form-label">
        活动名称（用途）
        <span class="required">*</span>
      </label>
      <div class="input-wrapper">
        <input
            v-model="formData.activityName"
            type="text"
            class="form-input"
            placeholder="请输入活动名称或用途"
            @blur="validateField('activityName')"
            @input="validateField('activityName')"
            @focus="markFieldTouched('activityName')"
        />
        <i v-if="touched.activityName && !errors.activityName" class="valid-icon">✓</i>
        <i v-if="touched.activityName && errors.activityName" class="error-icon">!</i>
      </div>
      <span v-if="touched.activityName && errors.activityName" class="error-message">{{ errors.activityName }}</span>
    </div>

    <div class="form-group">
      <label class="form-label">
        申请部门
        <span class="required">*</span>
      </label>
      <div class="input-wrapper">
        <input
            v-model="formData.department"
            type="text"
            class="form-input"
            placeholder="个人/组织名称"
            @blur="validateField('department')"
            @input="validateField('department')"
            @focus="markFieldTouched('department')"
        />
        <i v-if="touched.department && !errors.department" class="valid-icon">✓</i>
        <i v-if="touched.department && errors.department" class="error-icon">!</i>
      </div>
      <span v-if="touched.department && errors.department" class="error-message">{{ errors.department }}</span>
    </div>

    <div class="form-group">
      <label class="form-label">
        活动人数
        <span class="required">*</span>
      </label>
      <div class="number-input-group">
        <button
            class="number-btn"
            @click="decrementAttendees"
            :disabled="formData.attendees <= 1"
        >
          -
        </button>
        <input
            v-model.number="formData.attendees"
            type="number"
            class="form-input number-input"
            min="1"
            max="100"
            readonly
            @focus="markFieldTouched('attendees')"
        />
        <button
            class="number-btn"
            @click="incrementAttendees"
            :disabled="formData.attendees >= 100"
        >
          +
        </button>
      </div>
      <span v-if="touched.attendees && errors.attendees" class="error-message">{{ errors.attendees }}</span>
    </div>

    <div class="form-group checkbox-group">
      <label class="checkbox-label">
        <input
            type="checkbox"
            v-model="formData.needProjection"
            class="custom-checkbox"
        />
        <span>需要多媒体投屏</span>
      </label>
    </div>

    <div class="form-grid">
      <div class="form-group">
        <label class="form-label">
          使用人姓名
          <span class="required">*</span>
        </label>
        <div class="input-wrapper">
          <input
              v-model="formData.userName"
              type="text"
              class="form-input"
              placeholder="请输入使用人姓名"
              @blur="validateField('userName')"
              @input="validateField('userName')"
              @focus="markFieldTouched('userName')"
          />
          <i v-if="touched.userName && !errors.userName" class="valid-icon">✓</i>
          <i v-if="touched.userName && errors.userName" class="error-icon">!</i>
        </div>
        <span v-if="touched.userName && errors.userName" class="error-message">{{ errors.userName }}</span>
      </div>

      <div class="form-group">
        <label class="form-label">
          学号
          <span class="required">*</span>
        </label>
        <div class="input-wrapper">
          <input
              v-model="formData.studentId"
              type="text"
              class="form-input"
              placeholder="请输入学号"
              @blur="validateField('studentId')"
              @input="validateField('studentId')"
              @focus="markFieldTouched('studentId')"
          />
          <i v-if="touched.studentId && !errors.studentId" class="valid-icon">✓</i>
          <i v-if="touched.studentId && errors.studentId" class="error-icon">!</i>
        </div>
        <span v-if="touched.studentId && errors.studentId" class="error-message">{{ errors.studentId }}</span>
      </div>

      <div class="form-group">
        <label class="form-label">
          学院
          <span class="required">*</span>
        </label>
        <div class="input-wrapper">
          <input
              v-model="formData.college"
              type="text"
              class="form-input"
              placeholder="请输入所属学院"
              @blur="validateField('college')"
              @input="validateField('college')"
              @focus="markFieldTouched('college')"
          />
          <i v-if="touched.college && !errors.college" class="valid-icon">✓</i>
          <i v-if="touched.college && errors.college" class="error-icon">!</i>
        </div>
        <span v-if="touched.college && errors.college" class="error-message">{{ errors.college }}</span>
      </div>

      <div class="form-group">
        <label class="form-label">
          年级专业
          <span class="required">*</span>
        </label>
        <div class="input-wrapper">
          <input
              v-model="formData.major"
              type="text"
              class="form-input"
              placeholder="请输入年级专业"
              @blur="validateField('major')"
              @input="validateField('major')"
              @focus="markFieldTouched('major')"
          />
          <i v-if="touched.major && !errors.major" class="valid-icon">✓</i>
          <i v-if="touched.major && errors.major" class="error-icon">!</i>
        </div>
        <span v-if="touched.major && errors.major" class="error-message">{{ errors.major }}</span>
      </div>
    </div>

    <div class="form-group">
      <label class="form-label">
        联系方式
        <span class="required">*</span>
      </label>
      <div class="input-wrapper">
        <input
            v-model="formData.contact"
            type="text"
            class="form-input"
            placeholder="请输入11位手机号码"
            @blur="validateContact"
            @input="validateContact"
            @focus="markFieldTouched('contact')"
        />
        <i v-if="touched.contact && !errors.contact" class="valid-icon">✓</i>
        <i v-if="touched.contact && errors.contact" class="error-icon">!</i>
      </div>
      <span v-if="touched.contact && errors.contact" class="error-message">{{ errors.contact }}</span>
    </div>


    <div class="form-grid">
      <div class="form-group">
        <label class="form-label">指导老师姓名</label>
        <div class="input-wrapper">
          <input
              v-model="formData.teacherName"
              type="text"
              class="form-input"
              placeholder="请输入指导老师姓名（如有）"
          />
        </div>
      </div>

      <div class="form-group">
        <label class="form-label">指导老师联系方式</label>
        <div class="input-wrapper">
          <input
              v-model="formData.teacherContact"
              type="text"
              class="form-input"
              placeholder="请输入指导老师手机号码（如有）"
              @blur="validateTeacherContact"
              @input="validateTeacherContact"
              @focus="markFieldTouched('teacherContact')"
          />
          <i v-if="touched.teacherContact && formData.teacherContact && !errors.teacherContact" class="valid-icon">✓</i>
          <i v-if="touched.teacherContact && errors.teacherContact" class="error-icon">!</i>
        </div>
        <span v-if="touched.teacherContact && errors.teacherContact" class="error-message">{{ errors.teacherContact }}</span>
      </div>
    </div>

    <div class="form-group">
      <label class="form-label">其他需求</label>
      <div class="input-wrapper textarea-wrapper">
        <textarea
            v-model="formData.otherRequirements"
            class="form-textarea"
            placeholder="请输入其他需求（如有）"
            maxlength="500"
            rows="3"
        ></textarea>
        <div class="char-counter">{{ formData.otherRequirements.length }}/500</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, computed, watch, nextTick } from 'vue';

const props = defineProps({
  modelValue: {
    type: Object,
    required: true,
    default: () => ({
      activityName: '',
      department: '',
      attendees: 1,
      needProjection: false,
      userName: '',
      studentId: '',
      college: '',
      major: '',
      contact: '',
      teacherName: '',
      teacherContact: '',
      otherRequirements: ''
    })
  }
});

const emit = defineEmits(['update:modelValue', 'formValidityChange']);

// 表单数据双向绑定
const formData = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
});

// 错误信息
const errors = reactive({
  activityName: '',
  department: '',
  attendees: '',
  userName: '',
  studentId: '',
  college: '',
  major: '',
  contact: '',
  teacherContact: ''
});

// 字段交互标记
const touched = reactive({
  activityName: false,
  department: false,
  attendees: false,
  userName: false,
  studentId: false,
  college: false,
  major: false,
  contact: false,
  teacherContact: false
});

// 验证规则
const validationRules = {
  activityName: (value) => !value.trim() ? '活动名称不能为空' : '',
  department: (value) => !value.trim() ? '申请部门不能为空' : '',
  attendees: (value) => value < 1 || value > 100 ? '人数必须在1-100之间' : '',
  userName: (value) => !value.trim() ? '使用人姓名不能为空' : '',
  studentId: (value) => !value.trim() ? '学号不能为空' : '',
  college: (value) => !value.trim() ? '学院不能为空' : '',
  major: (value) => !value.trim() ? '年级专业不能为空' : '',
  contact: (value) => {
    if (!value.trim()) return '联系方式不能为空';
    if (!/^1[3-9]\d{9}$/.test(value.trim())) return '请输入有效的11位手机号码';
    return '';
  },
  teacherContact: (value) => {
    if (value.trim() && !/^1[3-9]\d{9}$/.test(value.trim())) return '请输入有效的11位手机号码';
    return '';
  }
};

// 验证单个字段
const validateField = (fieldName) => {
  const value = formData.value[fieldName];
  errors[fieldName] = validationRules[fieldName](value);
  checkFormValidity(false);
};

// 验证联系方式
const validateContact = () => {
  const value = formData.value.contact;
  errors.contact = validationRules.contact(value);
  checkFormValidity(false);
};

// 验证指导老师联系方式
const validateTeacherContact = () => {
  const value = formData.value.teacherContact;
  errors.teacherContact = validationRules.teacherContact(value);
  checkFormValidity(false);
};

// 表单整体有效性校验
// showErrors: 是否显示错误信息（true：显示，false：仅校验不显示）
const checkFormValidity = (showErrors = false) => {
  // 强制校验所有必填字段
  const requiredFields = ['activityName', 'department', 'attendees', 'userName', 'studentId', 'college', 'major', 'contact'];
  requiredFields.forEach(field => {
    errors[field] = validationRules[field](formData.value[field]);
    // 只有需要显示错误时才标记为touched
    if (showErrors) {
      touched[field] = true;
    }
  });

  // 校验非必填字段（有值时）
  if (formData.value.teacherContact.trim() && showErrors) {
    errors.teacherContact = validationRules.teacherContact(formData.value.teacherContact);
    touched.teacherContact = true;
  }

  const isValid = Object.values(errors).every(msg => !msg);
  emit('formValidityChange', isValid);
  return isValid;
};

// 暴露方法给父组件
defineExpose({
  checkFormValidity,
  touched,
  errors
});

// 人数增减
const incrementAttendees = () => {
  if (formData.value.attendees < 100) {
    formData.value.attendees++;
    markFieldTouched('attendees');
    validateField('attendees');
  }
};

const decrementAttendees = () => {
  if (formData.value.attendees > 1) {
    formData.value.attendees--;
    markFieldTouched('attendees');
    validateField('attendees');
  }
};

// 标记字段为已交互
const markFieldTouched = (fieldName) => {
  touched[fieldName] = true;
};

// 监听表单字段变化，实时校验（仅在字段已交互后）
watch([
  () => formData.value.activityName,
  () => formData.value.department,
  () => formData.value.attendees,
  () => formData.value.userName,
  () => formData.value.studentId,
  () => formData.value.college,
  () => formData.value.major,
  () => formData.value.contact,
  () => formData.value.teacherContact
], async (newValues, oldValues) => {
  await nextTick();

  Object.keys(validationRules).forEach((field, index) => {
    const oldValue = oldValues[index] || '';
    const newValue = newValues[index] || '';
    // 只有字段已交互或值发生变化时才校验
    if (touched[field] || oldValue.trim() !== newValue.trim()) {
      errors[field] = validationRules[field](newValue);
      // 只有值发生变化时才标记为已交互
      if (oldValue.trim() !== newValue.trim()) {
        touched[field] = true;
      }
    }
  });

  checkFormValidity(false);
}, { immediate: false });
</script>

<style scoped>
.booking-form {
  background-color: #ffffff;
  border-radius: 16px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.03);
  padding: 28px 24px;
  margin-top: 20px;
}

.form-title {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 24px;
  color: #1e293b;
  position: relative;
  padding-bottom: 8px;
}

.form-title::after {
  content: '';
  position: absolute;
  left: 0;
  bottom: 0;
  width: 32px;
  height: 2px;
  background-color: #3b82f6;
  border-radius: 2px;
}

/* --- 统一间距的核心修改 --- */

.form-group {
  /* 统一所有表单项的底部间距 */
  margin-bottom: 24px;
  position: relative;
}

/* 移除 form-grid 内部 form-group 的 margin-bottom，改用 gap 控制 */
.form-grid .form-group {
  margin-bottom: 0;
}

/* ----------------------- */

.form-label {
  display: block;
  margin-bottom: 8px;
  font-size: 14px;
  font-weight: 500;
  color: #475569;
}

.required {
  color: #ef4444;
  margin-left: 4px;
  font-size: 14px;
}

.input-wrapper {
  position: relative;
  display: flex;
  align-items: center;
}

.form-input {
  width: 100%;
  padding: 14px 16px;
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  font-size: 14px;
  transition: all 0.2s;
  background-color: #f8fafc;
  color: #1e293b;
  /* 确保输入框高度统一 */
  height: 48px;
  box-sizing: border-box;
}

.form-input:focus {
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
  outline: none;
  background-color: #ffffff;
}

.form-input::placeholder {
  color: #94a3b8;
  font-size: 13px;
}

.valid-icon, .error-icon {
  position: absolute;
  right: 16px;
  font-size: 16px;
  /* 默认隐藏，不占用点击 */
  pointer-events: none;
}

.valid-icon {
  color: #10b981;
}

.error-icon {
  color: #ef4444;
}

.form-textarea {
  width: 100%;
  padding: 14px 16px;
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  font-size: 14px;
  transition: all 0.2s;
  background-color: #f8fafc;
  color: #1e293b;
  resize: vertical;
  min-height: 100px;
}

.form-textarea:focus {
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
  outline: none;
  background-color: #ffffff;
}

.textarea-wrapper {
  position: relative;
}

.char-counter {
  position: absolute;
  right: 16px;
  bottom: 12px;
  text-align: right;
  font-size: 12px;
  color: #94a3b8;
  pointer-events: none;
}

/* 复选框组特殊处理，确保间距一致 */
.checkbox-group {
  display: flex;
  align-items: center;
  /* 确保高度与其他输入框区域协调 */
  min-height: 48px;
  margin-bottom: 24px;
}

.checkbox-label {
  display: flex;
  align-items: center;
  cursor: pointer;
  font-size: 14px;
  color: #475569;
}

.custom-checkbox {
  width: 18px;
  height: 18px;
  margin-right: 10px;
  border: 1px solid #e2e8f0;
  border-radius: 4px;
  cursor: pointer;
  appearance: none;
  position: relative;
  transition: all 0.2s;
  background-color: #fff;
}

.custom-checkbox:checked {
  background-color: #3b82f6;
  border-color: #3b82f6;
}

.custom-checkbox:checked::after {
  content: '✓';
  position: absolute;
  color: white;
  font-size: 12px;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
}

.number-input-group {
  display: flex;
  align-items: center;
  gap: 8px;
}

.number-input {
  text-align: center;
  flex: 1;
  -moz-appearance: textfield;
}

.number-input::-webkit-outer-spin-button,
.number-input::-webkit-inner-spin-button {
  -webkit-appearance: none;
  margin: 0;
}

.number-btn {
  width: 42px;
  height: 48px;
  border: 1px solid #e2e8f0;
  background-color: #f8fafc;
  border-radius: 10px;
  font-size: 18px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s;
  color: #475569;
}

.number-btn:hover:not(:disabled) {
  background-color: #e2e8f0;
  color: #3b82f6;
}

.number-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  background-color: #f1f5f9;
  color: #94a3b8;
}

/* --- 错误信息定位核心修改 (使用绝对定位) --- */
.error-message {
  position: absolute;
  left: 0;
  /* 确保位于输入框底部的合理位置，不会与下一个输入框的 label 重叠 */
  bottom: -20px;
  color: #ef4444;
  font-size: 12px;
  line-height: 1.4;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 100%;
  /* 初始不可见/透明 */
  opacity: 0;
  transition: opacity 0.2s;
}

.touched .error-message {
  /* 仅在 touched 且有错误时显示 */
  opacity: 1;
}
/* ----------------------------------------- */


/* 网格布局间距控制 */
.form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  /* gap 控制水平和垂直间距 */
  gap: 24px 16px;
  /* 确保网格底部的间距与普通表单项一致 */
  margin-bottom: 24px;
}

/* 响应式适配 */
@media (max-width: 768px) {
  .form-grid {
    grid-template-columns: 1fr;
    /* 手机端垂直间距保持一致 */
    gap: 24px;
  }
}
</style>
