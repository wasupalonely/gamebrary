package com.juandmv.game_library_microservice.models.dto;

import com.juandmv.game_library_microservice.enums.RoleEnum;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class UserDTO {

    @NotEmpty(message = "El username es requerido")
    private String username;

    @NotEmpty(message = "El email es requerido")
    @Email(message = "El email debe ser válido")
    private String email;

    @NotEmpty(message = "El password es requerido")
    private String password;

    private RoleEnum role;

    private boolean enabled;
}
