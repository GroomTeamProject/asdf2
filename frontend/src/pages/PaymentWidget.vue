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
import axios from "axios";

const couponApplied = ref(false);
const widgetsReady = ref(false);
let widgets = null;

onMounted(async () => {
    if (!window.TossPayments) {
        console.error("TossPayments SDK가 로딩되지 않았습니다.");
        return;
    }

    const tossPayments = window.TossPayments("test_gck_docs_Ovk5rk1EwkEbP0W43n07xlzm");
    const customerKey = "Pu_CmJW3lO06qzdfilC8J";
    widgets = tossPayments.widgets({ customerKey });

    await widgets.setAmount({ currency: "KRW", value: 50000 });

    await Promise.all([
        widgets.renderPaymentMethods({ selector: "#payment-method", variantKey: "DEFAULT" }),
        widgets.renderAgreement({ selector: "#agreement", variantKey: "AGREEMENT" }),
    ]);

    widgetsReady.value = true;
});

watch(couponApplied, async (newVal) => {
    if (!widgets) return;
    await widgets.setAmount({ currency: "KRW", value: newVal ? 45000 : 50000 });
});

const requestPayment = async () => {
    if (!widgets) return;

    try {
        const paymentResult = await widgets.requestPayment({
            orderId: "order-" + Date.now(),
            orderName: "토스 티셔츠 외 2건",
            successUrl: window.location.origin + "/success",
            failUrl: window.location.origin + "/fail",
            customerEmail: "customer123@gmail.com",
            customerName: "김토스",
            customerMobilePhone: "01012341234",
        });

        // 결제 완료 후 백엔드 저장
        await axios.post("http://localhost:8080/api/payments", {
            orderId: paymentResult.orderId,
            orderName: paymentResult.orderName,
            amount: paymentResult.amount.value,
            paymentMethod: paymentResult.method,
            pgProvider: paymentResult.pgProvider,
            pgTid: paymentResult.pgTid,
        });

        alert("결제가 완료되었습니다!");
    } catch (err) {
        console.error(err);
        alert("결제 실패!");
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
