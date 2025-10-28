<script setup>
import { ref, onMounted } from "vue";
import { useRouter, useRoute } from "vue-router";
import axios from "axios";

const router = useRouter();
const route = useRoute();

const amount = ref(0);
const message = ref("결제를 확인 중입니다...");
const orderItems = ref([]);
const orderId = ref(null);
const paymentStatus = ref(""); // 결제 상태 (PROCESSING, COMPLETED, FAILED)
const paymentDetails = ref(null); // SSE로 받은 결제 이벤트 정보

// SSE 연결
const connectSSE = (jwt) => {
  const sseUrl = `${import.meta.env.VITE_API_URL}/payments/sse/connect${jwt ? "?token=" + jwt : ""}`;
  const eventSource = new EventSource(sseUrl);

  eventSource.onmessage = (event) => {
    const data = JSON.parse(event.data);
    paymentStatus.value = data.status;
    paymentDetails.value = data;

    console.log("[SSE] 결제 이벤트 수신:", data);

    if (data.status === "COMPLETED" || data.status === "FAILED") {
      message.value =
        data.status === "COMPLETED"
          ? "주문과 결제가 완료되었습니다! 5초 후 고객 페이지로 이동합니다."
          : "결제 실패! 다시 시도해주세요.";

      setTimeout(() => router.push("/customer"), 5000);
      eventSource.close();
    }
  };

  eventSource.onerror = (err) => {
    console.error("[SSE] 연결 오류", err);
    eventSource.close();
  };
};

onMounted(async () => {
  const orderInfo = JSON.parse(localStorage.getItem("orderInfo") || "{}");
  orderItems.value = orderInfo.items || [];

  const paymentKey = route.query.paymentKey;
  const orderIdParam = route.query.orderId;
  amount.value = parseInt(route.query.amount || "0", 10);

  if (!paymentKey) {
    message.value = "결제 키가 존재하지 않습니다. 다시 시도해주세요.";
    return;
  }

  const jwt = localStorage.getItem("jwt");
  if (!jwt) {
    message.value = "로그인이 필요합니다.";
    return;
  }

  // SSE 연결
  connectSSE(jwt);

  try {
    // 결제 콜백 API 호출
    await axios.post(`${import.meta.env.VITE_API_URL}/payments/callback`, {
      paymentKey: paymentKey,
      amount: amount.value,
      orderId: orderIdParam,
    }, {
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${jwt}`
      }
    });
  } catch (err) {
    console.warn("Toss API 호출 실패:", err.message);
    message.value = "결제 확인 중 오류가 발생했습니다.";
  } finally {
    orderId.value = orderIdParam || null;
    ["cart", "orderInfo"].forEach(key => localStorage.removeItem(key));
  }
});
</script>

<template>
  <div class="payment-success p-6 text-center">
    <h2 class="text-2xl font-bold mb-4">결제 상태 확인</h2>

    <p v-if="orderId !== null" class="mb-2">주문 번호: {{ orderId }}</p>
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

    <div v-if="paymentDetails" class="mt-4 p-2 bg-gray-100 rounded">
      <h4 class="font-semibold mb-2">결제 상세</h4>
      <pre>{{ JSON.stringify(paymentDetails, null, 2) }}</pre>
    </div>
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
