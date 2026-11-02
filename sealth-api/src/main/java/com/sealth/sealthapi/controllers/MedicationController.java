package com.sealth.sealthapi.controllers;

import com.sealth.sealthapi.domain.user.User;
import com.sealth.sealthapi.dto.MedicationRequestDTO;
import com.sealth.sealthapi.dto.MedicationResponseDTO;
import com.sealth.sealthapi.infra.exception.ApiError;
import com.sealth.sealthapi.services.MedicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/medicines/mine")
@Tag(name = "Medicamentos", description = "Medicamentos em uso pelo usuário autenticado, com dosagem e horários")
@ApiResponse(responseCode = "401", description = "Token ausente ou inválido", content = @Content(schema = @Schema(implementation = ApiError.class)))
public class MedicationController {

    private final MedicationService service;

    public MedicationController(MedicationService service) {
        this.service = service;
    }

    @Operation(summary = "Cadastra um medicamento (nome, dosagem, horários HH:mm e, opcionalmente, período e observações)")
    @ApiResponse(responseCode = "201", description = "Medicamento cadastrado")
    @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content(schema = @Schema(implementation = ApiError.class)))
    @PostMapping
    public ResponseEntity<MedicationResponseDTO> create(@AuthenticationPrincipal User user,
                                                        @Valid @RequestBody MedicationRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(user, request));
    }

    @Operation(summary = "Lista os medicamentos do usuário: os em uso primeiro, depois os encerrados")
    @GetMapping
    public ResponseEntity<List<MedicationResponseDTO>> list(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(service.list(user));
    }

    @Operation(summary = "Atualiza um medicamento (substitui todos os campos)")
    @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content(schema = @Schema(implementation = ApiError.class)))
    @ApiResponse(responseCode = "404", description = "Medicamento não encontrado (ou de outro usuário)", content = @Content(schema = @Schema(implementation = ApiError.class)))
    @PutMapping("/{medicationId}")
    public ResponseEntity<MedicationResponseDTO> update(@AuthenticationPrincipal User user,
                                                        @PathVariable Long medicationId,
                                                        @Valid @RequestBody MedicationRequestDTO request) {
        return ResponseEntity.ok(service.update(user, medicationId, request));
    }

    @Operation(summary = "Remove um medicamento do usuário")
    @ApiResponse(responseCode = "204", description = "Medicamento removido")
    @ApiResponse(responseCode = "404", description = "Medicamento não encontrado (ou de outro usuário)", content = @Content(schema = @Schema(implementation = ApiError.class)))
    @DeleteMapping("/{medicationId}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal User user, @PathVariable Long medicationId) {
        service.delete(user, medicationId);
        return ResponseEntity.noContent().build();
    }
}
