package com.mjc.school.aicontroller;

import com.mjc.school.service.aiservice.AiAssistantService;
import com.mjc.school.service.aiservice.dto.ChatRequest;
import com.mjc.school.service.aiservice.dto.ChatResponse;
import com.mjc.school.service.aiservice.dto.ChatTurn;
import com.mjc.school.versioning.ApiVersion;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import static com.mjc.school.controller.RestApiConst.AI_API_ROOT_PATH;

@ApiVersion(1)
@RestController
@RequestMapping(value = AI_API_ROOT_PATH, produces = MediaType.APPLICATION_JSON_VALUE)
public class AiAssistantController {

    private final AiAssistantService aiService;

    @Autowired
    public AiAssistantController(AiAssistantService aiService){
        this.aiService=aiService;
    }

    @Operation(summary = "Chat with the AI assistant", description = "Sends a user message to the AI assistant and returns a response, including relevant news article IDs if found")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully processed the chat message"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "401", description = "You are not authorized to view the resource"),
            @ApiResponse(responseCode = "429", description = "Rate limit exceeded (5 requests per minute)")
    })
    @PostMapping(value="/chat")
    public ResponseEntity<ChatResponse> chat(@RequestBody ChatRequest request){
        ChatResponse response = aiService.chat(request.sessionId(),request.message());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get conversation history", description = "Returns the full conversation history for a given session ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved the conversation history"),
            @ApiResponse(responseCode = "401", description = "You are not authorized to view the resource"),
            @ApiResponse(responseCode = "404", description = "Session not found")
    })
    @GetMapping("/history/{sessionId}")
    public ResponseEntity<List<ChatTurn>> history(@PathVariable String sessionId){
        return ResponseEntity.ok(aiService.getHistory(sessionId));
    }

    @Operation(summary = "Clear conversation history", description = "Deletes all messages for a given session ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Successfully cleared the conversation history"),
            @ApiResponse(responseCode = "401", description = "You are not authorized to view the resource"),
            @ApiResponse(responseCode = "404", description = "Session not found")
    })
    @DeleteMapping("/history/{sessionId}")
    public ResponseEntity<Void> clearHistory(@PathVariable String sessionId){
        aiService.clearHistory(sessionId);
        return ResponseEntity.noContent().build();
    }
}
