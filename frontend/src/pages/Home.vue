<script setup>
import { useCounterStore } from '../stores/counter'

const counterStore = useCounterStore()
</script>

<template>
  <div class="container mx-auto px-4 py-8">
    <h1 class="text-3xl font-bold mb-6">Hello world!</h1>
  </div>
  <div class="container mx-auto px-4 py-8">
    <button @click="counterStore.increment" class="bg-blue-500 text-white w-12 h-12 rounded-md">+</button>
    <button @click="counterStore.decrement" class="bg-red-500 text-white w-12 h-12 rounded-md">-</button>
    <p class="text-2xl font-bold">Count: {{ counterStore.count }}</p>
  </div>
</template>
