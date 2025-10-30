<template>
  <div class="booking-form">
    <h2 class="section-title">预约信息</h2>

    <!-- 活动名称 -->
    <div class="form-group">
      <label for="activityName">活动名称（用途）<span class="required">*</span></label>
      <input
          id="activityName"
          v-model="formData.activityName"
          type="text"
          class="form-input"
          placeholder="请输入活动名称或用途"
          required
          @blur="validateField('activityName')"
      />
      <span v-if="errors.activityName" class="error-message">{{ errors.activityName }}</span>
    </div>

    <!-- 申请部门 -->
    <div class="form-group">
      <label for="department">申请部门<span class="required">*</span></label>
      <input
          id="department"
          v-model="formData.department"
          type="text"
          class="form-input"
          placeholder="个人/组织名称"
          required
          @blur="validateField('department')"
      />
      <span v-if="errors.department" class="error-message">{{ errors.department }}</span>
    </div>

    <!-- 活动人数 -->
    <div class="form-group">
      <label for="attendees">活动人数<span class="required">*</span></label>
      <div class="number-input-container">
        <button class="number-btn" @click="decrementAttendees" :disabled="formData.attendees <= 1">-</button>
        <input
            id="attendees"
            v-model.number="formData.attendees"
            type="number"
            class="form-input number-input"
            min="1"
            max="100"
            required
            @blur="validateField('attendees')"
        />
        <button class="number-btn" @click="incrementAttendees" :disabled="formData.attendees >= 100">+</button>
      </div>
      <span v-if="errors.attendees" class="error-message">{{ errors.attendees }}</span>
    </div>

    <!-- 多媒体投屏 -->
    <div class="form-group checkbox-group">
      <label class="checkbox-label">
        <input
            type="checkbox"
            v-model="formData.needProjection"
        />
        <span>需要多媒体投屏</span>
      </label>
    </div>

    <!-- 使用人姓名 -->
    <div class="form-group">
      <label for="userName">使用人姓名<span class="required">*</span></label>
      <input
          id="userName"
          v-model="formData.userName"
          type="text"
          class="form-input"
          placeholder="请输入使用人姓名"
          required
          @blur="validateField('userName')"
      />
      <span v-if="errors.userName" class="error-message">{{ errors.userName }}</span>
    </div>

    <!-- 学号 -->
    <div class="form-group">
      <label for="studentId">学号<span class="required">*</span></label>
      <input
          id="studentId"
          v-model="formData.studentId"
          type="text"
          class="form-input"
          placeholder="请输入学号"
          required
          @blur="validateField('studentId')"
      />
      <span v-if="errors.studentId" class="error-message">{{ errors.studentId }}</span>
    </div>

    <!-- 学院 -->
    <div class="form-group">
      <label for="college">学院<span class="required">*</span></label>
      <input
          id="college"
          v-model="formData.college"
          type="text"
          class="form-input"
          placeholder="请输入所属学院"
          required
          @blur="validateField('college')"
      />
      <span v-if="errors.college" class="error-message">{{ errors.college }}</span>
    </div>

    <!-- 年级专业 -->
    <div class="form-group">
      <label for="major">年级专业<span class="required">*</span></label>
      <input
          id="major"
          v-model="formData.major"
          type="text"
          class="form-input"
          placeholder="请输入年级专业"
          required
          @blur="validateField('major')"
      />
      <span v-if="errors.major" class="error-message">{{ errors.major }}</span>
    </div>

    <!-- 联系方式 -->
    <div class="form-group">
      <label for="contact">联系方式<span class="required">*</span></label>
      <input
          id="contact"
          v-model="formData.contact"
          type="text"
          class="form-input"
          placeholder="请输入11位手机号码"
          required
          @blur="validateContact"
      />
      <span v-if="errors.contact" class="error-message">{{ errors.contact }}</span>
    </div>

    <!-- 指导老师 -->
    <div class="form-group">
      <label for="teacherName">指导老师</label>
      <input
          id="teacherName"
          v-model="formData.teacherName"
          type="text"
          class="form-input"
          placeholder="请输入指导老师姓名（如有）"
      />
    </div>

    <!-- 指导老师联系方式 -->
    <div class="form-group">
      <label for="teacherContact">指导老师联系方式</label>
      <input
          id="teacherContact"
          v-model="formData.teacherContact"
          type="text"
          class="form-input"
          placeholder="请输入指导老师联系方式（如有）"
          @blur="validateTeacherContact"
      />
      <span v-if="errors.teacherContact" class="error-message">{{ errors.teacherContact }}</span>
    </div>

    <!-- 其他需求 -->
    <div class="form-group">
      <label for="otherRequirements">其他需求</label>
      <textarea
          id="otherRequirements"
          v-model="formData.otherRequirements"
          class="form-textarea"
          placeholder="请输入其他需求（如有）"
          maxlength="500"
      ></textarea>
      <div class="char-counter">{{ formData.otherRequirements.length }}/500</div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue';

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

