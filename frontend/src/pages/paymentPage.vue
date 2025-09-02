<script setup>
import { ref, computed, onMounted } from "vue";
import { ArrowLeft } from "lucide-vue-next";
import { useRouter } from "vue-router";
import axios from "axios";

const router = useRouter();
const cart = ref([]);
const name = ref("");
const cardNumber = ref("");
const expiry = ref("");
const cvc = ref("");

// 장바구니 로드
onMounted(() => {
    cart.value = JSON.parse(localStorage.getItem("cart") || "[]");
});

// 총 금액
const totalPrice = computed(() =>
    cart.value.reduce((sum, item) => sum + item.price * item.quantity, 0)
);

// 주문/결제
const placeOrder = async () => {
    if (!name.value || !cardNumber.value || !expiry.value || !cvc.value) {
        alert("모든 결제 정보를 입력해주세요.");
        return;
    }

    if (cart.value.length === 0) {
        alert("장바구니가 비어있습니다.");
        return;
    }

    try {
        const res = await axios.post('http://localhost:3000/api/payment', {
            cart: cart.value,
            customer: {
                name: name.value,
                cardNumber: cardNumber.value,
                expiry: expiry.value,
                cvc: cvc.value
            }
        });

        alert(`총 ${totalPrice.value.toLocaleString()}원 결제 완료!\n결제 ID: ${res.data.paymentId}`);
        localStorage.removeItem("cart");
        router.push("/");
    } catch (err) {
        console.error(err);
        alert("결제 실패, 서버 에러 발생!");
    }
};

const goBack = () => router.push("/cart");
</script>

<template>
    <div class="min-h-screen p-4 bg-gray-100">
        <header class="flex items-center gap-4 p-4 bg-white border-b">
            <button @click="goBack" class="flex items-center gap-2 px-2 py-1 border rounded hover:bg-gray-200">
                <ArrowLeft class="w-4 h-4" /> 돌아가기
            </button>
            <h1 class="text-xl font-bold">결제 페이지</h1>
        </header>

        <main class="mt-4 max-w-lg mx-auto bg-white p-6 rounded shadow space-y-4">
            <h2 class="text-lg font-semibold">주문 금액: {{ totalPrice.toLocaleString() }}원</h2>

            <div class="space-y-2">
                <label>이름</label>
                <input v-model="name" type="text" class="w-full border rounded px-2 py-1" />
            </div>

            <div class="space-y-2">
                <label>카드 번호</label>
                <input v-model="cardNumber" type="text" placeholder="1234-5678-9012-3456"
                    class="w-full border rounded px-2 py-1" />
            </div>

            <div class="flex gap-2">
                <div class="flex-1 space-y-2">
                    <label>유효기간</label>
                    <input v-model="expiry" type="text" placeholder="MM/YY" class="w-full border rounded px-2 py-1" />
                </div>
                <div class="flex-1 space-y-2">
                    <label>CVC</label>
                    <input v-model="cvc" type="text" placeholder="123" class="w-full border rounded px-2 py-1" />
                </div>
            </div>

            <button @click="placeOrder" class="w-full py-2 bg-blue-500 text-white rounded hover:bg-blue-600">
                결제하기
            </button>
        </main>
    </div>
</template>
