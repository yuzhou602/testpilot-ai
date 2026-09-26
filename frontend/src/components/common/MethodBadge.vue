<template>
  <span class="method" :class="`method-${method.toLowerCase()}`">{{ method }}</span>
</template>

<script setup lang="ts">
defineProps<{ method: string }>()
</script>
