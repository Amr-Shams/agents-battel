package iti.jets.java.prompt.api;

import iti.jets.java.prompt.api.model.CreatePromptPayload;
import iti.jets.java.prompt.api.model.QueryPromptResult;
import iti.jets.java.prompt.service.PromptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
@RequestMapping("api/prompts")
public class PromptController {

    @Autowired
    private PromptService promptService;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<QueryPromptResult> createPrompt(@Valid @RequestBody CreatePromptPayload payload) {
        QueryPromptResult result = promptService.processPrompt(payload);
        return ResponseEntity.accepted().body(result);
    }
}
