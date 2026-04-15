# GitHub Actions setup

В репозитории настроены два workflow:

- `ci.yml` - сборка и тесты на любой `push` и `pull_request`
- `deploy.yml` - build image и deploy в Kubernetes при `push` в `main`

Что нужно заполнить в GitHub:

## Secrets

Создай эти secrets в `Settings -> Secrets and variables -> Actions`:

- `KUBE_CONFIG`
  - Содержимое kubeconfig для целевого Kubernetes-кластера
  - Вставь полный YAML kubeconfig как secret value
- `POSTGRES_PASSWORD`
  - Пароль для PostgreSQL
  - Используется и postgres chart, и backend chart
- `APP_JWT_SECRET`
  - Значение для `APP_SECURITY_JWT_SECRET`
- `SPRING_MAIL_PASSWORD`
  - Пароль для SMTP
  - Если почта не нужна в окружении, можно оставить пустым

## Variables

Создай эти repository variables в `Settings -> Secrets and variables -> Actions -> Variables`:

- `KUBE_NAMESPACE`
  - Namespace для deploy
  - Пример: `interview-coach`
- `BACKEND_RELEASE_NAME`
  - Helm release name backend
  - Пример: `interview-coach-backend`
- `POSTGRES_RELEASE_NAME`
  - Helm release name postgres
  - Пример: `interview-coach-postgres`
- `ALERTMANAGER_RELEASE_NAME`
  - Helm release name alertmanager
  - Пример: `interview-coach-alertmanager`
- `IMAGE_NAME`
  - Имя Docker image без registry
  - Пример: `interview-coach-backend`

## Registry

Workflow настроен на публикацию образа в GHCR:

- registry: `ghcr.io`
- image: `ghcr.io/<owner>/<IMAGE_NAME>`

Для GHCR отдельные логин/пароль не нужны, используется `GITHUB_TOKEN`.

## Alertmanager

Для alertmanager используется готовый community chart:

- `prometheus-community/alertmanager`
- репозиторий добавляется прямо в `deploy.yml`

Локальный custom Helm chart для alertmanager в CI/CD не используется.

## Что при желании поменять

- если нужен другой registry, измени шаги `Login to registry` и `Build and push Docker image` в `deploy.yml`
- если нужен другой способ deploy, измени Helm-команды в `deploy.yml`
