<script setup>
import { ref, onMounted } from "vue";
import axios from "axios";

const orderInfo = ref({});
const user = ref(null);
const orderId = ref(`Order-${Date.now()}`);
const amount = ref(0);
const widgetsReady = ref(false);
let widgets = null;

onMounted(async () => {
  // 1. URL에서 주문 정보 가져오기
  const params = new URLSearchParams(window.location.search);
  const orderNumber = params.get("orderNumber");
  const totalAmount = params.get("totalAmount");
  const storeName = params.get("storeName");
  const deliveryAddress = params.get("deliveryAddress");
  const phoneNumber = params.get("phoneNumber");

  // 2. localStorage에 저장
  orderInfo.value = {
    orderIdString: orderNumber,
    totalAmount: Number(totalAmount),
    storeName,
    deliveryAddress,
    phoneNumber,
    items: [] // 필요 시 API에서 주문 아이템 받아서 넣으면 됨
  };
  localStorage.setItem("orderInfo", JSON.stringify(orderInfo.value));

  amount.value = Number(totalAmount);

  // 3. 사용자 정보 가져오기
  try {
    const res = await axios.get("/api/user/me");
    user.value = res.data;
  } catch (err) {
    console.error("사용자 정보 가져오기 실패", err);
  }

  // 4. Toss 결제 위젯 초기화
  if (!window.TossPayments) return;

  const tossPayments = window.TossPayments("test_gck_docs_Ovk5rk1EwkEbP0W43n07xlzm");
  const customerKey = "Pu_CmJW3lO06qzdfilC8J";
  widgets = tossPayments.widgets({ customerKey });

  await widgets.setAmount({ currency: "KRW", value: amount.value });

  await Promise.all([
    widgets.renderPaymentMethods({ selector: "#payment-method", variantKey: "DEFAULT" }),
    widgets.renderAgreement({ selector: "#agreement", variantKey: "AGREEMENT" }),
  ]);

  widgetsReady.value = true;
});

const requestPayment = async () => {
  if (!widgets) return;

  if (!orderInfo.value.orderIdString) {
    alert("주문 정보가 없습니다!");
    return;
  }

  await widgets.requestPayment({
    orderId: orderInfo.value.orderIdString,
    orderName: orderInfo.value.items.map(item => item.name).join(", ") || orderInfo.value.storeName,
    successUrl: window.location.origin + "/success",
    failUrl: window.location.origin + "/fail",
    customerEmail: user.value?.email,
    customerName: user.value?.name,
    customerMobilePhone: orderInfo.value.phoneNumber,
  });
};
</script>

<template>
  <div class="payment-container">
    <div id="payment-method"></div>
    <div id="agreement"></div>
    <button class="button" :disabled="!widgetsReady" @click="requestPayment">
      결제하기 ({{ amount.toLocaleString() }}원)
    </button>
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
</style>
