<!-- src/pages/PaymentWidget.vue -->
<template>
    <div class="payment-container">
        <!-- 할인 쿠폰 -->
        <div>
            <input type="checkbox" id="coupon-box" v-model="couponApplied" />
            <label for="coupon-box">5,000원 쿠폰 적용</label>
        </div>

        <!-- 결제 UI -->
        <div id="payment-method"></div>

        <!-- 이용약관 UI -->
        <div id="agreement"></div>

        <!-- 결제하기 버튼 -->
        <button class="button" :disabled="!widgetsReady" @click="requestPayment">
            결제하기
        </button>
    </div>
</template>

<script setup>
import { ref, onMounted, watch } from "vue";

const couponApplied = ref(false);
const widgetsReady = ref(false);
let widgets = null;

onMounted(async () => {
    // SDK 확인
    if (!window.TossPayments) {
        console.error("TossPayments SDK가 로딩되지 않았습니다.");
        return;
    }

    // TossPayments 위젯 초기화
    const tossPayments = window.TossPayments("test_gck_docs_Ovk5rk1EwkEbP0W43n07xlzm");

    // 회원 결제용 customerKey
    const customerKey = "Pu_CmJW3lO06qzdfilC8J";
    widgets = tossPayments.widgets({ customerKey });

    // 결제 금액 세팅
    await widgets.setAmount({ currency: "KRW", value: 50000 });

    // UI 렌더링
    await Promise.all([
        widgets.renderPaymentMethods({
            selector: "#payment-method",
            variantKey: "DEFAULT",
        }),
        widgets.renderAgreement({
            selector: "#agreement",
            variantKey: "AGREEMENT",
        }),
    ]);

    widgetsReady.value = true; // 위젯 준비 완료
});

// 쿠폰 체크 시 결제 금액 변경
watch(couponApplied, async (newVal) => {
    if (!widgets) return;
    await widgets.setAmount({
        currency: "KRW",
        value: newVal ? 50000 - 5000 : 50000,
    });
});

// 결제 요청
const requestPayment = async () => {
    if (!widgets) return;

    try {
        await widgets.requestPayment({
            orderId: "order-" + Date.now(), // 고유 주문번호
            orderName: "토스 티셔츠 외 2건",
            successUrl: window.location.origin + "/success",
            failUrl: window.location.origin + "/fail",
            customerEmail: "customer123@gmail.com",
            customerName: "김토스",
            customerMobilePhone: "01012341234",
        });
    } catch (err) {
        console.error("결제 요청 실패:", err);
    }
};
</script>

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
