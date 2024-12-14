package com.juandmv.rating_microservice.models.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RatingDTO {

    @NotBlank(message = "El id del usuario es requerido")
    private String userId;

    @NotNull(message = "El id del juego es requerido")
    private Long gameId;

    @NotNull(message = "La puntuación es requerida")
    @Max(value = 5, message = "La puntuación debe estar entre 1 y 5")
    @Min(value = 1, message = "La puntuación debe estar entre 1 y 5")
    private Integer rating;

    private String comment;
}
