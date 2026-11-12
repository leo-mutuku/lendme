# LendMe — Frontend

Vue 3 single-page app for LendMe. The Java backend lives in a separate repository.

**Stack:** Vue 3 · Vite · Vuetify · Vue Router · Pinia

## Requirements

- Node.js 20.19+ or 22.12+

## Getting started

```bash
npm install
cp .env.example .env.local   # optional, adjust backend URL
npm run dev                  # http://localhost:5173
```

During development, requests to `/api/*` are proxied to the backend
(default `http://localhost:8080`, override with `VITE_API_PROXY_TARGET`).

## Scripts

| Command           | Description                       |
| ----------------- | --------------------------------- |
| `npm run dev`     | Start the dev server              |
| `npm run build`   | Production build into `dist/`     |
| `npm run preview` | Serve the production build        |

## Project structure

```
src/
├── assets/        static assets processed by Vite
├── components/    reusable components
├── plugins/       Vuetify setup
├── router/        Vue Router routes
├── stores/        Pinia stores
├── views/         route-level pages
├── App.vue
└── main.js
```

See [CONTRIBUTING.md](CONTRIBUTING.md) for the branching workflow.
