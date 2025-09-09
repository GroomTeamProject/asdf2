<script setup>
import { ref, computed, onMounted } from "vue";
import { useRouter } from "vue-router";
import axios from "axios";

const router = useRouter();
const cart = ref([]);
const name = ref("");
const cardNumber = ref("");
const expiry = ref("");
const cvc = ref("");
const deliveryAddress = ref("");
const detailAddress = ref("");
const phone = ref("");
const orderMemo = ref("");

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
    if (!deliveryAddress.value || !phone.value) {
        alert("주소와 전화번호는 필수입니다.");
        return;
    }

    if (cart.value.length === 0) {
        alert("장바구니가 비어있습니다.");
        return;
    }

    const orderData = {
        deliveryAddress: deliveryAddress.value,
        detailAddress: detailAddress.value,
        phone: phone.value,
        orderMemo: orderMemo.value,
        totalAmount: totalPrice.value,
        items: cart.value.map((item) => ({
            menuId: item.id,
            quantity: item.quantity,
            totalPrice: item.price * item.quantity,
            options: item.options || [],
        })),
    };

    try {
        // 1️⃣ 주문 생성
        const orderRes = await axios.post("http://localhost:8080/api/orders", orderData);
        const orderId = orderRes.data.id;

        // 2️⃣ 결제 생성
        const paymentRes = await axios.post("http://localhost:8080/api/payments", {
            orderId,
            orderName: "주문 상품", // 필요 시 실제 상품명 넣기
            amount: totalPrice.value,
            paymentMethod: "CARD",
        });

        alert(`총 ${totalPrice.value.toLocaleString()}원 결제 완료!\n결제 ID: ${paymentRes.data.id}`);
        localStorage.removeItem("cart");
        router.push("/");
    } catch (err) {
        console.error(err);
        alert("주문/결제 중 오류가 발생했습니다.");
    }
};

const goBack = () => router.push("/cart");
</script>

<template>
    <div class="max-w-md mx-auto p-4">
        <h1 class="text-2xl font-bold mb-4">주문 결제</h1>

        <!-- 배달 주소 -->
        <div class="mb-4">
            <label class="block font-semibold mb-1">배달 주소</label>
            <input v-model="deliveryAddress" type="text" placeholder="도로명 주소"
                class="w-full border px-3 py-2 mb-2 rounded" />
            <input v-model="detailAddress" type="text" placeholder="상세 주소" class="w-full border px-3 py-2 rounded" />
        </div>

        <!-- 전화번호 -->
        <div class="mb-4">
            <label class="block font-semibold mb-1">전화번호</label>
            <input v-model="phone" type="tel" placeholder="010-1234-5678" class="w-full border px-3 py-2 rounded" />
        </div>

        <!-- 주문 메모 -->
        <div class="mb-4">
            <label class="block font-semibold mb-1">주문 메모</label>
            <textarea v-model="orderMemo" placeholder="예: 매운맛 빼주세요" class="w-full border px-3 py-2 rounded"></textarea>
        </div>

        <!-- 주문 상품 요약 -->
        <div class="mb-4">
            <h2 class="font-semibold mb-2">주문 상품</h2>
            <ul>
                <li v-for="item in cart" :key="item.id" class="flex justify-between mb-1">
                    <span>{{ item.name }} x {{ item.quantity }}</span>
                    <span>{{ (item.price * item.quantity).toLocaleString() }}원</span>
                </li>
            </ul>
            <p class="text-right font-bold mt-2">총 {{ totalPrice.toLocaleString() }}원</p>
        </div>

        <!-- 주문 버튼 -->
        <button @click="placeOrder" class="w-full bg-green-500 text-white py-2 rounded hover:bg-green-600">
            주문하기
        </button>
    </div>
</template>
