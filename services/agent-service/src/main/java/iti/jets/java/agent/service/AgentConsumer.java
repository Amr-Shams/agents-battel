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
  private final AgentConfig agentConfig;
  private final ChatService chatService;
  private final StreamBridge streamBridge;

  public AgentConsumer(AgentConfig agentConfig, ChatService chatService, StreamBridge streamBridge) {
    this.agentConfig = agentConfig;
    this.chatService = chatService;
    this.streamBridge = streamBridge;
  }

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

    log.info("Agent {} received prompt: loop {} - {}", agentConfig.getType(), loopCount, userPrompt);
    String finalPrompt = buildFinalPrompt(userPrompt, previousResponses);

    String result;
    String status;
    try {
      result = chatService.call(agentConfig.getSystemPrompt(), finalPrompt);
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
        "prompt", userPrompt);

    streamBridge.send("agentOutput", response);
  }

  private String buildFinalPrompt(String userPrompt, Map<String, String> previousResponses) {
    if (previousResponses == null || previousResponses.isEmpty()) {
      return userPrompt;
    }
    StringBuilder sb = new StringBuilder();
    sb.append("Previous responses from other agents:\n\n");
    previousResponses.forEach((k, v) -> sb.append("[").append(k).append("]\n").append(v).append("\n\n"));
    sb.append("Based on these responses, refine your answer to the original prompt. ")
        .append("Offer a different perspective with stronger claims.\n\n")
        .append("Original prompt: ").append(userPrompt);
    return sb.toString();
  }
}
