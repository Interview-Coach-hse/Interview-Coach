# TODO

- Вернуть observability stack в `docker compose`: `prometheus`, `grafana`, `loki`, `promtail`, `tempo`, `otel-collector`.
- Подключить production-ready конфиг для OpenTelemetry, а не только локальный compose-сценарий.
- Добавить базовые Grafana dashboards для приложения, Postgres и JVM.
- Завести alerting через `Alertmanager` и позже подключить уведомления в Telegram.
- Перенести инфраструктуру в Helm/k8s values для удалённого сервера.
- Вернуть Java toolchain в `21`, когда локальная среда и CI будут на JDK 21.
