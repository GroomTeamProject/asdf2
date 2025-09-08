<template>
    <div>
        <h2>결제 성공</h2>
        <p>paymentKey: {{ paymentKey }}</p>
        <p>주문번호: {{ orderId }}</p>
        <p>결제 금액: {{ amount }}</p>
    </div>
</template>

<script setup>
import { ref, onMounted } from "vue";

const paymentKey = ref("");
const orderId = ref("");
const amount = ref("");

onMounted(async () => {
    const urlParams = new URLSearchParams(window.location.search);
    paymentKey.value = urlParams.get("paymentKey");
    orderId.value = urlParams.get("orderId");
    amount.value = Number(urlParams.get("amount")) || 0;


    const requestData = {
        paymentKey: paymentKey.value,
        orderId: orderId.value,
        amount: amount.value,
    };

    try {
        const response = await fetch("http://localhost:3000/confirm", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(requestData)
        });

        const json = await response.json();

        if (!response.ok || json.result === "ERROR") {
            window.location.href = `/fail?message=${json.error?.message || "알 수 없는 오류"}&code=${json.error?.code || "UNKNOWN"}`;
        }


        console.log(json); // 성공 시 처리 로직
        // 5초 뒤 customer 페이지로 이동
        setTimeout(() => {
            router.push("/customer");
        }, 5000);
        
    } catch (err) {
        console.error("결제 확인 중 오류 발생:", err);
        window.location.href = `/fail?message=${encodeURIComponent(json.error?.message)}&code=${encodeURIComponent(json.error?.code)}`;

    }
});
</script>

<style scoped>
/* 필요하면 스타일 추가 */
</style>
