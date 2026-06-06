package iti.jets.java.prompt.domain;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.Instant;

@Data
@Accessors(chain = true)
public class PromptMessage {
    private String id;
    private String sessionId;
    private String prompt;
    private Instant timestamp;
}
