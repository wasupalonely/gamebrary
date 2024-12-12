package com.juandmv.game_library_microservice.utils;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Instant;

@Component
public class TokenManager {
    private String accessToken;
    private Instant tokenExpiration;

    private final WebClient webClient;

    public TokenManager(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("https://id.twitch.tv/oauth2/token").build();
    }

    public String getAccessToken() {
        System.out.println("Verificando token...");
        System.out.println("Token actual: " + accessToken);
        System.out.println("Expiración: " + tokenExpiration);

        if (accessToken == null || tokenExpiration == null || tokenExpiration.isBefore(Instant.now())) {
            System.out.println("Necesario refrescar token");
            refreshAccessToken();
        }

        return accessToken;
    }

    private void refreshAccessToken() {
        String clientId = "f2l6ci0hew27or9nkm3wn1ind13is1";
        String clientSecret = "cuscs9bvmy8dkdnsxj81d9amhqws09";

        try {
            TokenResponse response = webClient.post()
                    .uri(uriBuilder -> uriBuilder
                            .queryParam("client_id", clientId)
                            .queryParam("client_secret", clientSecret)
                            .queryParam("grant_type", "client_credentials")
                            .build())
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .retrieve()
                    .bodyToMono(TokenResponse.class)
                    .block();

            if (response != null) {
                System.out.println("Token Response Details:");
                System.out.println("Access Token: " + response.getAccessToken());
                System.out.println("Expires In: " + response.getExpiresIn());
                System.out.println("Token Type: " + response.getTokenType());

                this.accessToken = response.getAccessToken();
                this.tokenExpiration = Instant.now().plusSeconds(response.getExpiresIn());
            } else {
                System.out.println("No se recibió respuesta del token");
                debugTokenRequest();
            }
        } catch (Exception e) {
            System.err.println("Error al obtener el token:");
            e.printStackTrace();
            debugTokenRequest();
        }
    }

    private void debugTokenRequest() {
        String clientId = "f2l6ci0hew27or9nkm3wn1ind13is1";
        String clientSecret = "cuscs9bvmy8dkdnsxj81d9amhqws09";

        String responseString = webClient.post()
                .uri(uriBuilder -> uriBuilder
                        .queryParam("client_id", clientId)
                        .queryParam("client_secret", clientSecret)
                        .queryParam("grant_type", "client_credentials")
                        .build())
                .header("Content-Type", "application/x-www-form-urlencoded")
                .retrieve()
                .bodyToMono(String.class)
                .block();

        System.out.println("Respuesta cruda del token: " + responseString);
    }

    // Clase pública para mapear la respuesta
    public static class TokenResponse {
        @JsonProperty("access_token")
        private String accessToken;

        @JsonProperty("expires_in")
        private int expiresIn;

        @JsonProperty("token_type")
        private String tokenType;

        // Getters y setters
        public String getAccessToken() {
            return accessToken;
        }

        public void setAccessToken(String accessToken) {
            this.accessToken = accessToken;
        }

        public int getExpiresIn() {
            return expiresIn;
        }

        public void setExpiresIn(int expiresIn) {
            this.expiresIn = expiresIn;
        }

        public String getTokenType() {
            return tokenType;
        }

        public void setTokenType(String tokenType) {
            this.tokenType = tokenType;
        }
    }
}