// 表单数据
const formData = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
});

// 错误信息
const errors = ref({
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

// 验证规则
const validationRules = {
  activityName: (value) => !value.trim() ? '活动名称不能为空' : '',
  department: (value) => !value.trim() ? '申请部门不能为空' : '',
  attendees: (value) => value < 1 || value > 100 ? '人数必须在1-100之间' : '',
  userName: (value) => !value.trim() ? '使用人姓名不能为空' : '',
  studentId: (value) => !value.trim() ? '学号不能为空' : '',   // <--- 新增
  college: (value) => !value.trim() ? '学院不能为空' : '',
  major: (value) => !value.trim() ? '年级专业不能为空' : '',
  contact: (value) => {
    if (!value.trim()) return '联系方式不能为空';
    if (!/^1[3-9]\d{9}$/.test(value)) return '请输入有效的11位手机号码';
    return '';
  },
  teacherContact: (value) => {
    if (value && !/^1[3-9]\d{9}$/.test(value)) return '请输入有效的11位手机号码';
    return '';
  }
};


// 验证单个字段
const validateField = (fieldName) => {
  errors.value[fieldName] = validationRules[fieldName](formData.value[fieldName]);
  checkFormValidity();
};

// 特殊验证联系方式
const validateContact = () => {
  errors.value.contact = validationRules.contact(formData.value.contact);
  checkFormValidity();
};

// 验证指导老师联系方式
const validateTeacherContact = () => {
  errors.value.teacherContact = validationRules.teacherContact(formData.value.teacherContact);
  checkFormValidity();
};

// 检查整个表单有效性
const checkFormValidity = () => {
  const isValid = !Object.values(errors.value).some(msg => msg) &&
      formData.value.activityName.trim() &&
      formData.value.department.trim() &&
      formData.value.userName.trim() &&
      formData.value.studentId.trim() &&
      formData.value.college.trim() &&
      formData.value.major.trim() &&
      formData.value.contact.trim() &&
      /^1[3-9]\d{9}$/.test(formData.value.contact);


  emit('formValidityChange', isValid);
};

// 人数增减
const incrementAttendees = () => {
  if (formData.value.attendees < 100) {
    formData.value.attendees++;
    validateField('attendees');
  }
};

const decrementAttendees = () => {
  if (formData.value.attendees > 1) {
    formData.value.attendees--;
    validateField('attendees');
  }
};


// 在数据变化时自动校验，确保父组件拿到最新的有效性状态
watch(formData, () => checkFormValidity(), { deep: true, immediate: true });
</script>

<style scoped>
.booking-form {
  margin-bottom: 28px;
  border-top: 1px solid #eaeaea;
  padding-top: 24px;
}

.section-title {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 16px;
  color: #333;
}

.form-group {
  margin-bottom: 16px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  font-size: 14px;
  font-weight: 500;
  color: #333;
}

.required {
  color: #f5222d;
  margin-left: 4px;
}

.form-input, .form-textarea {
  width: 100%;
  padding: 12px 16px;
  border: 1px solid #d9d9d9;
  border-radius: 8px;
  font-size: 14px;
  transition: all 0.3s;
  background-color: #f9f9f9;
}

.form-input:focus, .form-textarea:focus {
  border-color: #1677ff;
  box-shadow: 0 0 0 2px rgba(22, 119, 255, 0.1);
  outline: none;
  background-color: #fff;
}

.form-textarea {
  min-height: 100px;
  resize: vertical;
}

.checkbox-group {
  margin-top: 8px;
  margin-bottom: 20px;
}

.checkbox-label {
  display: flex;
  align-items: center;
  cursor: pointer;
}

.checkbox-label input[type="checkbox"] {
  margin-right: 8px;
  width: 16px;
  height: 16px;
}

.number-input-container {
  display: flex;
  align-items: center;
}

.number-input {
  text-align: center;
  margin: 0 8px;
  width: 80px;
  -moz-appearance: textfield;
}

.number-input::-webkit-outer-spin-button,
.number-input::-webkit-inner-spin-button {
  -webkit-appearance: none;
  margin: 0;
}

.number-btn {
  width: 36px;
  height: 36px;
  border: 1px solid #d9d9d9;
  background-color: #f5f5f5;
  border-radius: 4px;
  font-size: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s;
}

.number-btn:hover {
  background-color: #e6e6e6;
}

.number-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.error-message {
  display: block;
  margin-top: 4px;
  color: #f5222d;
  font-size: 12px;
}

.char-counter {
  text-align: right;
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}
</style>
