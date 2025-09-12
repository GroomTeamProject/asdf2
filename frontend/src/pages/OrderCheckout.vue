<script setup>
import { ref, computed, onMounted } from "vue";

const items = ref([]);
const customerName = ref("");
const deliveryAddress = ref("");
const detailAddress = ref("");
const phone = ref("");
const orderMemo = ref("");

onMounted(() => {
    items.value = JSON.parse(localStorage.getItem("cartForCheckout") || "[]");
});

const totalAmount = computed(() =>
    items.value.reduce((sum, item) => sum + item.price * item.quantity, 0)
);

const goToPayment = () => {
    const fullAddress = `${deliveryAddress.value} ${detailAddress.value}`.trim();

    // 주문 정보 localStorage에 저장
    localStorage.setItem(
        "orderInfo",
        JSON.stringify({
            customerName: customerName.value,
            phoneNumber: phone.value,
            address: fullAddress,
            requestMessage: orderMemo.value,
            totalAmount: totalAmount.value,
            items: items.value.map(item => ({
                productName: item.menu_name || item.productName,
                quantity: item.quantity,
                price: item.price,
            }))
        })
    );

    // Toss 결제 페이지로 이동
    window.location.href = "/payment"; // PaymentPage.vue에서 처리
};
</script>


<template>
    <div class="max-w-md mx-auto p-4">
        <h1 class="text-2xl font-bold mb-4">주문 결제</h1>

        <!-- 이름 -->
        <div class="mb-4">
            <label class="block font-semibold mb-1">이름</label>
            <input v-model="customerName" type="text" placeholder="홍길동" class="w-full border px-3 py-2 rounded" />
        </div>

        <!-- 주소 -->
        <div class="mb-4">
            <label class="block font-semibold mb-1">주소</label>
            <input v-model="deliveryAddress" type="text" placeholder="도로명 주소"
                class="w-full border px-3 py-2 rounded mb-2" />
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
                <li v-for="item in items" :key="item.id" class="flex justify-between mb-1">
                    <span>{{ item.menu_name || item.productName }} x {{ item.quantity }}</span>
                    <span>{{ (item.price * item.quantity).toLocaleString() }}원</span>
                </li>
            </ul>
            <p class="text-right font-bold mt-2">
                총 {{ totalAmount.toLocaleString() }}원
            </p>
        </div>

        <button @click="goToPayment" class="w-full bg-blue-500 text-white py-2 rounded hover:bg-blue-600">
            결제하기
        </button>
    </div>
</template>
