# battle-of-agents

Multi-agent AI prompt router. Submit one prompt, fan it out to N agents with different personalities, collect all responses.

Three agents built-in: Java, Go, and Rust -- each with its own character, tone, and expertise. Config driven from YAML files; same code, different personality per container.

### Prerequisites

- Docker and Docker Compose
- A DeepSeek API key (set as DEEPSEEK_API_KEY)

### Quick start

```bash
export DEEPSEEK_API_KEY=sk-your-key-here
docker compose up
```

This starts RabbitMQ, Redis, prompt-service (8003), response-service (8004), three agents, and a frontend at **http://localhost:8080**.

Open the browser, enter a prompt, and crown the winning agent.

### How it works

Send a prompt:

```bash
curl -X POST http://localhost:8003/api/prompts \
  -H "Content-Type: application/json" \
  -d '{"sessionId": "abc-123", "prompt": "Write a function that reverses a linked list"}'
```

Returns a prompt ID:

```json
{"id": "uuid-here", "timestamp": "2026-06-06T12:00:00Z"}
```

Each agent receives the prompt, calls DeepSeek with its own system prompt, publishes the result to a response exchange. Poll for results:

```bash
curl http://localhost:8004/api/responses/uuid-here
```

Returns all agent responses:

```json
{
  "promptId": "uuid-here",
  "responses": [
    {"agentType": "java", "result": "...", "status": "completed"},
    {"agentType": "golang", "result": "...", "status": "completed"},
    {"agentType": "rust", "result": "...", "status": "completed"}
  ]
}
```

### Architecture

POST /api/prompts -> prompt-service -> prompt.exchange -> java agent, go agent, rust agent
                                                      -> each calls DeepSeek API
                                                      -> publishes to response.exchange
                                                      -> response-service stores in Redis
GET /api/responses/{id} -> response-service -> Redis -> all agent answers

### Agent configs

Agent personalities are defined in configs/agent-*.yml. Each sets system prompt, model, temperature, max tokens. Mounted per container at /etc/agent/agent.yml. Add more agents by dropping a new config file and adding a service in docker-compose.yml.

### Kubernetes

```bash
kubectl apply -k k8s/
```

Requires a Namespace, RabbitMQ, Redis, all services, Ingress, ConfigMaps per agent type, and a Secret with the DeepSeek API key.
