package com.juandmv.game_library_microservice.services;

import com.juandmv.game_library_microservice.dto.GameDTO;
import com.juandmv.game_library_microservice.dto.LibraryDTO;
import com.juandmv.game_library_microservice.models.entities.GameLibrary;
import com.juandmv.game_library_microservice.repository.GameLibraryRepository;
import com.juandmv.game_library_microservice.utils.ErrorResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class GameLibraryService {

    private final GameLibraryRepository gameLibraryRepository;

    @Autowired
    private IgdbService igdbService;

    public GameLibraryService(GameLibraryRepository gameLibraryRepository) {
        this.gameLibraryRepository = gameLibraryRepository;
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

        GameLibrary gameLibrary = new GameLibrary();
        gameLibrary.setUserId(libraryDTO.getUserId());
        gameLibrary.setGameId(libraryDTO.getGameId());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(gameLibraryRepository.save(gameLibrary));
    }

    private Optional<GameDTO> findGameById(Long gameId) {
        List<GameDTO> games = igdbService.getGamesByManyId(List.of(gameId));
        return Optional.ofNullable(games)
                .filter(list -> !list.isEmpty())
                .map(list -> list.get(0));
    }

    private boolean validateUserAlreadyHasGame(String userId, Long gameId) {
        return gameLibraryRepository
                .findByUserIdAndGameId(userId, gameId)
                .isPresent();
    }
}
