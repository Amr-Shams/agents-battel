package iti.jets.java.response.api.model;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class QueryResponseResult {
    private String promptId;
    private List<AgentResponse> responses;
    private boolean done;
}
