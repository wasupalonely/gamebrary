package com.juandmv.rating_microservice.services;

import com.juandmv.rating_microservice.enums.GameStatus;
import com.juandmv.rating_microservice.models.dto.GameDTO;
import com.juandmv.rating_microservice.models.dto.LibraryDTO;
import com.juandmv.rating_microservice.models.dto.RatingDTO;
import com.juandmv.rating_microservice.models.entities.Rating;
import com.juandmv.rating_microservice.repository.RatingRepository;
import com.juandmv.rating_microservice.utils.ErrorResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class RatingService {

    @Autowired
    private final RatingRepository ratingRepository;

    private final WebClient.Builder webClientBuilder;

    public ResponseEntity<?> saveRating(RatingDTO ratingDTO) {


        // VALIDAR LA EXISTENCIA DEL USUARIO
        String userId = ratingDTO.getUserId();

        boolean userResult = Boolean.TRUE.equals(this.webClientBuilder.build()
                .get()
                .uri("lb://user-microservice/api/users/exist/" + userId)
                .retrieve()
                .bodyToMono(Boolean.class)
                .block());

        if (!userResult) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(
                            "El usuario no existe",
                            "El usuario con ID: " + userId + " no existe"
                    ));
        }

        // VALIDAR LA EXISTENCIA DEL JUEGO
        Long gameId = ratingDTO.getGameId();

        GameDTO gameResult = this.webClientBuilder.build()
                .get()
                .uri("lb://library-microservice/api/games/" + gameId)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response -> {
                    if (response.statusCode().equals(HttpStatus.NOT_FOUND)) {
                        return Mono.error(new RuntimeException("El juego no existe"));
                    }
                    return response.createException();
                })
                .onStatus(HttpStatusCode::is5xxServerError, ClientResponse::createException)
                .bodyToMono(GameDTO.class)
                .onErrorResume(e -> Mono.empty()) // Esto evita que el flujo se rompa
                .block();

        if (gameResult == null) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(
                            "El juego no existe",
                            "El juego con ID: " + gameId + " no existe"
                    ));
        }


        LibraryDTO libraryResult = this.webClientBuilder.build()
                .get()
                .uri("lb://library-microservice/api/games/user/" + userId + "/game/" + gameId)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response -> {
                    if (response.statusCode().equals(HttpStatus.NOT_FOUND)) {
                        return Mono.error(new RuntimeException("El juego no existe"));
                    }
                    return response.createException();
                })
                .onStatus(HttpStatusCode::is5xxServerError, ClientResponse::createException)
                .bodyToMono(LibraryDTO.class)
                .onErrorResume(e -> Mono.empty())
                .block();

        if (libraryResult == null) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(
                            "El juego no existe",
                            "El juego con ID: " + gameId + " no existe"
                    ));
        }

        if (!libraryResult.getStatus().equals(GameStatus.PLAYED)) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(new ErrorResponse(
                            "El usuario no ha terminado el juego",
                            "El juego con ID: " + gameId + " no está en estado PLAYED"
                    ));
        }

        // VALIDAR SI EL USUARIO YA TIENE UNA CALIFICACIÓN PARA EL JUEGO
        Rating ratingResult = ratingRepository.findByUserIdAndGameId(userId, gameId).orElse(null);

        if (ratingResult != null) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(new ErrorResponse(
                            "La calificación ya existe",
                            "El usuario con ID: " + userId + " ya tiene una calificación para el juego con ID: " + gameId
                    ));
        }

        Rating rating = Rating.builder()
                .userId(userId)
                .gameId(gameId)
                .rating(ratingDTO.getRating())
                .comment(ratingDTO.getComment())
                .build();

        return ResponseEntity.ok(ratingRepository.save(rating));
    }
}
