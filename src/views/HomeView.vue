<script setup>
import { useCounterStore } from '@/stores/counter'

const counter = useCounterStore()
</script>

<template>
  <v-container class="py-8">
    <h1 class="text-h4 mb-4">Welcome to LendMe</h1>

    <v-card max-width="400">
      <v-card-title>Pinia example</v-card-title>
      <v-card-text>
        Count: {{ counter.count }} (double: {{ counter.doubleCount }})
      </v-card-text>
      <v-card-actions>
        <v-btn color="primary" prepend-icon="mdi-plus" @click="counter.increment">
          Increment
        </v-btn>
      </v-card-actions>
    </v-card>
  </v-container>
</template>
