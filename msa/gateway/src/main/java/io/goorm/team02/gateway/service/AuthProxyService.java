package io.goorm.team02.gateway.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthProxyService {

    private final WebClient webClient;

    //@Value("${USER_SERVICE_URL:http://user-service:8082}")
    @Value("${USER_SERVICE_URL:http://localhost:8082}")
    private String userServiceUrl;

    public Mono<Map> requestLogin(Map<String, Object> loginRequest) {
        return webClient.post()
                .uri(userServiceUrl + "/api/internal/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(loginRequest)
                .retrieve()
                .bodyToMono(Map.class);
    }

}
