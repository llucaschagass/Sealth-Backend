package com.sealth.sealthapi.controllers;

import com.sealth.sealthapi.domain.user.User;
import com.sealth.sealthapi.dto.AppointmentResponseDTO;
import com.sealth.sealthapi.dto.CreateAppointmentRequestDTO;
import com.sealth.sealthapi.dto.UpdateAppointmentRequestDTO;
import com.sealth.sealthapi.infra.exception.ApiError;
import com.sealth.sealthapi.services.AppointmentService;
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
@RequestMapping("/appointments")
@Tag(name = "Consultas", description = "Consultas do usuário autenticado")
@ApiResponse(responseCode = "401", description = "Token ausente ou inválido", content = @Content(schema = @Schema(implementation = ApiError.class)))
public class AppointmentController {

    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @Operation(summary = "Cria uma consulta para o usuário autenticado")
    @ApiResponse(responseCode = "201", description = "Consulta criada")
    @ApiResponse(responseCode = "400", description = "Dados inválidos (data yyyy-MM-dd, horário HH:mm)", content = @Content(schema = @Schema(implementation = ApiError.class)))
    @PostMapping("/create")
    public ResponseEntity<AppointmentResponseDTO> createAppointment(@AuthenticationPrincipal User user,
                                                                    @Valid @RequestBody CreateAppointmentRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(appointmentService.createAppointment(user, request));
    }

    @Operation(summary = "Lista as consultas do usuário autenticado, ordenadas por data")
    @GetMapping("/all")
    public ResponseEntity<List<AppointmentResponseDTO>> getAllAppointments(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(appointmentService.getAppointments(user));
    }

    @Operation(summary = "Lista os lembretes em vigor: consultas com horário e lembrete configurado cujo aviso já valeu e que ainda não começaram")
    @GetMapping("/reminders")
    public ResponseEntity<List<AppointmentResponseDTO>> getDueReminders(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(appointmentService.getDueReminders(user));
    }

    @Operation(summary = "Atualiza uma consulta do usuário autenticado")
    @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content(schema = @Schema(implementation = ApiError.class)))
    @ApiResponse(responseCode = "404", description = "Consulta não encontrada (ou de outro usuário)", content = @Content(schema = @Schema(implementation = ApiError.class)))
    @PutMapping("/{appointmentId}/update")
    public ResponseEntity<AppointmentResponseDTO> updateAppointment(@AuthenticationPrincipal User user,
                                                                    @PathVariable Long appointmentId,
                                                                    @Valid @RequestBody UpdateAppointmentRequestDTO request) {
        return ResponseEntity.ok(appointmentService.updateAppointment(user, appointmentId, request));
    }

    @Operation(summary = "Cancela (remove) uma consulta do usuário autenticado")
    @ApiResponse(responseCode = "204", description = "Consulta cancelada")
    @ApiResponse(responseCode = "404", description = "Consulta não encontrada (ou de outro usuário)", content = @Content(schema = @Schema(implementation = ApiError.class)))
    @DeleteMapping("/{appointmentId}/cancel")
    public ResponseEntity<Void> cancelAppointment(@AuthenticationPrincipal User user, @PathVariable Long appointmentId) {
        appointmentService.cancelAppointment(user, appointmentId);
        return ResponseEntity.noContent().build();
    }
}
