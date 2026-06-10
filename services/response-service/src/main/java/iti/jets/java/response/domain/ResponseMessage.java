package iti.jets.java.response.domain;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.Instant;
import java.util.Map;

@Data
@Accessors(chain = true)
public class ResponseMessage {
    private String promptId;
    private String agentType;
    private String result;
    private String status;
    private Instant timestamp;
    private int loopCount;
    private Map<String, String> previousResponses;
    private String prompt;
}
