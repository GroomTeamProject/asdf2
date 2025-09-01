<script setup>
import { computed } from 'vue'

const currentYear = computed(() => new Date().getFullYear())
</script>

<template>
  <footer class="mt-auto flex w-full flex-col items-center justify-center bg-neutral-900 py-6 text-neutral-200">
    <nav class="mb-2 flex gap-6">
      <router-link to="#" class="text-sm hover:underline" tabindex="-1" aria-disabled="true"> About </router-link>
      <router-link to="#" class="text-sm hover:underline" tabindex="-1" aria-disabled="true"> Privacy </router-link>
      <router-link to="#" class="text-sm hover:underline" tabindex="-1" aria-disabled="true"> Contact </router-link>
    </nav>
    <span class="text-xs text-neutral-400">© {{ currentYear }} team02. All rights reserved.</span>
  </footer>
</template>
