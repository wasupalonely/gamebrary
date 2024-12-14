package com.juandmv.rating_microservice.repository;

import com.juandmv.rating_microservice.models.entities.Rating;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RatingRepository extends JpaRepository<Rating, String> {

    Optional<Rating> findByUserIdAndGameId(String userId, Long gameId);
    Optional<List<Rating>> findByGameId(Long gameId);
    Optional<List<Rating>> findByUserId(String userId);
}
