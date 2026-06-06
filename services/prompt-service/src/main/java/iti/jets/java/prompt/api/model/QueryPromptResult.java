package iti.jets.java.prompt.api.model;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.Instant;

@Data
@Accessors(chain = true)
public class QueryPromptResult {
    private String id;
    private Instant timestamp;
}
