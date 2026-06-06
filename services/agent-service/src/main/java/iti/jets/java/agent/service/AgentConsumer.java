package iti.jets.java.agent.service;

import iti.jets.java.agent.config.AgentConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.messaging.support.MessageBuilder;

import java.time.Instant;
import java.util.Map;
import java.util.function.Consumer;

@Configuration
public class AgentConsumer {

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

    private void process(Map<String, Object> prompt) {
        String userPrompt = (String) prompt.get("prompt");
        String promptId = (String) prompt.get("id");

        String result = deepSeekService.chat(
                agentConfig.getSystemPrompt(),
                userPrompt,
                agentConfig.getTemperature(),
                agentConfig.getMaxTokens()
        );

        Map<String, Object> response = Map.of(
                "promptId", promptId,
                "agentType", agentConfig.getType(),
                "result", result,
                "status", "completed",
                "timestamp", Instant.now().toString()
        );

        streamBridge.send("agentOutput", response);
    }
}
