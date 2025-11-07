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

onMounted(async () => {
  try {
    // localStorage에서 orderInfo 읽기 (안정성 있게 파싱)
    const rawOrderInfo = localStorage.getItem("orderInfo") || "{}";
    const orderInfo = (() => {
      try { return JSON.parse(rawOrderInfo); } catch (e) { return {}; }
    })();

    orderItems.value = orderInfo.items || [];

    // 쿼리: orderId 또는 orderNumber 둘 다 체크
    const orderIdParam = route.query.orderId || route.query.orderNumber || "";
    // amount 또는 totalAmount
    const amountFromQuery = route.query.amount || route.query.totalAmount || "0";
    // paymentKey (토스 위젯에서 넘겨주는 키)
    const paymentKey = route.query.paymentKey || "";

    // amount 숫자 변환 (안전)
    amount.value = parseInt(String(amountFromQuery || "0").replace(/\D/g, ""), 10) || 0;

    // storeName 결정 (쿼리 > localStorage.orderInfo.storeName > orderItems[0] > fallback)
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

    // 디버그 로그 (문제 있을 때 복사해서 붙여넣기 쉬움)
    console.log("SuccessPage mounted");
    console.log("route.query:", route.query);
    console.log("orderInfo (localStorage):", orderInfo);
    console.log("detected orderIdParam:", orderIdParam);
    console.log("detected paymentKey:", paymentKey);
    console.log("detected amount:", amount.value);
    console.log("detected storeName:", storeName.value);

    if (!paymentKey) {
      message.value = "결제 키가 존재하지 않습니다. 다시 시도해주세요.";
      loading.value = false;
      return;
    }

    // JWT가 있으면 넣고, 없으면 서버에서 인증 없이 처리하도록 서버가 허용해야 함
    const jwt = localStorage.getItem("jwt");

    const payload = {
      paymentKey: paymentKey,
      amount: amount.value,
      orderId: orderIdParam,
    };

    // POST 호출 — JWT가 있을 때만 Authorization 헤더 추가
    const resp = await axios.post(
        `http://localhost:8084/api/payments/confirm`,
        payload,
        {
          headers: {
            "Content-Type": "application/json",
            ...(jwt ? { Authorization: `Bearer ${jwt}` } : {}),
          },
          timeout: 15000,
        }
    );

    // 성공 처리
    console.log("결제 confirm 응답:", resp?.data);

    // orderId로 보일 값: 쿼리에서 온 값(없으면 빈값)
    orderId.value = orderIdParam || null;

    // 장바구니/주문정보 정리
    ["cart", "orderInfo"].forEach((key) => localStorage.removeItem(key));

    message.value = "주문과 결제가 완료되었습니다! 잠시 후 완료 페이지로 이동합니다.";

    // order-complete로 보낼 데이터 구성 (문자열로 보낼 값들은 URLSearchParams가 인코딩 처리)
    const orderCompleteData = {
      orderNumber: orderIdParam || "",
      totalAmount: String(amount.value || 0),
      storeName: storeName.value || "알 수 없는 가게",
      deliveryAddress: orderInfo?.deliveryAddress || orderInfo?.deliveryAddr || "",
      phoneNumber: orderInfo?.phoneNumber || orderInfo?.phone || "",
    };

    const queryParams = new URLSearchParams(orderCompleteData);

    // 1초 뒤 이동 (요청한 동작)
    setTimeout(() => {
      router.push(`/customer/order-complete?${queryParams.toString()}`);
    }, 1000);
  } catch (err) {
    console.warn("Toss API 호출 실패:", err);

    if (err.response) {
      console.error("응답 status:", err.response.status);
      console.error("응답 data:", err.response.data);

      if (err.response.status === 401) {
        message.value = "인증이 실패했습니다. 다시 로그인해주세요.";
        setTimeout(() => router.push("/login"), 1400);
      } else if (err.response.status === 403) {
        message.value = "권한이 없습니다(403). 관리자에게 문의하세요.";
      } else {
        message.value = `결제 확인 중 오류가 발생했습니다. 상태코드: ${err.response.status}`;
      }
    } else if (err.request) {
      console.error("요청은 보냈지만 응답이 없습니다:", err.request);
      message.value = "서버 응답이 없습니다. 네트워크 또는 서버를 확인하세요.";
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
        <li
            v-for="(item, index) in orderItems"
            :key="index"
            class="flex justify-between mb-1"
        >
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
