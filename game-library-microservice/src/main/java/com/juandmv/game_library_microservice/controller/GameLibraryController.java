package com.juandmv.game_library_microservice.controller;

import com.juandmv.game_library_microservice.dto.GameDTO;
import com.juandmv.game_library_microservice.dto.LibraryDTO;
import com.juandmv.game_library_microservice.models.entities.GameLibrary;
import com.juandmv.game_library_microservice.services.GameLibraryService;
import com.juandmv.game_library_microservice.services.IgdbService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/games")
public class GameLibraryController {

    @Autowired
    private GameLibraryService gameService;

    @Autowired
    private IgdbService igdbService;

    @GetMapping("/igdb")
    public List<GameDTO> getAllIgdbGames() {
        return igdbService.getGames();
    }

    @GetMapping("/ids")
    public List<GameDTO> getGamesByManyId(@RequestBody List<Long> igdbIds) {
        return igdbService.getGamesByManyId(igdbIds);
    }

    @PostMapping("/")
    public ResponseEntity<?> saveGame(@RequestBody LibraryDTO gameLibrary) {
        System.out.println("Recibiendo gameLibrary game id: " + gameLibrary.getGameId());
        System.out.println("Recibiendo gameLibrary user id: " + gameLibrary.getUserId());
        return gameService.saveGameLibrary(gameLibrary);
    }
}
