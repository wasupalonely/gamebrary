package com.juandmv.user_microservice.model.dto;

import com.juandmv.user_microservice.enums.RoleEnum;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
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
