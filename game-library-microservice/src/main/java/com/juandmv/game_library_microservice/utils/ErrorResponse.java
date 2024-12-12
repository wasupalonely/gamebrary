package com.juandmv.game_library_microservice.utils;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Getter
@Setter
public class ErrorResponse {
    private String message;
    private String details;

    public ErrorResponse(String message, String details) {
        this.message = message;
        this.details = details;
    }
}
