package iti.jets.java.agent.service;

import iti.jets.java.agent.config.AgentConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.cloud.stream.function.StreamBridge;

import java.time.Instant;
import java.util.Map;
import java.util.function.Consumer;

@Configuration
public class AgentConsumer {

    private static final Logger log = LoggerFactory.getLogger(AgentConsumer.class);

    @Autowired
    private AgentConfig agentConfig;

    @Autowired
    private DeepSeekService deepSeekService;

    @Autowired
    private StreamBridge streamBridge;

    @Bean
    public Consumer<Map<String, Object>> promptInput() {
        return this::process;
    }

    @SuppressWarnings("unchecked")
    private void process(Map<String, Object> prompt) {
        String userPrompt = (String) prompt.get("prompt");
        String promptId = (String) prompt.get("id");
        int loopCount = prompt.getOrDefault("loopCount", 0) instanceof Number n ? n.intValue() : 0;
        Map<String, String> previousResponses = (Map<String, String>) prompt.get("previousResponses");

        boolean isRefinement = previousResponses != null && !previousResponses.isEmpty();
        log.info("Agent {} received prompt: loop={}, refine={}, agents={}",
                agentConfig.getType(), loopCount, isRefinement,
                isRefinement ? previousResponses.keySet() : "none");

        String result;
        String status;
        try {
            result = deepSeekService.chat(
                    agentConfig.getSystemPrompt(),
                    userPrompt,
                    agentConfig.getTemperature(),
                    agentConfig.getMaxTokens(),
                    previousResponses
            );
            status = "completed";
            log.info("Agent {} completed loop {} successfully", agentConfig.getType(), loopCount);
        } catch (Exception e) {
            result = e.getMessage();
            status = "error";
            log.error("Agent {} error on loop {}: {}", agentConfig.getType(), loopCount, e.getMessage());
        }

        Map<String, Object> response = Map.of(
                "promptId", promptId,
                "agentType", agentConfig.getType(),
                "result", result,
                "status", status,
                "timestamp", Instant.now().toString(),
                "loopCount", loopCount,
                "prompt", userPrompt
        );

        streamBridge.send("agentOutput", response);
    }
}
