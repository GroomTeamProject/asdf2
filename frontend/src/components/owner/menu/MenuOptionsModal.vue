<!-- src/components/owner/menu/MenuOptionsModal.vue -->
<template>
  <div class="modal-overlay" @click="$emit('close')">
    <div class="modal-content options-modal" @click.stop>
      <div class="modal-header">
        <div class="header-info">
          <h3>옵션 관리</h3>
          <p class="menu-name">{{ menu?.name }}</p>
        </div>
        <button @click="$emit('close')" class="close-btn">✕</button>
      </div>

      <div class="modal-body">
        <!-- 옵션 그룹 목록 -->
        <div class="option-groups-section">
          <div class="section-header">
            <h4>옵션 그룹</h4>
            <button @click="showAddGroupModal = true" class="add-group-btn">
              ➕ 그룹 추가
            </button>
          </div>

          <div v-if="optionGroups.length === 0" class="no-groups">
            옵션 그룹이 없습니다. 새 그룹을 추가해보세요.
          </div>

          <div v-else class="groups-list">
            <div 
              v-for="group in optionGroups" 
              :key="group.id"
              class="group-card"
              :class="{ 
                'invalid': !group.status.isValid,
                'selected': selectedGroup?.id === group.id 
              }"
              @click="selectGroup(group)"
            >
              <div class="group-info">
                <div class="group-header">
                  <h5 class="group-name">{{ group.name }}</h5>
                  <div class="group-badges">
                    <span class="type-badge" :class="group.type.toLowerCase()">
                      {{ getTypeText(group.type) }}
                    </span>
                    <span v-if="group.isRequired" class="required-badge">필수</span>
                  </div>
                </div>

                <div class="group-stats">
                  <span class="items-count">
                    아이템 {{ group.activeItems }}/{{ group.totalItems }}개
                  </span>
                  <span class="order">순서: {{ group.displayOrder }}</span>
                </div>

                <div class="group-status" :class="{ 'error': !group.status.isValid }">
                  {{ group.status.message }}
                </div>
              </div>

              <div class="group-controls">
                <button @click.stop="editGroup(group)" class="control-btn edit" title="수정">
                  ✏️
                </button>
                <button @click.stop="deleteGroup(group)" class="control-btn delete" title="삭제">
                  🗑️
                </button>
              </div>
            </div>
          </div>
        </div>

        <!-- 선택된 그룹의 옵션 아이템 목록 -->
        <div v-if="selectedGroup" class="option-items-section">
          <div class="section-header">
            <h4>{{ selectedGroup.name }} - 옵션 아이템</h4>
            <button @click="showAddItemModal = true" class="add-item-btn">
              ➕ 아이템 추가
            </button>
          </div>

          <div v-if="selectedGroup.items.length === 0" class="no-items">
            옵션 아이템이 없습니다. 새 아이템을 추가해보세요.
          </div>

          <div v-else class="items-list">
            <div 
              v-for="item in selectedGroup.items" 
              :key="item.id"
              class="item-card"
              :class="{ 'inactive': !item.isActive }"
            >
              <div class="item-info">
                <div class="item-header">
                  <h6 class="item-name">{{ item.name }}</h6>
                  <div class="item-price">
                    <span v-if="item.additionalPrice > 0">
                      +{{ formatPrice(item.additionalPrice) }}원
                    </span>
                    <span v-else class="free">무료</span>
                  </div>
                </div>

                <div class="item-details">
                  <span class="order">순서: {{ item.displayOrder }}</span>
                  <span :class="['status', item.isActive ? 'active' : 'inactive']">
                    {{ item.isActive ? '활성' : '비활성' }}
                  </span>
                </div>
              </div>

              <div class="item-controls">
                <button @click="editItem(item)" class="control-btn edit" title="수정">
                  ✏️
                </button>
                <button 
                  @click="toggleItemStatus(item)" 
                  class="control-btn toggle" 
                  :title="item.isActive ? '비활성화' : '활성화'"
                >
                  {{ item.isActive ? '⏸️' : '▶️' }}
                </button>
                <button @click="deleteItem(item)" class="control-btn delete" title="삭제">
                  🗑️
                </button>
              </div>
            </div>
          </div>
        </div>

        <!-- 선택된 그룹이 없을 때 -->
        <div v-else-if="optionGroups.length > 0" class="no-selection">
          옵션 그룹을 선택하면 아이템을 관리할 수 있습니다.
        </div>
      </div>

      <!-- 모달 액션 -->
      <div class="modal-footer">
        <button @click="$emit('close')" class="close-modal-btn">
          완료
        </button>
      </div>
    </div>

    <!-- 하위 모달들 -->
    <OptionGroupModal
      v-if="showAddGroupModal || showEditGroupModal"
      :group="editingGroup"
      :is-edit="showEditGroupModal"
      @close="closeGroupModal"
      @save="saveGroup"
    />

    <OptionItemModal
      v-if="showAddItemModal || showEditItemModal"
      :item="editingItem"
      :is-edit="showEditItemModal"
      @close="closeItemModal"
      @save="saveItem"
    />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { menuApi } from '@/api/owner/menuApi'
