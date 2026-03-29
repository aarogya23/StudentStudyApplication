package com.deltagamma.Acedemy.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.deltagamma.Acedemy.dto.AuthDtos.LoginRequest;
import com.deltagamma.Acedemy.dto.AuthDtos.LoginResponse;
import com.deltagamma.Acedemy.dto.AuthDtos.SignupRequest;
import com.deltagamma.Acedemy.model.User;
import com.deltagamma.Acedemy.service.AuthService;
import com.deltagamma.Acedemy.service.JwtService;
import com.deltagamma.Acedemy.service.UserProfileMapper;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;
    private final UserProfileMapper userProfileMapper;

    @Value("${spring.security.oauth2.client.registration.google.client-id:}")
    private String googleClientId;

    @Value("${spring.security.oauth2.client.registration.google.client-secret:}")
    private String googleClientSecret;

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody @Valid SignupRequest request) {
        try {
            User user = authService.signup(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(userProfileMapper.toDto(user));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(java.util.Map.of("message", ex.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Valid LoginRequest request) {
        try {
            User user = authService.authenticate(request);
            return ResponseEntity.ok(LoginResponse.builder()
                    .token(jwtService.generateToken(user))
                    .expiresIn(jwtService.getJwtExpirationMs())
                    .user(userProfileMapper.toDto(user))
                    .build());
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(java.util.Map.of("message", ex.getMessage()));
        }
    }

    @GetMapping("/oauth-enabled")
    public java.util.Map<String, Boolean> oauthEnabled() {
        boolean enabled = googleClientId != null
                && !googleClientId.isBlank()
                && googleClientSecret != null
                && !googleClientSecret.isBlank();
        return java.util.Map.of("google", enabled);
    }
}
