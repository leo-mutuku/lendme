# Contributing to LendMe

## Branches

```
feature/xyz ──PR──► dev ──PR──► main
 (one task)        (testing)   (stable)
```

- **`main`**: stable, working code only. Protected; nobody pushes to it directly.
- **`dev`**: where finished features are combined and tested.
- **`feature/...` / `fix/...`**: one short-lived branch per task.

## One-time setup

```bash
git clone https://github.com/leo-mutuku/lendme.git
cd lendme
git checkout dev
```

## For every task

1. **Start from the latest `dev`**
   ```bash
   git checkout dev
   git pull
   ```
2. **Create a branch for the task**
   ```bash
   git checkout -b feature/short-description
   ```
   Use `feature/` for new work and `fix/` for bug fixes, e.g. `feature/loan-api`, `fix/login-bug`.
3. **Write code and commit**
   ```bash
   git add .
   git commit -m "Add loan creation endpoint"
   ```
4. **Push your branch**
   ```bash
   git push -u origin feature/short-description
   ```
5. **Open a pull request into `dev`** on GitHub, or run:
   ```bash
   gh pr create --base dev
   ```
6. After it's reviewed and merged, delete your branch and start again from step 1.

## Releases

When `dev` is stable, a pull request is opened from `dev` into `main`.

## Rules

- Never work directly on `main`.
- Always start a task from an up-to-date `dev`.
- Keep branches small: one task per branch.
- Never commit secrets (passwords, API keys, database credentials). This repo is public. Use environment variables instead.
