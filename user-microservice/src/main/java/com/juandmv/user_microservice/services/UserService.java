package com.juandmv.user_microservice.services;

import com.juandmv.user_microservice.enums.RoleEnum;
import com.juandmv.user_microservice.model.dto.UserDTO;
import com.juandmv.user_microservice.model.entities.User;
import com.juandmv.user_microservice.repository.UserRepository;
import com.juandmv.user_microservice.utils.ErrorResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public boolean existUser(String userId) {
        return userRepository.existsById(userId);
    }

    public ResponseEntity<?> createUser(UserDTO userDTO) {
        Optional<User> emailExists = userRepository.findByEmail(userDTO.getEmail());
        Optional<User> usernameExists = userRepository.findByUsername(userDTO.getUsername());

        if (emailExists.isPresent() || usernameExists.isPresent()) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Error al crear el usuario", "El email o el username ya existen"));
        }

        User user = new User();
        user.setUsername(userDTO.getUsername());
        user.setEmail(userDTO.getEmail());
        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        user.setRole(RoleEnum.USER.name());
        user.setEnabled(true);

        userRepository.save(user);

        return ResponseEntity.status(201).body(user);
    }
}
