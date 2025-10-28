<template>
  <div class="fail-page">
    <h2>결제 실패</h2>

    <!-- SSE 또는 쿼리 파라미터에서 받은 실패 정보 -->
    <p v-if="code"><strong>에러코드:</strong> {{ code }}</p>
    <p v-if="message"><strong>실패 사유:</strong> {{ message }}</p>
    <p v-else>알 수 없는 이유로 결제에 실패했습니다.</p>

    <button @click="goHome">메인으로 돌아가기</button>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';

const route = useRoute();
const router = useRouter();

const code = ref('');
const message = ref('');

// SSE 연결 상태
let eventSource = null;

// 홈 이동
const goHome = () => {
  router.push('/customer');
};

onMounted(() => {
  // 1) Toss 콜백 쿼리 확인
  code.value = route.query.code || '';
  message.value = route.query.message || '';

  // 2) SSE 연결 (서버에서 실패 이벤트 수신)
  const jwt = localStorage.getItem("jwt");
  if (jwt) {
    const sseUrl = `${import.meta.env.VITE_API_URL}/payments/sse/connect?token=${jwt}`;
    eventSource = new EventSource(sseUrl);

    eventSource.onmessage = (event) => {
      const data = JSON.parse(event.data);

      if (data.status === 'FAILED') {
        code.value = data.paymentKey || 'UNKNOWN';
        message.value = data.status || '결제 실패';
        console.log('[SSE] 결제 실패 이벤트 수신:', data);

        // 5초 후 고객 페이지로 이동
        setTimeout(goHome, 5000);
        eventSource.close();
      }
    };

    eventSource.onerror = (err) => {
      console.error('[SSE] 연결 오류', err);
      eventSource.close();
    };
  }
});

</script>

<style scoped>
.fail-page {
  margin: 40px;
  text-align: center;
}

button {
  margin-top: 20px;
  padding: 10px 20px;
  background-color: #f44336;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
}

button:hover {
  opacity: 0.9;
}
</style>
