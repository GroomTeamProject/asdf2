<script setup>
import { ref, computed, onMounted } from "vue";
import { useRouter } from "vue-router";

const router = useRouter();

// 주문 상품 불러오기
const items = ref([]);

// 주문 정보
const deliveryAddress = ref("");
const detailAddress = ref("");
const phone = ref("");
const orderMemo = ref("");

// 마운트 시 localStorage에서 장바구니 불러오기
onMounted(() => {
    items.value = JSON.parse(localStorage.getItem("cartForCheckout") || "[]");
});

// 총 금액 계산
const totalAmount = computed(() =>
    items.value.reduce((sum, item) => sum + item.total_price, 0)
);

// 결제 페이지 이동
const goToPayment = () => {
    // DB 연동하게 되면 여기서 axios.post("/api/orders", {...}) 하면 돼
    localStorage.setItem(
        "orderInfo",
        JSON.stringify({
            items: items.value,
            deliveryAddress: deliveryAddress.value,
            detailAddress: detailAddress.value,
            phone: phone.value,
            orderMemo: orderMemo.value,
            totalAmount: totalAmount.value,
        })
    );

    router.push("/payment"); // PaymentWidget.vue로 이동
};
</script>

<template>
    <div class="max-w-md mx-auto p-4">
        <h1 class="text-2xl font-bold mb-4">주문 결제</h1>

        <!-- 배달 주소 -->
        <div class="mb-4">
            <label class="block font-semibold mb-1">배달 주소</label>
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
                    <span>{{ item.menu_name }} x {{ item.quantity }}</span>
                    <span>{{ item.total_price.toLocaleString() }}원</span>
                </li>
            </ul>
            <p class="text-right font-bold mt-2">
                총 {{ totalAmount.toLocaleString() }}원
            </p>
        </div>

        <!-- 결제 페이지 이동 버튼 -->
        <button @click="goToPayment" class="w-full bg-blue-500 text-white py-2 rounded hover:bg-blue-600">
            결제하기
        </button>
    </div>
</template>
