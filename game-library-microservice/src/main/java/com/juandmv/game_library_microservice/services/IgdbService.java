package com.juandmv.game_library_microservice.services;

import com.juandmv.game_library_microservice.models.dto.GameDTO;
import com.juandmv.game_library_microservice.utils.TokenManager;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class IgdbService {
    private final WebClient webClient;
    private final TokenManager tokenManager;

    public IgdbService(@Qualifier("defaultWebClientBuilder") WebClient.Builder webClientBuilder, TokenManager tokenManager) {
        this.webClient = webClientBuilder.baseUrl("https://api.igdb.com/v4").build();
        this.tokenManager = tokenManager;
    }

    public List<GameDTO> getGames() {
        try {
            String accessToken = tokenManager.getAccessToken();
            Mono<List<GameDTO>> gamesMono = webClient.post()
                    .uri("/games")
                    .header("Client-ID", "f2l6ci0hew27or9nkm3wn1ind13is1")
                    .header("Authorization", "Bearer " + accessToken)
                    .header("Accept", "application/json")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue("fields alternative_names,artworks,bundles,category,checksum,collection,collections,cover.url,created_at,dlcs,expanded_games,expansions,external_games,first_release_date,follows,forks,franchise,franchises,game_engines,game_localizations,game_modes,genres,hypes,involved_companies.company.name,keywords,language_supports,multiplayer_modes,name,parent_game,platforms.name,player_perspectives,ports,rating,rating_count,release_dates,remakes,remasters,screenshots,similar_games,slug,standalone_expansions,status,storyline,summary,tags,themes,total_rating,total_rating_count,updated_at,url,version_parent,version_title,videos,websites;")
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<>() {});

            return gamesMono.block();
        } catch (WebClientResponseException e) {
            System.err.println("Error en la API: " + e.getStatusCode());
            System.err.println("Cuerpo del error: " + e.getResponseBodyAsString());
            return null;
        }
    }

    public List<GameDTO> getGamesByManyId(List<Long> igdbIds) {
        try {
            String ids = igdbIds.stream()
                    .map(String::valueOf) // Convertir cada ID a String
                    .collect(Collectors.joining(","));

            String query = "fields age_ratings,aggregated_rating,aggregated_rating_count,alternative_names,artworks," +
                    "bundles,category,checksum,collection,collections,cover.url,created_at,dlcs,expanded_games," +
                    "expansions,external_games,first_release_date,follows,forks,franchise,franchises,game_engines," +
                    "game_localizations,game_modes,genres,hypes,involved_companies.company.name,keywords," +
                    "language_supports,multiplayer_modes,name,parent_game,platforms.name,player_perspectives,ports," +
                    "rating,rating_count,release_dates,remakes,remasters,screenshots,similar_games,slug," +
                    "standalone_expansions,status,storyline,summary,tags,themes,total_rating,total_rating_count," +
                    "updated_at,url,version_parent,version_title,videos,websites;where id = (" + ids + ");";

            String accessToken = tokenManager.getAccessToken();

            Mono<List<GameDTO>> gameMono = webClient.post()
                    .uri("/games/")
                    .header("Client-ID", "f2l6ci0hew27or9nkm3wn1ind13is1")
                    .header("Authorization", "Bearer " + accessToken)
                    .header("Accept", "application/json")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(query)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<GameDTO>>() {});

            return gameMono.block();
        } catch (WebClientResponseException e) {
            System.err.println("Error en la API: " + e.getStatusCode());
            System.err.println("Cuerpo del error: " + e.getResponseBodyAsString());
            return null;
        }
    }

}
