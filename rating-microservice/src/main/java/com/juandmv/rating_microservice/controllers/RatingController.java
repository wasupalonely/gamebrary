package com.juandmv.rating_microservice.controllers;

import com.juandmv.rating_microservice.models.dto.RatingDTO;
import com.juandmv.rating_microservice.services.RatingService;
import com.juandmv.rating_microservice.utils.Utils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/rating")
@RequiredArgsConstructor
public class RatingController {

    @Autowired
    private final RatingService ratingService;

    @PostMapping("/")
    public ResponseEntity<?> saveRating(@Valid @RequestBody RatingDTO ratingDTO, BindingResult result) {
        if (result.hasErrors()) return Utils.validation(result);
        return ratingService.saveRating(ratingDTO);
    }

    // TODO: HACER TODOS LOS DEMÁS ENDPOINTS PARA RATING, COMO: OBTENER RATING POR ID, OBTENER RATING POR JUEGO, OBTENER RATING POR USUARIO
    // Y PODER EDITAR LOS RATINGS
}
