package iti.jets.java.response.api;

import iti.jets.java.response.api.model.QueryResponseResult;
import iti.jets.java.response.service.ResponseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/responses")
public class ResponseController {

    @Autowired
    private ResponseService responseService;

    @GetMapping(value = "{promptId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<QueryResponseResult> getResponse(@PathVariable String promptId) {
        QueryResponseResult result = responseService.getResponse(promptId);
        if (result == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(result);
    }
}
