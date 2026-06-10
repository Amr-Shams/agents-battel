package iti.jets.java.agent.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class DeepSeekService {

    private static final Logger log = LoggerFactory.getLogger(DeepSeekService.class);

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
        return chat(systemPrompt, userPrompt, temperature, maxTokens, null);
    }

    public String chat(String systemPrompt, String userPrompt, double temperature, int maxTokens,
                       Map<String, String> previousResponses) {
        var messages = new ArrayList<Map<String, String>>();
        messages.add(Map.of("role", "system", "content", systemPrompt));

        String finalPrompt;
        if (previousResponses != null && !previousResponses.isEmpty()) {
            log.info("Building refinement prompt with {} previous responses", previousResponses.size());
            var sb = new StringBuilder();
            sb.append("Previous responses from other agents:\n\n");
            for (var entry : previousResponses.entrySet()) {
                sb.append("[").append(entry.getKey()).append("]\n")
                  .append(entry.getValue()).append("\n\n");
            }
            sb.append("Based on these responses, refine your answer to the original prompt. ")
              .append("You may agree, disagree, or offer a different perspective. ")
              .append("Your goal is to produce the strongest, most well-reasoned response.\n\n")
              .append("Original prompt: ").append(userPrompt);
            finalPrompt = sb.toString();
            log.info("Refinement prompt built, total length: {} chars", finalPrompt.length());
        } else {
            finalPrompt = userPrompt;
        }

        messages.add(Map.of("role", "user", "content", finalPrompt));

        Map<String, Object> request = Map.of(
                "model", "deepseek-chat",
                "messages", messages,
                "temperature", temperature,
                "max_tokens", maxTokens
        );

        Map response = restClient.post()
                .uri("/chat/completions")
                .body(request)
                .retrieve()
                .body(Map.class);

        List<Map> choices = (List<Map>) response.get("choices");
        String content = (String) ((Map) choices.get(0).get("message")).get("content");
        log.info("DeepSeek response received: {} chars", content.length());
        return content;
    }
}
