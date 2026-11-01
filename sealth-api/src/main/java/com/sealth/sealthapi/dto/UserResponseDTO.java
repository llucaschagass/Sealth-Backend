package com.sealth.sealthapi.dto;

import com.sealth.sealthapi.domain.user.User;

public record UserResponseDTO(String id, String name, String email) {

    public static UserResponseDTO from(User user) {
        return new UserResponseDTO(user.getId(), user.getName(), user.getEmail());
    }
}
