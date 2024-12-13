package com.juandmv.game_library_microservice.dto;

import com.juandmv.game_library_microservice.enums.GameStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LibraryDTO {
    @NotBlank(message = "El id del usuario es requerido")
    private String userId;

    @NotNull(message = "El id del juego es requerido")
    private Long gameId;

    private GameStatus status;
}
