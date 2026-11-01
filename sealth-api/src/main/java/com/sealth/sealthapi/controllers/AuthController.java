package com.sealth.sealthapi.controllers;

import com.sealth.sealthapi.dto.LoginRequestDTO;
import com.sealth.sealthapi.dto.RegisterRequestDTO;
import com.sealth.sealthapi.dto.ResponseDTO;
import com.sealth.sealthapi.services.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<ResponseDTO> login(@Valid @RequestBody LoginRequestDTO body) {
        return ResponseEntity.ok(authService.login(body));
    }

    @PostMapping("/register")
    public ResponseEntity<ResponseDTO> register(@Valid @RequestBody RegisterRequestDTO body) {
        return ResponseEntity.ok(authService.register(body));
    }
}
