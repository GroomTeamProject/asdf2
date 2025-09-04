<script setup>
import { ref, computed, onMounted } from "vue";
import { useRouter } from "vue-router";
import axios from "axios";

const router = useRouter();

// 결제용 장바구니
const cart = ref([]);

// 결제 정보
const name = ref("");
const cardNumber = ref("");
const expiry = ref("");
const cvc = ref("");

// 배송 정보
const deliveryAddress = ref("");
const detailAddress = ref("");
const phone = ref("");
const orderMemo = ref("");

// 장바구니 로드
onMounted(() => {
    cart.value = JSON.parse(localStorage.getItem("cartForPayment") || "[]");
});

// 총 금액 계산
const totalAmount = computed(() =>
    cart.value.reduce((sum, item) => sum + item.price * item.quantity, 0)
);

// 결제/주문
const placeOrder = async () => {
    if (!name.value || !cardNumber.value || !expiry.value || !cvc.value) {
        alert("모든 결제 정보를 입력해주세요.");
        return;
    }

    if (!deliveryAddress.value || !phone.value) {
        alert("주소와 전화번호는 필수입니다.");
        return;
    }

    if (cart.value.length === 0) {
        alert("장바구니가 비어있습니다.");
        return;
    }

    const orderData = {
        customer: {
            name: name.value,
            cardNumber: cardNumber.value,
            expiry: expiry.value,
            cvc: cvc.value,
        },
        delivery: {
            address: deliveryAddress.value,
            detail: detailAddress.value,
            phone: phone.value,
        },
        orderMemo: orderMemo.value,
        items: cart.value.map((i) => ({
            menu_id: i.id,
            menu_name: i.name,
            quantity: i.quantity,
            total_price: i.price * i.quantity,
            options: i.options || [],
        })),
        totalAmount: totalAmount.value,
    };

    try {
        const res = await axios.post("http://localhost:8080/api/orders", orderData);
        alert(`주문 완료! 총 ${totalAmount.value.toLocaleString()}원 결제됨.`);
        localStorage.removeItem("cart");
        localStorage.removeItem("cartForPayment");
        router.push("/"); // 메인 페이지로 이동
    } catch (err) {
        console.error(err);
        alert("주문 중 오류가 발생했습니다.");
    }
};

// 뒤로가기
const goBack = () => router.push("/cart");
</script>

<template>
    <div class="max-w-md mx-auto p-4">
        <h1 class="text-2xl font-bold mb-4">주문 결제</h1>

        <!-- 배송 정보 -->
        <div class="mb-4">
            <label class="block font-semibold mb-1">배달 주소</label>
            <input v-model="deliveryAddress" type="text" placeholder="도로명 주소"
                class="w-full border px-3 py-2 rounded mb-2" />
            <input v-model="detailAddress" type="text" placeholder="상세 주소" class="w-full border px-3 py-2 rounded" />
        </div>

        <div class="mb-4">
            <label class="block font-semibold mb-1">전화번호</label>
            <input v-model="phone" type="tel" placeholder="010-1234-5678" class="w-full border px-3 py-2 rounded" />
        </div>

        <!-- 결제 정보 -->
        <div class="mb-4">
            <label class="block font-semibold mb-1">이름</label>
            <input v-model="name" type="text" placeholder="홍길동" class="w-full border px-3 py-2 rounded mb-2" />
            <label class="block font-semibold mb-1">카드 번호</label>
            <input v-model="cardNumber" type="text" placeholder="1234-5678-9012-3456"
                class="w-full border px-3 py-2 rounded mb-2" />
            <label class="block font-semibold mb-1">유효기간</label>
            <input v-model="expiry" type="text" placeholder="MM/YY" class="w-full border px-3 py-2 rounded mb-2" />
            <label class="block font-semibold mb-1">CVC</label>
            <input v-model="cvc" type="text" placeholder="123" class="w-full border px-3 py-2 rounded" />
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
            <p class="text-right font-bold mt-2">총 {{ totalAmount.toLocaleString() }}원</p>
        </div>

        <!-- 주문 버튼 -->
        <button @click="placeOrder" class="w-full bg-green-500 text-white py-2 rounded hover:bg-green-600">
            주문하기
        </button>

        <!-- 뒤로가기 -->
        <button @click="goBack" class="mt-2 w-full bg-gray-300 text-gray-800 py-2 rounded hover:bg-gray-400">
            장바구니로 돌아가기
        </button>
    </div>
</template>

<style scoped>
/* 필요하면 추가 스타일 */
</style>
