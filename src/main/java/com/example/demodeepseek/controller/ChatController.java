package com.example.demodeepseek.controller;

import com.example.demodeepseek.service.DeepseekService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
public class ChatController {

    private final DeepseekService deepseekService;

    public ChatController(DeepseekService deepseekService) {
        this.deepseekService = deepseekService;
    }

    @PostMapping("/chat")
    public ResponseEntity<Map<String, String>> handleChatRequest(@RequestParam("prompt") String prompt) throws IOException {
        String deepseekResponse = deepseekService.getDeepseekResponse(prompt);
        Map<String, String> response = new HashMap<>();
        response.put("response", deepseekResponse);
        return ResponseEntity.ok(response);
    }
}