package com.juandmv.user_microservice.controllers;

import com.juandmv.user_microservice.model.dto.UserDTO;
import com.juandmv.user_microservice.model.entities.User;
import com.juandmv.user_microservice.services.UserService;
import com.juandmv.user_microservice.utils.Utils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/exist/{userId}")
    @ResponseStatus(HttpStatus.OK)
    public boolean existUser(@PathVariable String userId) {
        return userService.existUser(userId);
    }

    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<?> createUser(@Valid @RequestBody UserDTO user, BindingResult result) {
        if (result.hasErrors()) return Utils.validation(result);
        return userService.createUser(user);
    }
}
