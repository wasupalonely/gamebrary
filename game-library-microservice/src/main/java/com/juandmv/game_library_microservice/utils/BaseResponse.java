package com.juandmv.game_library_microservice.utils;

public record BaseResponse(String[] errorMessages) {
    public boolean hasErrors() { return errorMessages != null && errorMessages.length > 0; }
}
