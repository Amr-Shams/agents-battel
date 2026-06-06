package iti.jets.java.agent.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

@Data
@Validated
@ConfigurationProperties(prefix = "agent")
public class AgentConfig {
    @NotBlank
    private String type;
    @NotBlank
    private String systemPrompt;
    @NotBlank
    private String model;
    @Positive
    private double temperature;
    @Positive
    private int maxTokens;
}
