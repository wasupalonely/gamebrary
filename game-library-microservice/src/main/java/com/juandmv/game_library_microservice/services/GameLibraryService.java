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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.*;

@Service
@RequiredArgsConstructor
public class GameLibraryService {

    @Autowired
    private final GameLibraryRepository gameLibraryRepository;

    @Autowired
    private IgdbService igdbService;

    private final WebClient.Builder webClientBuilder;



    public ResponseEntity<?> getAllGamesByUserId(String userId) {
        // TODO: Validar que el usuario exista con el microservicio de usuarios
        boolean result = Boolean.TRUE.equals(this.webClientBuilder.build()
                .get()
                .uri("http://localhost:8084/users/exist/" + userId)
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
        System.out.println("games: " + games);
        System.out.println("ESTÁ VAACÍO? " + games.isEmpty());

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


    // HELPERS
    private boolean validateUserAlreadyHasGame(String userId, Long gameId) {
        return gameLibraryRepository
                .findByUserIdAndGameId(userId, gameId)
                .isPresent();
    }
}
