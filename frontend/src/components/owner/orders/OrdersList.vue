<template>
  <div class="border-2 border-gray-400 bg-white rounded-lg">
    <div class="border-b border-gray-300 p-4">
      <h2 class="text-gray-800">주문 관리</h2>
    </div>
    <div class="p-4">
      <div class="space-y-4">
        <OrderItem
          v-for="order in orders"
          :key="order.id"
          :order="order"
          @update-status="$emit('update-status', $event.orderId, $event.status)"
        />
        
        <!-- 주문이 없는 경우 -->
        <div v-if="!orders || orders.length === 0" class="text-center py-8 text-gray-500">
          <p>처리할 주문이 없습니다.</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import OrderItem from './OrderItem.vue'

const props = defineProps({
  orders: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['update-status'])
</script>