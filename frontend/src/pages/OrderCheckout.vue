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

    // 필수값 체크
    if (!customerName.value || !phone.value || !fullAddress || items.value.length === 0) {
        alert("필수 입력값을 모두 입력해주세요.");
        return;
    }

    const orderData = {
        customerName: customerName.value,
        phoneNumber: phone.value,
        address: fullAddress,
        requestMessage: orderMemo.value,
        totalAmount: totalAmount.value,
        items: items.value.map(item => ({
            menuId: item.id,
            productName: item.name,
            quantity: item.quantity,
            price: item.price
        }))
    };

    try {
        // 1️⃣ 주문 생성
        const response = await axios.post("http://localhost:8080/api/orders/create", orderData);
        const savedOrder = response.data; // 서버에서 { orderId, orderIdString } 반환 가정
        console.log("주문 생성 응답:", savedOrder);

        // 2️⃣ 로컬스토리지에 주문 정보 저장
        localStorage.setItem(
            "orderInfo",
            JSON.stringify({
                ...orderData,
                orderId: savedOrder.orderId,
                orderIdString: savedOrder.orderIdString
            })
        );

        // 3️⃣ 장바구니 초기화
        localStorage.removeItem("cartForCheckout");

        // 4️⃣ Toss 결제 호출 준비
        // 예시: Toss 결제 파라미터 구성
        const tossPaymentParams = {
            amount: totalAmount.value,
            orderId: savedOrder.orderIdString,
            orderName: `주문 #${savedOrder.orderId}`,
            customerName: customerName.value,
            successUrl: `${window.location.origin}/payment/success`,
            failUrl: `${window.location.origin}/payment/fail`,
        };

        // 실제 Toss 위젯 호출
        // tossPayments.requestPayment(tossPaymentParams); 
        // 위 코드는 Toss SDK를 프론트에 import 하고 사용해야 함

        // 5️⃣ 결제 페이지로 이동 (위젯 미사용시)
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
