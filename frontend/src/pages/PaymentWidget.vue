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
            결제하기 ({{ displayAmount.toLocaleString() }}원)
        </button>
    </div>
</template>

<script setup>
import { ref, onMounted, computed, watch } from "vue";

const couponApplied = ref(false);
const widgetsReady = ref(false);
let widgets = null;

const orderInfo = JSON.parse(localStorage.getItem("orderInfo") || "{}");
const baseAmount = ref(orderInfo?.totalAmount || 0);
const displayAmount = computed(() => (couponApplied.value ? baseAmount.value - 5000 : baseAmount.value));

onMounted(async () => {
    if (!window.TossPayments) return;

    const tossPayments = window.TossPayments("test_gck_docs_Ovk5rk1EwkEbP0W43n07xlzm");
    const customerKey = "Pu_CmJW3lO06qzdfilC8J";
    widgets = tossPayments.widgets({ customerKey });

    await widgets.setAmount({ currency: "KRW", value: displayAmount.value });

    await Promise.all([
        widgets.renderPaymentMethods({ selector: "#payment-method", variantKey: "DEFAULT" }),
        widgets.renderAgreement({ selector: "#agreement", variantKey: "AGREEMENT" }),
    ]);

    widgetsReady.value = true;
});

watch(couponApplied, async (newVal) => {
    if (!widgets) return;
    await widgets.setAmount({ currency: "KRW", value: displayAmount.value });
});

// 결제 요청 (결제 완료 후 SuccessPage로 redirect)
const requestPayment = async () => {
    if (!widgets) return;

    await widgets.requestPayment({
        orderId: "order-" + Date.now(),
        orderName: "장바구니 주문",
        successUrl: window.location.origin + "/success", // ✅ redirect
        failUrl: window.location.origin + "/fail",
        customerEmail: orderInfo.customerEmail || "customer123@gmail.com",
        customerName: orderInfo.customerName || "홍길동",
        customerMobilePhone: orderInfo.phoneNumber || "01012341234",
    });
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
