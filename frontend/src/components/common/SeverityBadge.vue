<template>
  <span class="severity" :class="`severity-${level.toLowerCase()}`">{{ level }}</span>
</template>

<script setup lang="ts">
defineProps<{ level: string }>()
</script>
