package iti.jets.java.response.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import iti.jets.java.response.api.model.AgentResponse;
import iti.jets.java.response.api.model.QueryResponseResult;
import iti.jets.java.response.domain.ResponseMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ResponseService {

    private static final Logger log = LoggerFactory.getLogger(ResponseService.class);
    private static final String KEY_PREFIX = "response:";

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private StreamBridge streamBridge;

    @Value("${app.agent-types:java,golang,rust}")
    private String agentTypesConfig;

    @Value("${app.max-loops:2}")
    private int maxLoops;

    public void storeResponse(ResponseMessage message) {
        try {
            String key = KEY_PREFIX + message.getPromptId();
            log.info("Storing response: promptId={}, agent={}, loop={}, status={}",
                    message.getPromptId(), message.getAgentType(),
                    message.getLoopCount(), message.getStatus());

            AgentResponse agentResponse = new AgentResponse()
                    .setAgentType(message.getAgentType())
                    .setResult(message.getResult())
                    .setStatus(message.getStatus())
                    .setTimestamp(message.getTimestamp())
                    .setLoopCount(message.getLoopCount());

            String json = objectMapper.writeValueAsString(agentResponse);
            String field = message.getAgentType() + ":" + message.getLoopCount();
            redisTemplate.opsForHash().put(key, field, json);

            if (message.getPrompt() != null) {
                redisTemplate.opsForHash().putIfAbsent(key, "_prompt", message.getPrompt());
            }
            redisTemplate.opsForHash().putIfAbsent(key, "_maxLoops", String.valueOf(maxLoops));

            if (allAgentsResponded(key, message.getLoopCount())) {
                log.info("All agents responded for loop {}, promptId={}",
                        message.getLoopCount(), message.getPromptId());

                if (message.getLoopCount() < maxLoops - 1) {
                    int nextLoop = message.getLoopCount() + 1;
                    redisTemplate.opsForHash().put(key, "_currentLoop", String.valueOf(nextLoop));

                    Map<String, String> previous = getPreviousResponses(key, message.getLoopCount());
                    String prompt = (String) redisTemplate.opsForHash().get(key, "_prompt");

                    log.info("Publishing loop-back: promptId={}, loop {} -> {}, previous agents: {}",
                            message.getPromptId(), message.getLoopCount(), nextLoop, previous.keySet());

                    Map<String, Object> loopBack = Map.of(
                            "id", message.getPromptId(),
                            "prompt", prompt != null ? prompt : "",
                            "sessionId", "",
                            "timestamp", Instant.now().toString(),
                            "loopCount", nextLoop,
                            "previousResponses", previous
                    );

                    boolean sent = streamBridge.send("promptOutput", loopBack);
                    log.info("Loop-back published: {}", sent ? "success" : "FAILED");
                } else {
                    log.info("Max loops ({}) reached for promptId={}", maxLoops, message.getPromptId());
                }
            } else {
                log.info("Not all agents responded yet for loop {}, promptId={}",
                        message.getLoopCount(), message.getPromptId());
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize agent response", e);
        }
    }

    private boolean allAgentsResponded(String key, int loopCount) {
        String[] agents = agentTypesConfig.split(",");
        for (String agent : agents) {
            String field = agent.trim() + ":" + loopCount;
            if (redisTemplate.opsForHash().get(key, field) == null) return false;
        }
        return true;
    }

    private Map<String, String> getPreviousResponses(String key, int loopCount) {
        String[] agents = agentTypesConfig.split(",");
        return java.util.Arrays.stream(agents)
                .map(String::trim)
                .collect(Collectors.toMap(
                        a -> a,
                        a -> {
                            String field = a + ":" + loopCount;
                            String json = (String) redisTemplate.opsForHash().get(key, field);
                            if (json == null) return "";
                            try {
                                return objectMapper.readValue(json, AgentResponse.class).getResult();
                            } catch (Exception e) {
                                return "";
                            }
                        }
                ));
    }

    public QueryResponseResult getResponse(String promptId) {
        String key = KEY_PREFIX + promptId;
        Map<Object, Object> all = redisTemplate.opsForHash().entries(key);

        if (all.isEmpty()) {
            return null;
        }

        String loopStr = (String) all.get("_currentLoop");
        int currentLoop = loopStr != null ? Integer.parseInt(loopStr) : 0;
        String maxStr = (String) all.get("_maxLoops");
        int max = maxStr != null ? Integer.parseInt(maxStr) : maxLoops;

        List<AgentResponse> responses = all.entrySet().stream()
                .filter(e -> !e.getKey().toString().startsWith("_"))
                .map(e -> {
                    try {
                        return objectMapper.readValue((String) e.getValue(), AgentResponse.class);
                    } catch (JsonProcessingException ex) {
                        throw new RuntimeException(ex);
                    }
                })
                .collect(Collectors.toList());

        boolean done = currentLoop >= max - 1 && allAgentsResponded(key, currentLoop);

        log.debug("getResponse: promptId={}, currentLoop={}, maxLoops={}, responses={}, done={}",
                promptId, currentLoop, max, responses.size(), done);

        return new QueryResponseResult()
                .setPromptId(promptId)
                .setResponses(responses)
                .setDone(done);
    }
}