import OptionGroupModal from './OptionGroupModal.vue'
import OptionItemModal from './OptionItemModal.vue'

const props = defineProps({
  menu: {
    type: Object,
    required: true
  }
})

const emit = defineEmits(['close', 'refresh'])

// 상태 관리
const optionGroups = ref([])
const selectedGroup = ref(null)
const showAddGroupModal = ref(false)
const showEditGroupModal = ref(false)
const showAddItemModal = ref(false)
const showEditItemModal = ref(false)
const editingGroup = ref(null)
const editingItem = ref(null)

// 데이터 로딩
const loadOptionGroups = async () => {
  try {
    optionGroups.value = await menuApi.getOptionGroups(props.menu.id)
    optionGroups.value.sort((a, b) => a.displayOrder - b.displayOrder)
    
    // 첫 번째 그룹 자동 선택
    if (optionGroups.value.length > 0 && !selectedGroup.value) {
      selectedGroup.value = optionGroups.value[0]
    }
  } catch (error) {
    console.error('옵션 그룹 로드 실패:', error)
  }
}

// 그룹 관리
const selectGroup = (group) => {
  selectedGroup.value = group
}

const editGroup = (group) => {
  editingGroup.value = { ...group }
  showEditGroupModal.value = true
}

const saveGroup = async (groupData) => {
  try {
    if (showEditGroupModal.value) {
      await menuApi.updateOptionGroup(props.menu.id, editingGroup.value.id, groupData)
      alert('옵션 그룹이 수정되었습니다!')
    } else {
      await menuApi.createOptionGroup(props.menu.id, groupData)
      alert('옵션 그룹이 추가되었습니다!')
    }
    
    closeGroupModal()
    await loadOptionGroups()
    
  } catch (error) {
    console.error('옵션 그룹 저장 실패:', error)
    alert('옵션 그룹 저장에 실패했습니다: ' + error.message)
  }
}

const deleteGroup = async (group) => {
  if (!confirm(`"${group.name}" 옵션 그룹을 삭제하시겠습니까?\n포함된 모든 아이템도 함께 삭제됩니다.`)) {
    return
  }
  
  try {
    await menuApi.deleteOptionGroup(props.menu.id, group.id)
    alert('옵션 그룹이 삭제되었습니다!')
    
    // 삭제된 그룹이 선택된 그룹이면 선택 해제
    if (selectedGroup.value?.id === group.id) {
      selectedGroup.value = null
    }
    
    await loadOptionGroups()
    
  } catch (error) {
    console.error('옵션 그룹 삭제 실패:', error)
    alert('옵션 그룹 삭제에 실패했습니다: ' + error.message)
  }
}

