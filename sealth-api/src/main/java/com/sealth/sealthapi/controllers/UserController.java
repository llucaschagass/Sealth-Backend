package com.sealth.sealthapi.controllers;

import com.sealth.sealthapi.domain.user.User;
import com.sealth.sealthapi.dto.UpdateUserRequestDTO;
import com.sealth.sealthapi.dto.UserResponseDTO;
import com.sealth.sealthapi.infra.exception.ApiError;
import com.sealth.sealthapi.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@Tag(name = "Usuário", description = "Dados do próprio usuário autenticado")
@ApiResponse(responseCode = "401", description = "Token ausente ou inválido", content = @Content(schema = @Schema(implementation = ApiError.class)))
@ApiResponse(responseCode = "403", description = "Tentativa de acessar outro usuário", content = @Content(schema = @Schema(implementation = ApiError.class)))
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "Atualiza nome, e-mail e/ou senha (campos vazios ou ausentes são mantidos)")
    @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content(schema = @Schema(implementation = ApiError.class)))
    @ApiResponse(responseCode = "409", description = "E-mail já cadastrado", content = @Content(schema = @Schema(implementation = ApiError.class)))
    @PutMapping("/update/{userId}")
    public ResponseEntity<UserResponseDTO> updateUser(@AuthenticationPrincipal User user,
                                                      @PathVariable String userId,
                                                      @Valid @RequestBody UpdateUserRequestDTO request) {
        return ResponseEntity.ok(userService.updateUser(user, userId, request));
    }

    @Operation(summary = "Consulta os dados do usuário autenticado")
    @GetMapping("/{userId}")
    public ResponseEntity<UserResponseDTO> getUserById(@AuthenticationPrincipal User user, @PathVariable String userId) {
        return ResponseEntity.ok(userService.getUser(user, userId));
    }
}
