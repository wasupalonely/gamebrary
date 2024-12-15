package com.juandmv.game_library_microservice.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    // Llamado a apis externas
    @Bean("defaultWebClientBuilder")
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }

    // Llamado a otros microservicios
    @Bean("loadBalancedWebClientBuilder")
    @LoadBalanced
    public WebClient.Builder loadBalancedWebClientBuilder() {
        return WebClient.builder();
    }
}
