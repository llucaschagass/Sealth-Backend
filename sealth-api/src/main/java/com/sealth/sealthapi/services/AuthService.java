package com.sealth.sealthapi.services;

import com.sealth.sealthapi.domain.user.User;
import com.sealth.sealthapi.dto.LoginRequestDTO;
import com.sealth.sealthapi.dto.RegisterRequestDTO;
import com.sealth.sealthapi.dto.ResponseDTO;
import com.sealth.sealthapi.infra.exception.ConflictException;
import com.sealth.sealthapi.infra.security.TokenService;
import com.sealth.sealthapi.repositories.UserRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, TokenService tokenService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
    }

    public ResponseDTO login(LoginRequestDTO request) {
        User user = userRepository.findByEmail(request.email())
                .filter(found -> passwordEncoder.matches(request.password(), found.getPassword()))
                .orElseThrow(() -> new BadCredentialsException("E-mail ou senha inválidos"));
        return toResponse(user);
    }

    public ResponseDTO register(RegisterRequestDTO request) {
        if (userRepository.existsByEmail(request.email().trim())) {
            throw new ConflictException("E-mail já cadastrado");
        }
        User user = new User();
        user.setName(request.name().trim());
        user.setEmail(request.email().trim());
        user.setPassword(passwordEncoder.encode(request.password()));
        return toResponse(userRepository.save(user));
    }

    private ResponseDTO toResponse(User user) {
        return new ResponseDTO(user.getName(), tokenService.generateToken(user), user.getId());
    }
}
