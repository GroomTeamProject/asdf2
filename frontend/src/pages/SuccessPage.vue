<script setup>
import { ref, onMounted } from "vue";
import { useRouter } from "vue-router";
import axios from "axios";

const router = useRouter();
const orderId = ref(null);
const amount = ref(0);
const message = ref("결제를 확인 중입니다...");
const orderItems = ref([]); // ✅ 주문 상품 배열 추가

onMounted(async () => {
    const orderInfo = JSON.parse(localStorage.getItem("orderInfo") || "{}");

    amount.value = orderInfo.totalAmount || 0;

    try {
        // 주문 생성 API 호출
        const orderResponse = await axios.post("http://localhost:8080/api/orders/create", orderInfo);
        orderId.value = orderResponse.data.id;

        // ✅ 주문 상품 배열 가져오기
        orderItems.value = orderInfo.items || [];

        console.log("주문번호: ", orderId.value);
        console.log("주문 상품:", orderItems.value);

        localStorage.removeItem("cart");
        localStorage.removeItem("orderInfo");

        message.value = "주문이 완료되었습니다! 5초 후 고객 페이지로 이동합니다.";
        setTimeout(() => router.push("/customer"), 5000);
    } catch (err) {
        console.error("❌ 주문 저장 중 오류 발생:", err);
        message.value = `주문 저장 중 오류가 발생했습니다: ${err.message}`;
    }
});
</script>

<template>
    <div class="payment-success p-6 text-center">
        <h2 class="text-2xl font-bold mb-4">결제 성공</h2>
        <p class="mb-2">주문번호: {{ orderId }}</p>
        <p class="mb-2">결제금액: {{ amount.toLocaleString() }}원</p>

        <!-- 주문 상품 목록 -->
        <div v-if="orderItems.length > 0" class="mt-4 text-left">
            <h3 class="font-semibold mb-2">주문 상품</h3>
            <ul>
                <li v-for="(item, index) in orderItems" :key="index" class="flex justify-between mb-1">
                    <span>{{ item.productName }} x {{ item.quantity }}</span>
                    <span>{{ (item.price * item.quantity).toLocaleString() }}원</span>
                </li>
            </ul>
        </div>

        <p class="text-lg text-blue-600 mt-4">{{ message }}</p>
    </div>
</template>

<style scoped>
.payment-success {
    max-width: 500px;
    margin: 50px auto;
    padding: 20px;
    border: 1px solid #ddd;
    border-radius: 8px;
    background-color: #f9f9f9;
}
</style>
