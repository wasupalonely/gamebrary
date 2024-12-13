package com.juandmv.game_library_microservice.services;

import com.juandmv.game_library_microservice.dto.GameDTO;
import com.juandmv.game_library_microservice.dto.LibraryDTO;
import com.juandmv.game_library_microservice.enums.GameStatus;
import com.juandmv.game_library_microservice.models.entities.GameLibrary;
import com.juandmv.game_library_microservice.repository.GameLibraryRepository;
import com.juandmv.game_library_microservice.utils.ErrorResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class GameLibraryService {

    @Autowired
    private final GameLibraryRepository gameLibraryRepository;

    @Autowired
    private IgdbService igdbService;

    public GameLibraryService(GameLibraryRepository gameLibraryRepository) {
        this.gameLibraryRepository = gameLibraryRepository;
    }

    public ResponseEntity<?> getAllGamesByUserId(String userId) {
        // TODO: Validar que el usuario exista con el microservicio de usuarios
        Optional<List<GameLibrary>> games = gameLibraryRepository.findByUserId(userId);

        if (games.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(
                            "Juegos no encontrados",
                            "No se encontraron juegos para el usuario: " + userId
                    ));
        }

        List<Long> igdbIds = games
                .get()
                .stream()
                .map(GameLibrary::getGameId)
                .toList();

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(igdbService.getGamesByManyId(igdbIds));

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
