package com.ecommerce.product.controller;

import com.ecommerce.product.ai.ProductAiService;
import com.ecommerce.product.dto.AiDescriptionRequest;
import com.ecommerce.product.dto.ChatRequest;
import com.ecommerce.product.dto.ChatResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
public class ProductAiController {

    private final ProductAiService productAiService;

    public ProductAiController(ProductAiService productAiService) {
        this.productAiService = productAiService;
    }

    @PostMapping("/generate-description")
    public ResponseEntity<String> generateDescription(@RequestBody AiDescriptionRequest request) {
        return ResponseEntity.ok(productAiService.generateDescription(request));
    }

    @PostMapping("/assistant")
    public ResponseEntity<ChatResponse> assistant(@RequestBody ChatRequest request) {
        return ResponseEntity.ok(new ChatResponse(productAiService.chatWithAssistant(request.message())));
    }
}
