import { ref, computed } from 'vue'
import { defineStore } from 'pinia'

// Example store — replace with real stores (e.g. auth, loans) as features land.
export const useCounterStore = defineStore('counter', () => {
  const count = ref(0)
  const doubleCount = computed(() => count.value * 2)

  function increment() {
    count.value++
  }

  return { count, doubleCount, increment }
})
