<template>
  <div class="modal-overlay" v-if="show" @click.self="close">
    <div class="modal-content">
      <div class="modal-header">
        <div class="modal-title">四、场地借用注意事项</div>
        <div class="modal-close" @click="close">×</div>
      </div>
      <div class="modal-body">
        <div class="rule-section">
          <p>（一）遵循"先预约后使用、先到先得、谁使用谁负责"的原则，若活动时间发生变动，请提前清除表格中预约登记信息。对已预约但累计未开展活动超过三次的使用单位视情况纳入学生社区活动信用"黑名单"。</p>
          <p>（二）借用场地单位需遵守本办法，严禁以校内单位名义申请活动室供校外机构进行商业活动或进行私人聚会性质活动，如查实违反规定，将严肃处理。</p>
          <p>（三）借用责任人确保使用活动室时，不得损坏场地内设备设施，保持室内卫生，如需变更场地桌椅等设施摆放，活动后需将桌椅恢复原样，使用后断电离开。</p>
          <p>（四）如遇特殊情况，党委学生工作部可对借用单位的时间安排进行调整，已经预约登记的各类活动如遇学校重要活动使用，请服从学工部统筹安排。</p>
          <p>（五）活动室使用时间内消防安全、用电安全由使用单位负责，请确保活动室使用的规范性、安全性。</p>
          <p>（六）如有违反以上管理规定，视情况严重程度，学工部有权限制一定时间的借用申请。</p>
        </div>
      </div>
      <div class="modal-footer">
        <button class="modal-btn modal-cancel" @click="close">取消</button>
        <button class="modal-btn modal-agree" @click="agree">同意</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { defineProps, defineEmits } from 'vue';

const props = defineProps({
  show: {
    type: Boolean,
    required: true
  }
});

const emit = defineEmits(['update:show', 'agree']);

const close = () => {
  emit('update:show', false);
};

const agree = () => {
  emit('agree');
  close();
};
</script>

<style scoped>
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 100;
}

.modal-content {
  width: 90%;
  max-width: 450px;
  background-color: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
  animation: modal-in 0.3s ease-out;
}

@keyframes modal-in {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid #eee;
  background-color: #4080ff;
  color: white;
}

.modal-title {
  font-size: 18px;
  font-weight: 600;
}

.modal-close {
  font-size: 24px;
  cursor: pointer;
  line-height: 1;
}

.modal-body {
  padding: 20px;
  max-height: 60vh;
  overflow-y: auto;
}

.rule-section {
  font-size: 14px;
  line-height: 1.6;
  color: #333;
}

.rule-section p {
  margin-bottom: 12px;
  text-indent: 2em;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  padding: 16px 20px;
  border-top: 1px solid #eee;
  gap: 12px;
}

.modal-btn {
  padding: 8px 20px;
  border-radius: 6px;
  font-size: 15px;
  font-weight: 500;
  cursor: pointer;
  border: none;
  transition: all 0.2s;
}

.modal-cancel {
  background-color: #f5f5f5;
  color: #666;
}

.modal-cancel:hover {
  background-color: #e8e8e8;
}

.modal-agree {
  background-color: #4080ff;
  color: white;
}

.modal-agree:hover {
  background-color: #3070ee;
}
</style>
