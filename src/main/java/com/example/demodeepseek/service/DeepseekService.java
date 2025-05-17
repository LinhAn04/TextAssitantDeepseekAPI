package com.example.demodeepseek.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Service
public class DeepseekService {

    private static final Logger logger = LoggerFactory.getLogger(DeepseekService.class);
    private static final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final String DEEPSEEK_URL = "https://api.deepseek.com/v1/chat/completions";

    @Value("${deepseek.api.key}")
    private String DEEPSEEK_API_KEY;

    public String getDeepseekResponse(String prompt) throws IOException {
        if (DEEPSEEK_API_KEY == null || DEEPSEEK_API_KEY.trim().isEmpty()) {
            logger.error("DeepSeek API key chưa được cung cấp");
            return "DeepSeek API key chưa được cung cấp";
        }

        String requestBody = String.format(
                "{\"model\": \"deepseek-chat\", \"messages\": [{\"role\": \"user\", \"content\": \"%s\"}], \"max_tokens\": 150}",
                prompt
        );

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(DEEPSEEK_URL))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + DEEPSEEK_API_KEY)
                .timeout(Duration.ofSeconds(30))
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        try {
            logger.info("Gửi yêu cầu đến DeepSeek API với prompt: {}", prompt);
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            logger.debug("Phản hồi từ DeepSeek API: {}", response.body());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                String responseBody = response.body();
                JsonNode jsonResponse = mapper.readTree(responseBody);

                if (jsonResponse.has("choices") && jsonResponse.get("choices").isArray() && !jsonResponse.get("choices").isEmpty()) {
                    JsonNode choice = jsonResponse.get("choices").get(0);
                    if (choice.has("message") && choice.get("message").has("content")) {
                        return choice.get("message").get("content").asText();
                    } else {
                        logger.warn("Không tìm thấy message hoặc content trong phản hồi: {}", responseBody);
                        return "Phản hồi không chứa nội dung hợp lệ";
                    }
                } else {
                    logger.warn("Không tìm thấy choices trong phản hồi: {}", responseBody);
                    return "Phản hồi không chứa choices";
                }
            } else {
                logger.error("Lỗi API DeepSeek: {} - {}", response.statusCode(), response.body());
                return "Lỗi API DeepSeek: " + response.statusCode() + ", " + response.body();
            }
        } catch (IOException e) {
            logger.error("Lỗi I/O khi gọi DeepSeek API: {}", e.getMessage(), e);
            return "Lỗi I/O khi gọi DeepSeek API: " + e.getMessage();
        } catch (InterruptedException e) {
            logger.error("Yêu cầu bị gián đoạn: {}", e.getMessage(), e);
            Thread.currentThread().interrupt(); // Khôi phục trạng thái gián đoạn
            return "Yêu cầu bị gián đoạn: " + e.getMessage();
        }
    }
}