// 아이템 관리
const editItem = (item) => {
  editingItem.value = { ...item }
  showEditItemModal.value = true
}

const saveItem = async (itemData) => {
  try {
    if (showEditItemModal.value) {
      await menuApi.updateOptionItem(
        props.menu.id, 
        selectedGroup.value.id, 
        editingItem.value.id, 
        itemData
      )
      alert('옵션 아이템이 수정되었습니다!')
    } else {
      await menuApi.createOptionItem(props.menu.id, selectedGroup.value.id, itemData)
      alert('옵션 아이템이 추가되었습니다!')
    }
    
    closeItemModal()
    await loadOptionGroups()
    
  } catch (error) {
    console.error('옵션 아이템 저장 실패:', error)
    alert('옵션 아이템 저장에 실패했습니다: ' + error.message)
  }
}

const toggleItemStatus = async (item) => {
  const action = item.isActive ? '비활성화' : '활성화'
  
  if (!confirm(`"${item.name}" 아이템을 ${action}하시겠습니까?`)) {
    return
  }
  
  try {
    await menuApi.updateOptionItemStatus(
      props.menu.id,
      selectedGroup.value.id,
      item.id,
      { 
        isActive: !item.isActive,
        reason: `관리자에 의한 ${action}`
      }
    )
    
    alert(`아이템이 ${action}되었습니다!`)
    await loadOptionGroups()
    
  } catch (error) {
    console.error('아이템 상태 변경 실패:', error)
    alert('상태 변경에 실패했습니다: ' + error.message)
  }
}

const deleteItem = async (item) => {
  if (!confirm(`"${item.name}" 아이템을 삭제하시겠습니까?`)) {
    return
  }
  
  try {
    await menuApi.deleteOptionItem(props.menu.id, selectedGroup.value.id, item.id)
    alert('옵션 아이템이 삭제되었습니다!')
    await loadOptionGroups()
    
  } catch (error) {
    console.error('옵션 아이템 삭제 실패:', error)
    alert('옵션 아이템 삭제에 실패했습니다: ' + error.message)
  }
}

// 모달 관리
const closeGroupModal = () => {
  showAddGroupModal.value = false
  showEditGroupModal.value = false
  editingGroup.value = null
}

const closeItemModal = () => {
  showAddItemModal.value = false
  showEditItemModal.value = false
  editingItem.value = null
}

// 유틸리티 함수
const getTypeText = (type) => {
  return type === 'SINGLE' ? '단일선택' : '다중선택'
}

const formatPrice = (price) => {
  return Number(price).toLocaleString()
}

// 컴포넌트 마운트
onMounted(() => {
  loadOptionGroups()
})
</script>

<style scoped>
.options-modal {
  max-width: 900px;
  max-height: 85vh;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 1.5rem;
  border-bottom: 1px solid #e5e7eb;
}

.header-info h3 {
  font-size: 20px;
  font-weight: 600;
  color: #111827;
  margin: 0 0 0.25rem 0;
}

.menu-name {
  font-size: 14px;
  color: #6b7280;
  margin: 0;
}

.modal-body {
  padding: 1.5rem;
  max-height: 60vh;
  overflow-y: auto;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
  padding-bottom: 0.5rem;
  border-bottom: 1px solid #f3f4f6;
}

.section-header h4 {
  font-size: 16px;
  font-weight: 600;
  color: #111827;
  margin: 0;
}

.add-group-btn, .add-item-btn {
  background: #3b82f6;
  color: white;
  border: none;
  padding: 0.5rem 0.75rem;
  border-radius: 6px;
  cursor: pointer;
  font-size: 12px;
  font-weight: 500;
  transition: background-color 0.2s;
}

.add-group-btn:hover, .add-item-btn:hover {
  background: #2563eb;
}

.option-groups-section {
  margin-bottom: 2rem;
}

