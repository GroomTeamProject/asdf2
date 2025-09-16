<script setup>
import { ref, onMounted } from "vue";
import { useRouter } from "vue-router";

const router = useRouter();
const orders = ref([]);

// 페이지 로드 시 로컬 스토리지에서 주문 정보 가져오기
onMounted(() => {
    const savedOrders = JSON.parse(localStorage.getItem("cartForPayment") || "[]");

    // 각 주문에 상태 추가 (초기 상태: 조리중)
    orders.value = savedOrders.map(item => ({
        ...item,
        status: STATUS_LIST[0],
        canceled: false, // 취소 여부
    }));

    // 상태 자동 업데이트
    orders.value.forEach((order, index) => {
        let step = 0;
        const interval = setInterval(() => {
            if (order.canceled) {
                clearInterval(interval); // 취소된 주문은 업데이트 중지
                return;
            }

            step++;
            if (step < STATUS_LIST.length) {
                order.status = STATUS_LIST[step];
            } else {
                clearInterval(interval);
            }
        }, 3000 + index * 1000);
    });
});

// 주문 취소
const cancelOrder = (order) => {
    const confirmed = confirm(`${order.name} 주문을 취소하시겠습니까?`);
    if (!confirmed) return;

    order.canceled = true;
    orders.value = orders.value.filter(o => o !== order);
    localStorage.setItem("cartForPayment", JSON.stringify(orders.value));
};

// 뒤로가기
const goBack = () => router.push("/customer");
</script>

<template>
    <div class="max-w-md mx-auto p-4">
        <h1 class="text-2xl font-bold mb-4">주문 상태 확인</h1>

        <div v-if="orders.length === 0" class="text-center py-12 text-gray-600">
            주문 내역이 없습니다.
        </div>

        <div v-else class="space-y-4">
            <div v-for="item in orders" :key="item.id"
                class="border p-4 rounded bg-white flex justify-between items-center">
                <div>
                    <h2 class="font-semibold">{{ item.name }}</h2>
                    <p>수량: {{ item.quantity }}</p>
                    <p>가격: {{ (item.price * item.quantity).toLocaleString() }}원</p>
                </div>
                <div class="text-right">
                    <p>상태:
                        <span :class="{
                            'text-yellow-500': item.status === '조리중',
                            'text-blue-500': item.status === '배달중',
                            'text-green-500': item.status === '배달 완료'
                        }">
                            {{ item.status }}
                        </span>
                    </p>
                    <p>총: {{ (item.price * item.quantity).toLocaleString() }}원</p>
                    <button @click="cancelOrder(item)"
                        class="mt-2 px-3 py-1 bg-red-500 text-white rounded hover:bg-red-600">
                        주문 취소
                    </button>
                </div>
            </div>

            <p class="text-right font-bold mt-2">
                총 금액: {{orders.reduce((sum, item) => sum + item.price * item.quantity, 0).toLocaleString()}}원
            </p>
        </div>

        <button @click="goBack" class="mt-6 w-full bg-blue-500 text-white py-2 rounded hover:bg-blue-600">
            뒤로가기
        </button>
    </div>
</template>

<style scoped>
/* 필요하면 스타일 조정 가능 */
</style>
