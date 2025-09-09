import axios from "axios";

// 백엔드 기본 URL
const api = axios.create({
    baseURL: "http://localhost:8080", // Spring Boot 서버 URL
    timeout: 5000,
});

// 결제 생성
export function createPayment(paymentData) {
    // paymentData = { orderId, orderName, amount, paymentMethod }
    return api.post("/api/payments", paymentData);
}

// 특정 결제 조회
export function getPayment(paymentId) {
    return api.get(`/api/payments/${paymentId}`);
}

// 결제 취소
export function cancelPayment(paymentId) {
    return api.post(`/api/payments/${paymentId}/cancel`);
}
