package iti.jets.java.response.api.model;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.Instant;

@Data
@Accessors(chain = true)
public class AgentResponse {
    private String agentType;
    private String result;
    private String status;
    private Instant timestamp;
    private int loopCount;
}
