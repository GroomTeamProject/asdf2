<script setup>
import { ref, onMounted } from "vue";
import { useRouter } from "vue-router";
import axios from "axios";

const router = useRouter();
const orderId = ref(1);
const amount = ref(0);
const message = ref("결제를 확인 중입니다...");

onMounted(async () => {
    const orderInfo = JSON.parse(localStorage.getItem("orderInfo") || "{}");
    const cartItems = JSON.parse(localStorage.getItem("cart") || "[]");

    amount.value = orderInfo.totalAmount || 0;
    orderId.value = orderInfo.orderId || 1;

    const orderRequest = {
        customerName: orderInfo.customerName || "테스트 고객",
        phone: orderInfo.phoneNumber || "010-0000-0000", // DTO와 매칭
        address: orderInfo.address || "테스트 주소",
        requestMessage: orderInfo.requestMessage || "",
        totalAmount: amount.value,
        items: cartItems.map(item => ({
            productName: item.name, // DTO와 일치
            productId: item.productId || null,
            menuId: item.menuId || null,
            price: item.price,
            quantity: item.quantity
        }))
    };

    try {
        const orderResponse = await axios.post("http://localhost:8080/api/orders/create", orderInfo);
        const createdOrder = orderResponse.data;

        console.log("✅ 주문 저장 성공:", response.data);

        localStorage.removeItem("cart");

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
        <p class="mb-2">결제금액: {{ amount }}원</p>
        <p class="text-lg text-blue-600">{{ message }}</p>
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
