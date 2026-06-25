package com.example.ProductComparison.Service;

import com.example.ProductComparison.DTO.AuthLoginRequestDto;
import com.example.ProductComparison.DTO.AuthAdminRegisterRequestDto;
import com.example.ProductComparison.DTO.AuthRegisterRequestDto;
import com.example.ProductComparison.DTO.AuthResponseDto;
import com.example.ProductComparison.Entity.AppUser;
import com.example.ProductComparison.Entity.Role;
import com.example.ProductComparison.Exception.DuplicateUsernameException;
import com.example.ProductComparison.Mapper.AuthMapper;
import com.example.ProductComparison.Repository.UserRepo;
import com.example.ProductComparison.Security.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {
    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthMapper authMapper;
    private final String adminRegistrationKey;

    public AuthService(
            UserRepo userRepo,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            AuthMapper authMapper,
            @Value("${app.admin.registration-key:}") String adminRegistrationKey
    ) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authMapper = authMapper;
        this.adminRegistrationKey = adminRegistrationKey;
    }

    @Transactional
    public AuthResponseDto register(AuthRegisterRequestDto req) {
        if (userRepo.existsByUsernameIgnoreCase(req.getUsername())) {
            throw new DuplicateUsernameException(req.getUsername());
        }
        AppUser user = AppUser.builder()
                .username(req.getUsername().trim())
                .passwordHash(passwordEncoder.encode(req.getPassword()))
                .role(Role.USER)
                .build();
        userRepo.save(user);

        String token = jwtService.generateToken(user.getUsername(), user.getRole().name());
        return authMapper.toAuthResponseDto(user, token);
    }

    @Transactional
    public AuthResponseDto registerAdmin(AuthAdminRegisterRequestDto req) {
        if (adminRegistrationKey == null || adminRegistrationKey.isBlank()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "admin registration is disabled");
        }
        if (!adminRegistrationKey.equals(req.getAdminKey())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "invalid admin key");
        }
        if (userRepo.existsByUsernameIgnoreCase(req.getUsername())) {
            throw new DuplicateUsernameException(req.getUsername());
        }

        AppUser user = AppUser.builder()
                .username(req.getUsername().trim())
                .passwordHash(passwordEncoder.encode(req.getPassword()))
                .role(Role.ADMIN)
                .build();
        userRepo.save(user);

        String token = jwtService.generateToken(user.getUsername(), user.getRole().name());
        return authMapper.toAuthResponseDto(user, token);
    }

    @Transactional(readOnly = true)
    public AuthResponseDto login(AuthLoginRequestDto req) {
        AppUser user = userRepo.findByUsernameIgnoreCase(req.getUsername().trim())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "invalid credentials"));

        if (!passwordEncoder.matches(req.getPassword(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "invalid credentials");
        }

        String token = jwtService.generateToken(user.getUsername(), user.getRole().name());
        return authMapper.toAuthResponseDto(user, token);
    }

    public AppUser getCurrentUser() {
        String username = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        return userRepo.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}

