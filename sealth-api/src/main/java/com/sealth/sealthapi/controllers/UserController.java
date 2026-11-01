package com.sealth.sealthapi.controllers;

import com.sealth.sealthapi.domain.user.User;
import com.sealth.sealthapi.dto.UpdateUserRequestDTO;
import com.sealth.sealthapi.dto.UserResponseDTO;
import com.sealth.sealthapi.services.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PutMapping("/update/{userId}")
    public ResponseEntity<UserResponseDTO> updateUser(@AuthenticationPrincipal User user,
                                                      @PathVariable String userId,
                                                      @Valid @RequestBody UpdateUserRequestDTO request) {
        return ResponseEntity.ok(userService.updateUser(user, userId, request));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserResponseDTO> getUserById(@AuthenticationPrincipal User user, @PathVariable String userId) {
        return ResponseEntity.ok(userService.getUser(user, userId));
    }
}
