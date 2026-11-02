package com.sealth.sealthapi.controllers;

import com.sealth.sealthapi.domain.user.User;
import com.sealth.sealthapi.dto.CreateHealthRecordRequestDTO;
import com.sealth.sealthapi.dto.HealthRecordResponseDTO;
import com.sealth.sealthapi.infra.exception.ApiError;
import com.sealth.sealthapi.services.HealthRecordService;
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
@RequestMapping("/health/records")
@Tag(name = "Saúde", description = "Histórico de medições de saúde do usuário autenticado")
@ApiResponse(responseCode = "401", description = "Token ausente ou inválido", content = @Content(schema = @Schema(implementation = ApiError.class)))
public class HealthRecordController {

    private final HealthRecordService service;

    public HealthRecordController(HealthRecordService service) {
        this.service = service;
    }

    @Operation(summary = "Registra uma nova medição (peso, altura, idade, sexo e, opcionalmente, pressão arterial)")
    @ApiResponse(responseCode = "201", description = "Medição registrada")
    @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content(schema = @Schema(implementation = ApiError.class)))
    @PostMapping
    public ResponseEntity<HealthRecordResponseDTO> create(@AuthenticationPrincipal User user,
                                                          @Valid @RequestBody CreateHealthRecordRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(user, request));
    }

    @Operation(summary = "Lista as medições do usuário, da mais recente para a mais antiga")
    @GetMapping
    public ResponseEntity<List<HealthRecordResponseDTO>> list(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(service.list(user));
    }

    @Operation(summary = "Devolve a medição mais recente do usuário")
    @ApiResponse(responseCode = "404", description = "O usuário ainda não registrou nenhuma medição", content = @Content(schema = @Schema(implementation = ApiError.class)))
    @GetMapping("/latest")
    public ResponseEntity<HealthRecordResponseDTO> latest(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(service.latest(user));
    }

    @Operation(summary = "Remove uma medição do usuário")
    @ApiResponse(responseCode = "204", description = "Medição removida")
    @ApiResponse(responseCode = "404", description = "Medição não encontrada (ou de outro usuário)", content = @Content(schema = @Schema(implementation = ApiError.class)))
    @DeleteMapping("/{recordId}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal User user, @PathVariable Long recordId) {
        service.delete(user, recordId);
        return ResponseEntity.noContent().build();
    }
}
