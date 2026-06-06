package iti.jets.java.agent.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Service
public class DeepSeekService {

    private final RestClient restClient;

    public DeepSeekService(RestClient.Builder builder) {
        String apiKey = System.getenv("DEEPSEEK_API_KEY");
        this.restClient = builder
                .baseUrl("https://api.deepseek.com/v1")
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    public String chat(String systemPrompt, String userPrompt, double temperature, int maxTokens) {
        Map<String, Object> request = Map.of(
                "model", "deepseek-chat",
                "messages", List.of(
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user", "content", userPrompt)
                ),
                "temperature", temperature,
                "max_tokens", maxTokens
        );

        Map response = restClient.post()
                .uri("/chat/completions")
                .body(request)
                .retrieve()
                .body(Map.class);

        List<Map> choices = (List<Map>) response.get("choices");
        return (String) ((Map) choices.get(0).get("message")).get("content");
    }
}
