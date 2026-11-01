package com.sealth.sealthapi.services;

import com.sealth.sealthapi.domain.user.User;
import com.sealth.sealthapi.dto.UpdateUserRequestDTO;
import com.sealth.sealthapi.dto.UserResponseDTO;
import com.sealth.sealthapi.infra.exception.ConflictException;
import com.sealth.sealthapi.infra.exception.ForbiddenException;
import com.sealth.sealthapi.infra.exception.ResourceNotFoundException;
import com.sealth.sealthapi.repositories.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserResponseDTO updateUser(User authenticated, String userId, UpdateUserRequestDTO request) {
        User user = findAuthorized(authenticated, userId);

        if (hasText(request.name())) {
            user.setName(request.name().trim());
        }
        if (hasText(request.email()) && !request.email().trim().equalsIgnoreCase(user.getEmail())) {
            if (userRepository.existsByEmail(request.email().trim())) {
                throw new ConflictException("E-mail já cadastrado");
            }
            user.setEmail(request.email().trim());
        }
        if (hasText(request.password())) {
            user.setPassword(passwordEncoder.encode(request.password()));
        }

        return UserResponseDTO.from(userRepository.save(user));
    }

    public UserResponseDTO getUser(User authenticated, String userId) {
        return UserResponseDTO.from(findAuthorized(authenticated, userId));
    }

    private User findAuthorized(User authenticated, String userId) {
        if (!authenticated.getId().equals(userId)) {
            throw new ForbiddenException("Acesso negado");
        }
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
