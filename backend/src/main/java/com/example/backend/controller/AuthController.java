package com.example.backend.controller;

import com.example.backend.dto.auth.AuthUserResponse;
import com.example.backend.dto.auth.ForgotPasswordRequest;
import com.example.backend.dto.auth.LoginRequest;
import com.example.backend.dto.auth.MessageResponse;
import com.example.backend.dto.auth.ResetPasswordRequest;
import com.example.backend.service.interfaces.IAuthentificationService;
import com.example.backend.service.interfaces.IPasswordResetService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final IAuthentificationService authentificationService;
    private final IPasswordResetService passwordResetService;

    public AuthController(
            IAuthentificationService authentificationService,
            IPasswordResetService passwordResetService) {
        this.authentificationService = authentificationService;
        this.passwordResetService = passwordResetService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthUserResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authentificationService.login(request.getEmail(), request.getPassword()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        authentificationService.logout();
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<MessageResponse> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        passwordResetService.forgotPassword(request == null ? null : request.getEmail());
        return ResponseEntity.ok(new MessageResponse(
                "Si un compte existe avec cet email, un lien de reinitialisation a ete envoye."));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<MessageResponse> resetPassword(@RequestBody ResetPasswordRequest request) {
        passwordResetService.resetPassword(
                request == null ? null : request.getToken(),
                request == null ? null : request.getNewPassword(),
                request == null ? null : request.getConfirmPassword());
        return ResponseEntity.ok(new MessageResponse("Mot de passe reinitialise avec succes."));
    }

    @GetMapping("/me")
    public ResponseEntity<AuthUserResponse> me() {
        return ResponseEntity.ok(authentificationService.getCurrentUser());
    }
}
