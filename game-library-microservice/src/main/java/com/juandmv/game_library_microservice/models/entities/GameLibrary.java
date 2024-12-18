package com.juandmv.game_library_microservice.models.entities;

import com.juandmv.game_library_microservice.enums.GameStatus;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "game_library")
public class GameLibrary {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    private String userId;
    private Long gameId;
    private GameStatus status;
}
