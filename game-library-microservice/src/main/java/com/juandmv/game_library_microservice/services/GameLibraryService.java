package com.juandmv.game_library_microservice.services;

import com.juandmv.game_library_microservice.models.dto.GameDTO;
import com.juandmv.game_library_microservice.models.dto.LibraryDTO;
import com.juandmv.game_library_microservice.enums.GameStatus;
import com.juandmv.game_library_microservice.models.entities.GameLibrary;
import com.juandmv.game_library_microservice.repository.GameLibraryRepository;
import com.juandmv.game_library_microservice.utils.BaseResponse;
import com.juandmv.game_library_microservice.utils.ErrorResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.*;

@Service
public class GameLibraryService {

    @Autowired
    private final GameLibraryRepository gameLibraryRepository;

    @Autowired
    private final IgdbService igdbService;

    private final WebClient.Builder webClientBuilder;

    public GameLibraryService(
            GameLibraryRepository gameLibraryRepository,
            @Qualifier("loadBalancedWebClientBuilder") WebClient.Builder webClientBuilder,
            IgdbService igdbService
    ) {
        this.gameLibraryRepository = gameLibraryRepository;
        this.webClientBuilder = webClientBuilder;
        this.igdbService = igdbService;
    }

    public ResponseEntity<?> getAllGamesByUserId(String userId) {
        boolean result = Boolean.TRUE.equals(this.webClientBuilder.build()
                .get()
                .uri("lb://user-microservice/api/users/exist/" + userId)
                .retrieve()
                .bodyToMono(Boolean.class)
                .block());

        if (!result) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(
                            "El usuario no existe",
                            "El usuario con ID: " + userId + " no existe"
                    ));
        }

        List<GameLibrary> games = gameLibraryRepository.findByUserId(userId).orElse(Collections.emptyList());

        if (games.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(
                            "Juegos no encontrados",
                            "No se encontraron juegos para el usuario: " + userId
                    ));
        } else {
            List<Long> igdbIds = games
                    .stream()
                    .map(GameLibrary::getGameId)
                    .toList();

            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(igdbService.getGamesByManyId(igdbIds));
        }
    }

    public ResponseEntity<?> saveGameLibrary(LibraryDTO libraryDTO) {
        String userId = libraryDTO.getUserId();
        boolean result = Boolean.TRUE.equals(this.webClientBuilder.build()
                .get()
                .uri("lb://user-microservice/api/users/exist/" + userId)
                .retrieve()
                .bodyToMono(Boolean.class)
                .block());

        if (!result) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(
                            "El usuario no existe",
                            "El usuario con ID: " + userId + " no existe"
                    ));
        }

        Optional<GameDTO> game = findGameById(libraryDTO.getGameId());

        if (game.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(
                            "Juego no encontrado",
                            "No se pudo encontrar un juego con el ID: " + libraryDTO.getGameId()
                    ));
        }

        if (validateUserAlreadyHasGame(libraryDTO.getUserId(), libraryDTO.getGameId())) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(new ErrorResponse(
                            "Juego duplicado",
                            "El juego ya existe en la biblioteca del usuario"
                    ));
        }

        boolean isValidStatus = Arrays.stream(GameStatus.values())
                .anyMatch(status -> status.name().equalsIgnoreCase(libraryDTO.getStatus().toString()));

        if (!isValidStatus) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(
                            "Estado inválido",
                            "El estado proporcionado no es válido: " + libraryDTO.getStatus()
                    ));
        }



        GameLibrary gameLibrary = new GameLibrary();
        gameLibrary.setUserId(libraryDTO.getUserId());
        gameLibrary.setGameId(libraryDTO.getGameId());
        gameLibrary.setStatus(libraryDTO.getStatus());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(gameLibraryRepository.save(gameLibrary));
    }

    public ResponseEntity<?> deleteGameLibrary(String userId, Long gameId) {
        return gameLibraryRepository
                .findByUserIdAndGameId(userId, gameId)
                .map(gameLibrary -> {
                    gameLibraryRepository.delete(gameLibrary);
                    return ResponseEntity.ok(true);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    public Optional<GameDTO> findGameById(Long gameId) {
        List<GameDTO> games = igdbService.getGamesByManyId(List.of(gameId));
        return Optional.ofNullable(games)
                .filter(list -> !list.isEmpty())
                .map(list -> list.get(0));
    }

    public ResponseEntity<?> findGameByUserIdAndGameId(String userId, Long gameId) {
        boolean result = Boolean.TRUE.equals(this.webClientBuilder.build()
                .get()
                .uri("lb://user-microservice/api/users/exist/" + userId)
                .retrieve()
                .bodyToMono(Boolean.class)
                .block());

        if (!result) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(
                            "El usuario no existe",
                            "El usuario con ID: " + userId + " no existe"
                    ));
        }

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

        return gameLibraryRepository.findByUserIdAndGameId(userId, gameId)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body(new ErrorResponse(
                                "Juego no encontrado",
                                "No se encontró un juego con el ID: " + gameId + " para el usuario con ID: " + userId
                        )));
    }


    // TODO: Crear la función para editar el estado del juego



    // HELPERS
    private boolean validateUserAlreadyHasGame(String userId, Long gameId) {
        return gameLibraryRepository
                .findByUserIdAndGameId(userId, gameId)
                .isPresent();
    }
}
