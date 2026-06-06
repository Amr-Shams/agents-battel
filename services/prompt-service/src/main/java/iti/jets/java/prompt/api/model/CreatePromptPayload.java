package iti.jets.java.prompt.api.model;

import lombok.Data;
import lombok.experimental.Accessors;
import jakarta.validation.constraints.NotBlank;

import jakarta.validation.constraints.NotNull;

@Data
@Accessors(chain = true)
public class CreatePromptPayload {

    @NotNull
    @NotBlank
    private String sessionId;

    @NotNull
    @NotBlank
    private String prompt;
}
