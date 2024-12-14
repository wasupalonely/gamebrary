package com.juandmv.user_microservice.utils;

public record BaseResponse(String[] errorMessages) {
    public boolean hasErrors() { return errorMessages != null && errorMessages.length > 0; }
}
