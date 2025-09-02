<script setup>
import { useCounterStore } from '../stores/counter'

const counterStore = useCounterStore()
</script>

<template>

    <body class="min-h-screen bg-gray-100">
        <!-- 메인 역할 선택 페이지 -->
        <div class="min-h-screen bg-gray-100 flex items-center justify-center p-4">
            <!-- 메인 역할 선택 카드 -->
            <div class="w-full max-w-md wireframe-card">
                <!-- 헤더: 서비스 제목과 설명 -->
                <div class="text-center border-b border-gray-300 p-6">
                    <h1 class="text-3xl text-gray-800 mb-2">QuickDeliver</h1>
                    <p class="text-gray-600">
                        배달 서비스 플랫폼
                    </p>
                </div>

                <!-- 콘텐츠: 역할 선택 버튼들 -->
                <div class="space-y-4 p-6">
                    <div class="grid gap-3">
                        <!-- 고객 역할 선택 버튼 -->
                        <button @click="goRole('customer')">고객으로 시작</button>

                        <!-- 점주 역할 선택 버튼 -->
                        <button onclick="navigateToRole('owner')" class="w-full h-12 wireframe-button rounded-md">
                            점주로 시작하기
                        </button>

                        <!-- 배달원 역할 선택 버튼 -->
                        <button onclick="navigateToRole('driver')" class="w-full h-12 wireframe-button rounded-md">
                            배달원으로 시작하기
                        </button>

                        <!-- 구분선 및 플로우 다이어그램 버튼 -->
                        <div class="border-t border-gray-300 pt-3 mt-4">
                            <button onclick="navigateToFlow()"
                                class="w-full h-12 border-2 border-gray-600 bg-gray-50 text-gray-800 hover:bg-gray-100 rounded-md transition-colors">
                                📋 플로우 다이어그램 보기
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </body>
</template>

<script>
export default {
    methods: {
        goRole(role) {
            switch (role) {
                case 'customer':
                    this.$router.push('/customer')
                    break
                case 'owner':
                    this.$router.push('/owner')
                    break
                case 'driver':
                    this.$router.push('/driver')
                    break
            }
        },
        goFlow() {
            this.$router.push('/flow')
        }
    }
}
</script>