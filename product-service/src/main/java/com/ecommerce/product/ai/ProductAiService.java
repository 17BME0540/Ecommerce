package com.ecommerce.product.ai;

import com.ecommerce.product.dto.AiDescriptionRequest;
import com.ecommerce.product.model.Product;
import com.ecommerce.product.repository.ProductRepository;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Wraps Spring AI's ChatClient to add two shopping-assistant features:
 *  1. Auto-generating marketing-quality product descriptions.
 *  2. A recommendation chatbot grounded in the current product catalog
 *     (a lightweight retrieval step instead of a full vector-store RAG pipeline —
 *     swap in VectorStore + similaritySearch here if the catalog grows large).
 */
@Service
public class ProductAiService {

    private final ChatClient chatClient;
    private final ProductRepository productRepository;

    public ProductAiService(ChatClient.Builder chatClientBuilder, ProductRepository productRepository) {
        this.chatClient = chatClientBuilder.build();
        this.productRepository = productRepository;
    }

    public String generateDescription(AiDescriptionRequest request) {
        String tone = (request.tone() == null || request.tone().isBlank()) ? "friendly and persuasive" : request.tone();
        String systemPrompt = """
                You are an e-commerce copywriter. Write a concise, %s product description
                (max 80 words) for the given product and its key features. Do not use markdown.
                """.formatted(tone);

        Message system = new SystemMessage(systemPrompt);
        Message user = new UserMessage("Product: %s\nKey features: %s"
                .formatted(request.productName(), request.keyFeatures()));

        return chatClient.prompt(new Prompt(List.of(system, user)))
                .call()
                .content();
    }

    public String chatWithAssistant(String userMessage) {
        List<Product> catalog = productRepository.findAll();
        String catalogSummary = catalog.stream()
                .map(p -> "- %s (%s): %s | $%s | stock: %d".formatted(
                        p.getName(), p.getCategory(), p.getDescription(), p.getPrice(), p.getStockQuantity()))
                .collect(Collectors.joining("\n"));

        String systemPrompt = """
                You are a helpful shopping assistant for an online store. Recommend products
                ONLY from the catalog below. If nothing fits, say so honestly. Keep answers short.

                Catalog:
                %s
                """.formatted(catalogSummary.isBlank() ? "(catalog is currently empty)" : catalogSummary);

        Message system = new SystemMessage(systemPrompt);
        Message user = new UserMessage(userMessage);

        return chatClient.prompt(new Prompt(List.of(system, user)))
                .call()
                .content();
    }
}
