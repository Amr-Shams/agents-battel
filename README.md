# agents-battel

microservice arch project, that separate the deployment form the volume(state) and keeps the arch scalabel as much as possible.
solving non-byzantine issues, and covering the fundmental battle between languges.i used the cheapest model outhere(i am borke tbh).
you are open to fork and make your own oc(but mention me ;)

as a local first advocate, the setup is quite simple 
## local start as always
```bash
echo "DEEPSEEK_API_KEY=sk-..." > .env
docker compose up --build
```

Open http://localhost:8080. and enjoy the game

## Arch 
<p align="center">
  <img src="docs/arch.png" alt="Architecture" width="700"/>
</p>

## Agent configuration

Each agent loads a YAML config mounted,  remember to include **make no mistake** `AGENT_CONFIG_PATH`:

```yaml
agent:
  type: java
  system-prompt: |
    You are a senior Java developer with expert knowledge of Spring Boot, Hibernate,
    Jakarta EE, and JVM internals. Think in terms of object-oriented design...
  model: deepseek-chat
  temperature: 0.2
  max-tokens: 1000
```

Built-in configs in `configs/`:

| Agent | File | Personality |
|---|---|---|
| Java | `configs/agent-java.yml` | OOP, Spring Boot, JVM internals, production-ready code |
| Go | `configs/agent-golang.yml` | Goroutines, channels, composition, stdlib, minimalism |
| Rust | `configs/agent-rust.yml` | Ownership, lifetimes, zero-cost abstractions, safety |

Add a new agent: create a config file, add a service block in `docker-compose.yml` or a ConfigMap + Deployment in `k8s/`.

## Services

| Service | Port | Role |
|---|---|---|---|
| discovery-service | 8761 | Eureka service registry |
| gateway-service | 8060 | Spring Cloud Gateway (routes via Eureka) |
| prompt-service | 8003 | REST entry point, publishes to prompt.exchange |
| response-service | 8004 | Consumes response.exchange, Redis CRUD, GET endpoint |
| agent-service | 8005 | Message-driven, calls DeepSeek API, publishes result |
| frontend | 8080 | Nginx reverse proxy → gateway-service |
| prometheus | 9090 | Metrics collection (scrapes /actuator/prometheus) |
| grafana | 3000 | Dashboards (Prometheus + Loki datasources) |
| loki | 3100 | Centralized log aggregation |
| rabbitmq | 5672 / 15672 | Message broker / Management UI |
| redis | 6379 | Response cache (HSET/HGETALL) |

## Environment variables

| Variable | Required | Default | Used by |
|---|---|---|---|
| `DEEPSEEK_API_KEY` | yes | — | agent-service (DeepSeek auth) |
| `RABBITMQ_HOST` | no | localhost | all services |
| `REDIS_HOST` | no | localhost | response-service |
| `AGENT_TYPE` | yes | — | agent-service (group name, must match config) |
| `AGENT_CONFIG_PATH` | no | /etc/agent/agent.yml | agent-service |
| `EUREKA_CLIENT_SERVICEURL_DEFAULTZONE` | no | http://localhost:8761/eureka/ | all services |

## Monitoring

All Spring Boot services expose `/actuator/prometheus` for Prometheus scraping.
Prometheus scrapes via static targets in `monitoring/prometheus.yml`.

- **Prometheus** → http://localhost:9090
- **Grafana** → http://localhost:3000 (admin/admin)
- **Loki** → http://localhost:3100 (log queries from Grafana)

Promtail reads Docker container logs and ships them to Loki.
Grafana comes pre-configured with Prometheus and Loki datasources.

## Deployment

### Docker Compose (dev)

```bash
docker compose up --build
```

### Kubernetes (k8s)

```bash
kubectl apply -k k8s/
```

Requires a Secret with `deepseek-api-key`:

```bash
kubectl create secret generic deepseek-api-key --from-literal=api-key=sk-... -n prompts
```
