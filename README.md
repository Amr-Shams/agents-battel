# battle-of-agents

Fan-out prompt router. One prompt → N personality-configured agents → DeepSeek API → collect + compare responses.

## local start as always
```bash
echo "DEEPSEEK_API_KEY=sk-..." > .env
docker compose up --build
```

Open http://localhost:8080. All services start in dependency order:
1. RabbitMQ + Redis
2. 3 agents (healthcheck passes when actuator + RabbitMQ connection is UP)
3. prompt-service + response-service
4. Frontend



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
|---|---|---|
| prompt-service | 8003 | REST entry point, publishes to prompt.exchange |
| response-service | 8004 | Consumes response.exchange, Redis CRUD, GET endpoint |
| agent-service | 8005 | Message-driven, calls DeepSeek API, publishes result |
| frontend | 8080 | Nginx reverse proxy + static assets |

## Environment variables

| Variable | Required | Default | Used by |
|---|---|---|---|
| `DEEPSEEK_API_KEY` | yes | — | agent-service (DeepSeek auth) |
| `RABBITMQ_HOST` | no | localhost | all services |
| `REDIS_HOST` | no | localhost | response-service |
| `AGENT_TYPE` | yes | — | agent-service (group name, must match config) |
| `AGENT_CONFIG_PATH` | no | /etc/agent/agent.yml | agent-service |

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

## Frontend game

After all 3 agents respond, the user picks the best answer. Scores persist per session (in-memory JS, reset on page reload).
