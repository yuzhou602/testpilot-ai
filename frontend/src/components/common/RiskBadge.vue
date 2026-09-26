<template>
  <span class="risk" :class="`risk-${level.toLowerCase()}`">{{ level }}</span>
</template>

<script setup lang="ts">
defineProps<{ level: string }>()
</script>
