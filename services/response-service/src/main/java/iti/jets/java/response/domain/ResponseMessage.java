package iti.jets.java.response.domain;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.Instant;

@Data
@Accessors(chain = true)
public class ResponseMessage {
    private String promptId;
    private String agentType;
    private String result;
    private String status;
    private Instant timestamp;
}
