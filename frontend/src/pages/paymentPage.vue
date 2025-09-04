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
            <h2 class="font-semibold mb-2">주문 상품</h2>
            <ul>
                <li v-for="item in items" :key="item.id" class="flex justify-between mb-1">
                    <span>{{ item.menu_name }} x {{ item.quantity }}</span>
                    <span>{{ item.total_price.toLocaleString() }}원</span>
                </li>
            </ul>
            <p class="text-right font-bold mt-2">총 {{ totalAmount.toLocaleString() }}원</p>
        </div>

        <!-- 주문 버튼 -->
        <button @click="placeOrder" class="w-full bg-green-500 text-white py-2 rounded hover:bg-green-600">
            주문하기
        </button>
    </div>
</template>

<script>
import axios from "axios";

export default {
    name: "OrderCheckout",
    props: {
        items: {
            type: Array,
            required: true, // 메뉴 정보는 부모 컴포넌트에서 받아옴
        },
    },
    data() {
        return {
            deliveryAddress: "",
            detailAddress: "",
            phone: "",
            orderMemo: "",
        };
    },
    computed: {
        totalAmount() {
            return this.items.reduce((sum, item) => sum + item.total_price, 0);
        },
    },
    methods: {
        placeOrder() {
            if (!this.deliveryAddress || !this.phone) {
                alert("주소와 전화번호는 필수입니다.");
                return;
            }

            const orderData = {
                delivery_address: this.deliveryAddress,
                delivery_detail_address: this.detailAddress,
                phone: this.phone,
                order_memo: this.orderMemo,
                total_amount: this.totalAmount,
                items: this.items.map((i) => ({
                    menu_id: i.id,
                    quantity: i.quantity,
                    total_price: i.total_price,
                    options: i.options || [],
                })),
            };

            // 백엔드 API 호출
            axios.post("http://localhost:8080/api/orders", orderData)
                .then(res => {
                    alert("주문이 완료되었습니다!");
                    // 결제 완료 후 페이지 이동 등 처리
                })
                .catch(err => {
                    console.error(err);
                    alert("주문 중 오류가 발생했습니다.");
                });
        },
    },
};
</script>
