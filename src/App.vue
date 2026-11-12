<template>
  <v-app>
    <v-app-bar color="primary" flat>
      <v-app-bar-title>LendMe</v-app-bar-title>
      <v-btn :to="{ name: 'home' }" variant="text">Home</v-btn>
      <v-btn :to="{ name: 'about' }" variant="text">About</v-btn>
    </v-app-bar>

    <v-main>
      <router-view />
    </v-main>
  </v-app>
</template>
