<script setup>
import { ref, computed, onMounted } from "vue";
import { useRouter } from "vue-router";
import axios from "axios";

const router = useRouter();

const items = ref([]);
const customerName = ref("");
const deliveryAddress = ref("");
const detailAddress = ref("");
const phone = ref("");
const orderMemo = ref("");

onMounted(() => {
    items.value = JSON.parse(localStorage.getItem("cartForCheckout") || "[]");
    console.log("장바구니 데이터 확인:", items.value);
});

const totalAmount = computed(() =>
    items.value.reduce((sum, item) => sum + item.price * item.quantity, 0)
);

const goToPayment = async () => {
    const fullAddress = `${deliveryAddress.value} ${detailAddress.value}`.trim();

    const orderData = {
        customerName: customerName.value,
        phoneNumber: phone.value,
        address: fullAddress,
        requestMessage: orderMemo.value,
        totalAmount: totalAmount.value,
        items: items.value.map(item => ({
            menuId: item.id,
            name: item.name,
            productId: item.id,
            productName: item.name,
            price: item.price,
            quantity: item.quantity
        }))
    };

    try {
        // 주문 생성
        const response = await axios.post("http://localhost:8080/api/orders/create", orderData);
        const savedOrder = response.data;
        console.log("주문 생성 응답:", savedOrder);

        // localStorage에 저장 (orderId 포함)
        localStorage.setItem(
            "orderInfo",
            JSON.stringify({
                ...orderData,
                orderId: savedOrder.id,
                orderIdString: `order-${savedOrder.id}` // Toss 규칙에 맞게 가공
            })
        );

        // 장바구니 초기화
        localStorage.removeItem("cartForCheckout");

        // Payment 페이지 이동
        router.push("/payment");
    } catch (err) {
        console.error("주문 생성 실패:", err);
        alert("주문 생성 중 오류가 발생했습니다.");
    }
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
            <input v-model="phone" type="tel" placeholder="01012345678" class="w-full border px-3 py-2 rounded" />
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
                    <span>{{ item.name || item.productName }} x {{ item.quantity }}</span>
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
