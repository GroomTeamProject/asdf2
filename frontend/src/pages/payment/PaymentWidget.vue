<script setup>
import { ref, onMounted } from "vue";
import axios from "axios";

const orderInfo = JSON.parse(localStorage.getItem("orderInfo") || "{}");
const user = ref(null);
const orderId = `Order-${Date.now()}`;
const amount = ref(orderInfo.totalAmount || 0);
const widgetsReady = ref(false);
let widgets = null;

const paymentStatus = ref(""); // 결제 상태 표시
const paymentDetails = ref(null); // 결제 상세 정보

// SSE 연결
const connectSSE = () => {
  // Authorization 필요 시 쿼리 파라미터로 전달
  const token = localStorage.getItem("accessToken"); // JWT 토큰
  const sseUrl = `${import.meta.env.VITE_API_URL}/payments/sse/connect${token ? "?token=" + token : ""}`;
  console.log("[SSE] 연결 시도 URL:", sseUrl);
  const eventSource = new EventSource(sseUrl);

  eventSource.onmessage = (event) => {
    const data = JSON.parse(event.data);
    paymentStatus.value = data.status;
    paymentDetails.value = data;

    console.log("[SSE] 결제 이벤트 수신:", data);
  };

  eventSource.onerror = (err) => {
    console.error("[SSE] 연결 오류", err);
    eventSource.close();
  };
};

onMounted(async () => {
  if (!window.TossPayments) return;

  // 유저 정보 조회
  const res = await axios.get("/api/user/me");
  user.value = res.data;

  const tossPayments = window.TossPayments("test_gck_docs_Ovk5rk1EwkEbP0W43n07xlzm");
  const customerKey = "Pu_CmJW3lO06qzdfilC8J";
  widgets = tossPayments.widgets({ customerKey });

  await widgets.setAmount({ currency: "KRW", value: amount.value });

  await Promise.all([
    widgets.renderPaymentMethods({ selector: "#payment-method", variantKey: "DEFAULT" }),
    widgets.renderAgreement({ selector: "#agreement", variantKey: "AGREEMENT" }),
  ]);

  widgetsReady.value = true;

  // SSE 연결
  connectSSE();
});

const requestPayment = async () => {
  if (!widgets) return;

  if (!orderInfo.orderIdString) {
    alert("주문 정보가 없습니다!");
    return;
  }

  try {
    await widgets.requestPayment({
      orderId: orderId,
      orderName: orderInfo.items.map(item => item.name).join(", "),
      successUrl: window.location.origin + "/success",
      failUrl: window.location.origin + "/fail",
      customerEmail: user.value.email,
      customerName: user.value.name,
      customerMobilePhone: orderInfo.phoneNumber,
    });

    paymentStatus.value = "PROCESSING"; // 결제 요청 후 상태 표시

  } catch (error) {
    console.error("결제 요청 실패", error);
    paymentStatus.value = "FAILED";
  }
};
</script>

<template>
  <div class="payment-container">
    <!-- 결제 UI -->
    <div id="payment-method"></div>

    <!-- 이용약관 UI -->
    <div id="agreement"></div>

    <!-- 결제하기 버튼 -->
    <button class="button" :disabled="!widgetsReady" @click="requestPayment">
      결제하기 ({{ amount.toLocaleString() }}원)
    </button>

    <!-- 결제 상태 표시 -->
    <div v-if="paymentStatus" class="status">
      결제 상태: {{ paymentStatus }}
      <pre v-if="paymentDetails">{{ JSON.stringify(paymentDetails, null, 2) }}</pre>
    </div>
  </div>
</template>

<style scoped>
.payment-container {
  margin: 20px;
}

.button {
  margin-top: 30px;
  padding: 10px 20px;
  background: #0064ff;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
}

.button:disabled {
  background: #999;
  cursor: not-allowed;
}

.status {
  margin-top: 20px;
  padding: 10px;
  background: #f2f2f2;
  border-radius: 6px;
}
</style>
