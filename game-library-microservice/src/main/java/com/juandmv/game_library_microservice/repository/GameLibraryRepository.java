package com.juandmv.game_library_microservice.repository;

import com.juandmv.game_library_microservice.models.entities.GameLibrary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GameLibraryRepository extends JpaRepository<GameLibrary, String> {
    Optional<GameLibrary> findByUserId(String userId);
    Optional<GameLibrary> findByUserIdAndGameId(String userId, Long gameId);
}