.no-groups, .no-items, .no-selection {
  text-align: center;
  padding: 2rem;
  color: #6b7280;
  background: #f9fafb;
  border-radius: 8px;
  border: 1px dashed #d1d5db;
  font-style: italic;
}

.groups-list {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.group-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem;
  border: 2px solid #e5e7eb;
  border-radius: 8px;
  background: white;
  cursor: pointer;
  transition: all 0.2s;
}

.group-card:hover {
  border-color: #3b82f6;
}

.group-card.selected {
  border-color: #3b82f6;
  background: #f0f9ff;
}

.group-card.invalid {
  border-color: #ef4444;
  background: #fef2f2;
}

.group-info {
  flex: 1;
}

.group-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.5rem;
}

.group-name {
  font-size: 16px;
  font-weight: 600;
  color: #111827;
  margin: 0;
}

.group-badges {
  display: flex;
  gap: 0.25rem;
}

.type-badge {
  padding: 0.125rem 0.5rem;
  border-radius: 12px;
  font-size: 10px;
  font-weight: 600;
}

.type-badge.single {
  background: #dbeafe;
  color: #1e40af;
}

.type-badge.multiple {
  background: #f3e8ff;
  color: #7c2d12;
}

.required-badge {
  background: #fef3c7;
  color: #92400e;
  padding: 0.125rem 0.5rem;
  border-radius: 12px;
  font-size: 10px;
  font-weight: 600;
}

.group-stats {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.5rem;
  font-size: 12px;
  color: #6b7280;
}

.group-status {
  font-size: 12px;
  color: #059669;
}

.group-status.error {
  color: #dc2626;
  font-weight: 500;
}

.group-controls {
  display: flex;
  gap: 0.5rem;
}

.items-list {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.item-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.75rem;
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  background: white;
  transition: border-color 0.2s;
}

.item-card:hover {
  border-color: #d1d5db;
}

.item-card.inactive {
  opacity: 0.6;
  background: #f9fafb;
}

.item-info {
  flex: 1;
}

.item-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.25rem;
}

.item-name {
  font-size: 14px;
  font-weight: 500;
  color: #111827;
  margin: 0;
}

.item-price {
  font-size: 13px;
  font-weight: 600;
  color: #059669;
}

.item-price .free {
  color: #6b7280;
}

.item-details {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 11px;
  color: #6b7280;
}

.item-details .status.active {
  color: #059669;
}

.item-details .status.inactive {
  color: #dc2626;
}

.item-controls {
  display: flex;
  gap: 0.25rem;
}

.control-btn {
  background: none;
  border: 1px solid #d1d5db;
  padding: 0.25rem;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
}

.control-btn:hover {
  transform: scale(1.05);
}

.control-btn.edit:hover {
  background: #dbeafe;
  border-color: #3b82f6;
}

.control-btn.toggle:hover {
  background: #fef3c7;
  border-color: #f59e0b;
}

.control-btn.delete:hover {
  background: #fee2e2;
  border-color: #ef4444;
}

.modal-footer {
  padding: 1rem 1.5rem;
  border-top: 1px solid #e5e7eb;
  display: flex;
  justify-content: flex-end;
}

.close-modal-btn {
  background: #3b82f6;
  color: white;
  border: none;
  padding: 0.75rem 1.5rem;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: background-color 0.2s;
}

.close-modal-btn:hover {
  background: #2563eb;
}

/* 반응형 */
@media (max-width: 768px) {
  .options-modal {
    width: 95%;
    max-height: 90vh;
  }
  
  .modal-body {
    max-height: 70vh;
  }
  
  .group-card, .item-card {
    flex-direction: column;
    gap: 0.75rem;
    align-items: stretch;
  }
  
  .group-controls, .item-controls {
    justify-content: center;
  }
  
  .section-header {
    flex-direction: column;
    gap: 0.75rem;
    align-items: stretch;
  }
}
</style>