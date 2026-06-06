package iti.jets.java.prompt.service;

import iti.jets.java.prompt.api.model.CreatePromptPayload;
import iti.jets.java.prompt.api.model.QueryPromptResult;
import iti.jets.java.prompt.domain.PromptMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class PromptService {

    @Autowired
    private StreamBridge streamBridge;

    public QueryPromptResult processPrompt(CreatePromptPayload payload) {
        String id = UUID.randomUUID().toString();
        Instant now = Instant.now();
        PromptMessage message = new PromptMessage()
                .setId(id)
                .setSessionId(payload.getSessionId())
                .setPrompt(payload.getPrompt())
                .setTimestamp(now);
        streamBridge.send("promptOutput", message);
        return new QueryPromptResult().setId(id).setTimestamp(now);
    }
}
