package com.sealth.sealthapi.controllers;

import com.sealth.sealthapi.domain.user.User;
import com.sealth.sealthapi.dto.AppointmentResponseDTO;
import com.sealth.sealthapi.dto.CreateAppointmentRequestDTO;
import com.sealth.sealthapi.dto.UpdateAppointmentRequestDTO;
import com.sealth.sealthapi.services.AppointmentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @PostMapping("/create")
    public ResponseEntity<AppointmentResponseDTO> createAppointment(@AuthenticationPrincipal User user,
                                                                    @Valid @RequestBody CreateAppointmentRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(appointmentService.createAppointment(user, request));
    }

    @GetMapping("/all")
    public ResponseEntity<List<AppointmentResponseDTO>> getAllAppointments(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(appointmentService.getAppointments(user));
    }

    @PutMapping("/{appointmentId}/update")
    public ResponseEntity<AppointmentResponseDTO> updateAppointment(@AuthenticationPrincipal User user,
                                                                    @PathVariable Long appointmentId,
                                                                    @Valid @RequestBody UpdateAppointmentRequestDTO request) {
        return ResponseEntity.ok(appointmentService.updateAppointment(user, appointmentId, request));
    }

    @DeleteMapping("/{appointmentId}/cancel")
    public ResponseEntity<Void> cancelAppointment(@AuthenticationPrincipal User user, @PathVariable Long appointmentId) {
        appointmentService.cancelAppointment(user, appointmentId);
        return ResponseEntity.noContent().build();
    }
}
