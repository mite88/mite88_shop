package io.mite88.mite88shop.mite88shop.global.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;

/**
 * packageName    : io.mite88.mite88shop.mite88shop.global.config
 * fileName       : AiModelClient
 * author         : Admin
 * date           : 26. 6. 12.
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 26. 6. 12.        Admin       최초 생성
 */
@Component
@RequiredArgsConstructor
public class AiModelClient {

    private final WebClient.Builder webClientBuilder;


    @Value("${AI_MODEL_URL}")

    private String aiModelUrl;

    @Value("${AI_MODEL_ENDPOINT:/predict}")
    private String aiModelEndpoint;

    @Value("${AI_MODEL_TIMEOUT:5s}")
    private Duration timeout;

    public Object callModel(String input) {
        return webClientBuilder
                .baseUrl(aiModelUrl)
                .build()
                .post()
                .uri(aiModelEndpoint)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(input)            // 요청 바디 누락되어 있었음
                .retrieve()
                .bodyToMono(Object.class)
                .timeout(timeout)
                .block();
    }
}
