<template>
  <div class="border border-gray-400 rounded-lg p-4 bg-white">
    <div class="flex items-center justify-between mb-4">
      <div>
        <h3>주문 #{{ order.id }}</h3>
        <p class="text-sm text-gray-600">고객: {{ order.customerName }}</p>
        <p class="text-sm text-gray-600">주문 시간: {{ order.orderTime }}</p>
      </div>
      <div class="text-right">
        <span class="inline-flex px-2 py-1 text-xs rounded border border-gray-400 bg-gray-200 text-gray-800">
          {{ getStatusText(order.status) }}
        </span>
        <p class="mt-1">{{ formatPrice(order.total) }}</p>
      </div>
    </div>
    
    <div class="mb-4">
      <h4 class="mb-2">주문 내역</h4>
      <div 
        v-for="item in order.items" 
        :key="item.name"
        class="flex justify-between text-sm"
      >
        <span>{{ item.name }} x{{ item.quantity }}</span>
        <span>{{ formatPrice(item.price * item.quantity) }}</span>
      </div>
    </div>

    <div class="flex gap-2">
      <button 
        v-if="order.status === 'pending'"
        @click="updateStatus('preparing')"
        class="bg-gray-600 text-white px-3 py-1 rounded text-sm hover:bg-gray-700"
      >
        조리 시작
      </button>
      <button 
        v-if="order.status === 'preparing'"
        @click="updateStatus('ready')"
        class="bg-gray-600 text-white px-3 py-1 rounded text-sm hover:bg-gray-700"
      >
        조리 완료
      </button>
      <button 
        v-if="order.status === 'ready'"
        @click="updateStatus('delivered')"
        class="bg-gray-600 text-white px-3 py-1 rounded text-sm hover:bg-gray-700"
      >
        배달 시작
      </button>
    </div>
  </div>
</template>

<script setup>
const props = defineProps({
  order: {
    type: Object,
    required: true
  }
})

const emit = defineEmits(['update-status'])

const updateStatus = (status) => {
  emit('update-status', { orderId: props.order.id, status })
}

const formatPrice = (price) => {
  return `${(price || 0).toLocaleString()}원`
}

const getStatusText = (status) => {
  const statusMap = {
    'pending': '주문 접수',
    'preparing': '조리 중',
    'ready': '조리 완료',
    'delivered': '배달 완료'
  }
  return statusMap[status] || status
}
</script>