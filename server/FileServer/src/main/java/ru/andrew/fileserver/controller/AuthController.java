package ru.andrew.fileserver.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.andrew.fileserver.dto.AuthResponse;
import ru.andrew.fileserver.entities.FileUser;
import ru.andrew.fileserver.service.AuthService;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping(value = "/signup", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AuthResponse> signUp(@RequestBody FileUser fileUser) {
        return authService.signUp(fileUser);
    }

    @PostMapping(value = "/login", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AuthResponse> login(@RequestBody FileUser fileUser) {
        return authService.login(fileUser);
    }

    @GetMapping(value = "/authenticate", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<String> authenticate() {
        return ResponseEntity.ok("");
    }
}
