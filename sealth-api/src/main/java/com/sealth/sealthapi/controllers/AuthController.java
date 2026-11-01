package com.sealth.sealthapi.controllers;

import com.sealth.sealthapi.dto.LoginRequestDTO;
import com.sealth.sealthapi.dto.RegisterRequestDTO;
import com.sealth.sealthapi.dto.ResponseDTO;
import com.sealth.sealthapi.infra.exception.ApiError;
import com.sealth.sealthapi.services.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@Tag(name = "Autenticação", description = "Login e cadastro (endpoints públicos)")
@SecurityRequirements
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(summary = "Autentica o usuário e devolve o token JWT (válido por 2 horas)")
    @ApiResponse(responseCode = "200", description = "Login realizado")
    @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content(schema = @Schema(implementation = ApiError.class)))
    @ApiResponse(responseCode = "401", description = "E-mail ou senha inválidos", content = @Content(schema = @Schema(implementation = ApiError.class)))
    @PostMapping("/login")
    public ResponseEntity<ResponseDTO> login(@Valid @RequestBody LoginRequestDTO body) {
        return ResponseEntity.ok(authService.login(body));
    }

    @Operation(summary = "Cadastra um novo usuário e já devolve o token JWT")
    @ApiResponse(responseCode = "200", description = "Usuário cadastrado")
    @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content(schema = @Schema(implementation = ApiError.class)))
    @ApiResponse(responseCode = "409", description = "E-mail já cadastrado", content = @Content(schema = @Schema(implementation = ApiError.class)))
    @PostMapping("/register")
    public ResponseEntity<ResponseDTO> register(@Valid @RequestBody RegisterRequestDTO body) {
        return ResponseEntity.ok(authService.register(body));
    }
}
