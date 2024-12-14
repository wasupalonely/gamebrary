package com.juandmv.game_library_microservice.controllers;

import com.juandmv.game_library_microservice.models.dto.GameDTO;
import com.juandmv.game_library_microservice.models.dto.LibraryDTO;
import com.juandmv.game_library_microservice.services.GameLibraryService;
import com.juandmv.game_library_microservice.services.IgdbService;
import com.juandmv.game_library_microservice.utils.Utils;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/games")
public class GameLibraryController {

    @Autowired
    private GameLibraryService gameService;

    @Autowired
    private IgdbService igdbService;

    @GetMapping("/{id}")
    public ResponseEntity<GameDTO> getGameById(@PathVariable Long id) {
        return gameService.findGameById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }


    @GetMapping("/igdb")
    public List<GameDTO> getAllIgdbGames() {
        return igdbService.getGames();
    }

    @GetMapping("/ids")
    public List<GameDTO> getGamesByManyId(@RequestBody List<Long> igdbIds) {
        return igdbService.getGamesByManyId(igdbIds);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getAllGamesByUserId(@PathVariable String userId) {
        return gameService.getAllGamesByUserId(userId);
    }

    @PostMapping("/")
    public ResponseEntity<?> saveGame(@Valid @RequestBody LibraryDTO gameLibrary, BindingResult result) {
        if (result.hasErrors()) return Utils.validation(result);
        System.out.println("Recibiendo gameLibrary game id: " + gameLibrary.getGameId());
        System.out.println("Recibiendo gameLibrary user id: " + gameLibrary.getUserId());
        return gameService.saveGameLibrary(gameLibrary);
    }

    @DeleteMapping("/user/{userId}/game/{gameId}")
    public ResponseEntity<?> deleteGame(@PathVariable String userId, @PathVariable Long gameId) {
        return gameService.deleteGameLibrary(userId, gameId);
    }
}
