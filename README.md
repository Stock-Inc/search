# search

## DevOps

Запускается через Docker Compose:

```sh
cp .env.example .env
docker compose up --build
```

Сервисы:

- frontend: http://localhost
- Spring Boot backend: http://localhost:8080
- Swagger UI: http://localhost:8080/docs
- Elasticsearch: http://localhost:9200
- Prometheus: http://localhost:9090
- Grafana: http://localhost:3000

Тестовые документы можно загрузить после старта backend:

```sh
sh init.sh
```
## backend

```sh
docker compose up --build -d
```
Для тестирования:
- http://localhost:8080/swagger-ui/swagger-ui/index.html
