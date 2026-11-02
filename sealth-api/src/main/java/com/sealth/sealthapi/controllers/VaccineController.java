package com.sealth.sealthapi.controllers;

import com.sealth.sealthapi.domain.user.User;
import com.sealth.sealthapi.dto.VaccinationOverviewDTO;
import com.sealth.sealthapi.dto.VaccinationRecordRequestDTO;
import com.sealth.sealthapi.dto.VaccinationRecordResponseDTO;
import com.sealth.sealthapi.dto.VaccineStatusDTO;
import com.sealth.sealthapi.infra.exception.ApiError;
import com.sealth.sealthapi.services.VaccineService;
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
@RequestMapping("/vaccines")
@Tag(name = "Vacinas", description = "Calendário de vacinação, doses tomadas e alertas do usuário autenticado")
@ApiResponse(responseCode = "401", description = "Token ausente ou inválido", content = @Content(schema = @Schema(implementation = ApiError.class)))
public class VaccineController {

    private final VaccineService service;

    public VaccineController(VaccineService service) {
        this.service = service;
    }

    @Operation(summary = "Calendário de vacinação com a situação de cada vacina para o usuário (a idade vem da última medição de saúde)")
    @GetMapping
    public ResponseEntity<VaccinationOverviewDTO> overview(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(service.overview(user));
    }

    @Operation(summary = "Alertas de vacinação: reforços vencidos, esquemas incompletos e reforços previstos para os próximos 30 dias")
    @GetMapping("/alerts")
    public ResponseEntity<List<VaccineStatusDTO>> alerts(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(service.alerts(user));
    }

    @Operation(summary = "Registra uma dose tomada")
    @ApiResponse(responseCode = "201", description = "Dose registrada")
    @ApiResponse(responseCode = "400", description = "Dados inválidos (vacina desconhecida ou data futura)", content = @Content(schema = @Schema(implementation = ApiError.class)))
    @PostMapping("/records")
    public ResponseEntity<VaccinationRecordResponseDTO> addRecord(@AuthenticationPrincipal User user,
                                                                  @Valid @RequestBody VaccinationRecordRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.addRecord(user, request));
    }

    @Operation(summary = "Lista as doses registradas, da mais recente para a mais antiga")
    @GetMapping("/records")
    public ResponseEntity<List<VaccinationRecordResponseDTO>> listRecords(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(service.listRecords(user));
    }

    @Operation(summary = "Remove uma dose registrada")
    @ApiResponse(responseCode = "204", description = "Dose removida")
    @ApiResponse(responseCode = "404", description = "Dose não encontrada (ou de outro usuário)", content = @Content(schema = @Schema(implementation = ApiError.class)))
    @DeleteMapping("/records/{recordId}")
    public ResponseEntity<Void> deleteRecord(@AuthenticationPrincipal User user, @PathVariable Long recordId) {
        service.deleteRecord(user, recordId);
        return ResponseEntity.noContent().build();
    }
}
