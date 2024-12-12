package com.juandmv.game_library_microservice.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LibraryDTO {

    private Long id;
    private String userId;
    private Long gameId;
}
