package com.juandmv.rating_microservice.models.dto;

import com.juandmv.rating_microservice.enums.GameStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LibraryDTO {
    @NotBlank(message = "El id del usuario es requerido")
    private String userId;

    @NotNull(message = "El id del juego es requerido")
    private Long gameId;

    private GameStatus status;
}
