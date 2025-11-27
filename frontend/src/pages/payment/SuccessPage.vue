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
const storeName = ref("알 수 없는 가게");
const loading = ref(true);

// 환경변수 기반 API URL (끝 '/' 제거)
const API_BASE = import.meta.env.VITE_API_URL.replace(/\/$/, "");

onMounted(async () => {
  try {
    // localStorage에서 orderInfo 읽기
    const rawOrderInfo = localStorage.getItem("orderInfo") || "{}";
    const orderInfo = (() => {
      try { return JSON.parse(rawOrderInfo); } catch (e) { return {}; }
    })();

    orderItems.value = orderInfo.items || [];

    // 쿼리에서 값 읽기
    const orderIdParam = route.query.orderId || route.query.orderNumber || "";
    const amountFromQuery = route.query.amount || route.query.totalAmount || "0";
    const paymentKey = route.query.paymentKey || "";

    amount.value = parseInt(String(amountFromQuery).replace(/\D/g, ""), 10) || 0;

    // storeName 결정
    const rawStoreFromQuery = route.query.storeName || route.query.store || "";
    const decodedQueryStoreName = rawStoreFromQuery
        ? decodeURIComponent(String(rawStoreFromQuery).replace(/\+/g, " "))
        : "";
    const storeFromOrderInfo = orderInfo.storeName || orderInfo.store?.name || "";
    const storeFromItems =
        orderItems.value[0]?.storeName ||
        orderItems.value[0]?.storeInfo?.name ||
        orderItems.value[0]?.store?.name ||
        "";

    storeName.value =
        decodedQueryStoreName ||
        storeFromOrderInfo ||
        storeFromItems ||
        "알 수 없는 가게";

    console.log("SuccessPage mounted", { orderIdParam, paymentKey, amount: amount.value, storeName: storeName.value });

    if (!paymentKey) {
      message.value = "결제 키가 존재하지 않습니다. 다시 시도해주세요.";
      loading.value = false;
      return;
    }

    // JWT 있으면 넣기
    const jwt = localStorage.getItem("jwt");

    const payload = {
      paymentKey,
      amount: amount.value,
      orderId: orderIdParam,
    };

    // POST 호출
    const resp = await axios.post(
        `${API_BASE}/payments/confirm`,
        payload,
        {
          headers: {
            "Content-Type": "application/json",
            ...(jwt ? { Authorization: `Bearer ${jwt}` } : {}),
          },
          timeout: 15000,
          withCredentials: true, // Spring Security allowCredentials:true 맞춤
        }
    );

    console.log("결제 confirm 응답:", resp?.data);

    orderId.value = orderIdParam || null;

    // 로컬스토리지 정리
    ["cart", "orderInfo"].forEach(key => localStorage.removeItem(key));

    message.value = "주문과 결제가 완료되었습니다! 잠시 후 완료 페이지로 이동합니다.";

    // order-complete 페이지로 전달할 데이터
    const orderCompleteData = {
      orderNumber: orderIdParam || "",
      totalAmount: String(amount.value || 0),
      storeName: storeName.value,
      deliveryAddress: orderInfo?.deliveryAddress || orderInfo?.deliveryAddr || "",
      phoneNumber: orderInfo?.phoneNumber || orderInfo?.phone || "",
    };
    const queryParams = new URLSearchParams(orderCompleteData);

    setTimeout(() => {
      router.push(`/customer/order-complete?${queryParams.toString()}`);
    }, 1000);

  } catch (err) {
    console.warn("Toss API 호출 실패:", err);

    if (err.response) {
      console.error("응답 status:", err.response.status);
      console.error("응답 data:", err.response.data);

      if (err.response.status === 401) {
        message.value = "인증 실패. 다시 로그인해주세요.";
        setTimeout(() => router.push("/login"), 1400);
      } else if (err.response.status === 403) {
        message.value = "권한이 없습니다(403). 관리자에게 문의하세요.";
      } else {
        message.value = `결제 확인 중 오류가 발생했습니다. 상태코드: ${err.response.status}`;
      }
    } else if (err.request) {
      console.error("요청은 보냈지만 응답 없음:", err.request);
      message.value = "서버 응답이 없습니다. 네트워크 또는 서버 확인 필요.";
    } else {
      message.value = `오류 발생: ${err.message}`;
    }
  } finally {
    loading.value = false;
  }
});
</script>

<template>
  <div class="payment-success p-6 text-center">
    <h2 class="text-2xl font-bold mb-4">결제 성공</h2>

    <p v-if="orderId !== null" class="mb-2">주문 번호: {{ orderId }}</p>
    <p class="mb-2">가게: {{ storeName }}</p>
    <p class="mb-2">결제금액: {{ amount.toLocaleString() }}원</p>

    <div v-if="orderItems.length > 0" class="mt-4 text-left">
      <h3 class="font-semibold mb-2">주문 상품</h3>
      <ul>
        <li v-for="(item, index) in orderItems" :key="index" class="flex justify-between mb-1">
          <span>{{ item.productName || item.menuName || item.name }} x {{ item.quantity }}</span>
          <span>{{ ((item.price || item.menuPrice || 0) * item.quantity).toLocaleString() }}원</span>
        </li>
      </ul>
    </div>

    <p class="text-lg text-blue-600 mt-4">{{ message }}</p>
    <div v-if="loading" class="mt-4">처리 중입니다...</div>
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
