package iti.jets.java.response.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import iti.jets.java.response.api.model.AgentResponse;
import iti.jets.java.response.api.model.QueryResponseResult;
import iti.jets.java.response.domain.ResponseMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ResponseService {

    private static final String KEY_PREFIX = "response:";

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    public void storeResponse(ResponseMessage message) {
        try {
            String key = KEY_PREFIX + message.getPromptId();
            AgentResponse agentResponse = new AgentResponse()
                    .setAgentType(message.getAgentType())
                    .setResult(message.getResult())
                    .setStatus(message.getStatus())
                    .setTimestamp(message.getTimestamp());
            String json = objectMapper.writeValueAsString(agentResponse);
            redisTemplate.opsForHash().put(key, message.getAgentType(), json);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize agent response", e);
        }
    }

    public QueryResponseResult getResponse(String promptId) {
        String key = KEY_PREFIX + promptId;
        List<String> values = redisTemplate.opsForHash().values(key)
                .stream()
                .map(v -> (String) v)
                .collect(Collectors.toList());

        if (values.isEmpty()) {
            return null;
        }

        List<AgentResponse> responses = values.stream().map(json -> {
            try {
                return objectMapper.readValue(json, AgentResponse.class);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Failed to deserialize agent response", e);
            }
        }).collect(Collectors.toList());

        return new QueryResponseResult()
                .setPromptId(promptId)
                .setResponses(responses);
    }
}
