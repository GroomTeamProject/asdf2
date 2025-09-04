// src/api/payments.js
import axios from "axios";

// 백엔드 기본 URL
const api = axios.create({
    baseURL: "http://localhost:8080", // 백엔드 포트 확인!
    timeout: 5000,
});

// 결제 생성
export function createPayment({ orderId, paymentKey, method, amount, pgProvider, pgTid }) {
    return api.post("/api/payments", null, {
        params: {
            orderId,
            paymentKey,
            method,
            amount,
            pgProvider,
            pgTid,
        },
    });
}

// 특정 결제 조회
export function getPayment(paymentId) {
    return api.get(`/api/payments/${paymentId}`);
}

// 결제 취소
export function cancelPayment(paymentId) {
    return api.post(`/api/payments/${paymentId}/cancel`);
}
