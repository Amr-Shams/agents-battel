package iti.jets.java.prompt.domain;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.Instant;
import java.util.Map;

@Data
@Accessors(chain = true)
public class PromptMessage {
    private String id;
    private String sessionId;
    private String prompt;
    private Instant timestamp;
    private int loopCount = 0;
    private Map<String, String> previousResponses;
}
