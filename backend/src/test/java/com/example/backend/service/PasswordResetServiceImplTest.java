package com.example.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.backend.exception.BadRequestException;
import com.example.backend.model.PasswordResetToken;
import com.example.backend.model.Utilisateur;
import com.example.backend.model.enums.Role;
import com.example.backend.repository.PasswordResetTokenRepository;
import com.example.backend.repository.UtilisateurRepository;
import com.example.backend.service.impl.PasswordResetServiceImpl;
import com.example.backend.service.interfaces.IEmailService;
import com.example.backend.testutil.TestFixtures;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceImplTest {

    @Mock
    private UtilisateurRepository utilisateurRepository;

    @Mock
    private PasswordResetTokenRepository tokenRepository;

    @Mock
    private IEmailService emailService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private Environment environment;

    private PasswordResetServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new PasswordResetServiceImpl(
                utilisateurRepository,
                tokenRepository,
                emailService,
                passwordEncoder,
                environment,
                "http://localhost:4200",
                30);
    }

    @Test
    void forgotPasswordIgnoresInvalidEmailWithoutRevealingAccountExistence() {
        service.forgotPassword("not-an-email");

        verify(utilisateurRepository, never()).findByEmail(anyString());
        verify(tokenRepository, never()).save(any());
        verify(emailService, never()).sendPasswordResetEmail(anyString(), anyString());
    }

    @Test
    void forgotPasswordCreatesNewTokenAndMarksPreviousTokensUsed() {
        Utilisateur utilisateur = TestFixtures.utilisateur(1L, "user@example.test", Role.EMPLOYE);
        PasswordResetToken oldToken = new PasswordResetToken();
        oldToken.setUtilisateur(utilisateur);
        oldToken.setUsed(false);

        when(utilisateurRepository.findByEmail("user@example.test")).thenReturn(Optional.of(utilisateur));
        when(tokenRepository.findByUtilisateurIdAndUsedFalse(1L)).thenReturn(List.of(oldToken));
        when(tokenRepository.save(any(PasswordResetToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.forgotPassword("  USER@example.test  ");

        assertThat(oldToken.isUsed()).isTrue();
        assertThat(oldToken.getUsedAt()).isNotNull();
        verify(tokenRepository).save(oldToken);
        ArgumentCaptor<PasswordResetToken> tokenCaptor = ArgumentCaptor.forClass(PasswordResetToken.class);
        verify(tokenRepository, org.mockito.Mockito.times(2)).save(tokenCaptor.capture());
        PasswordResetToken newToken = tokenCaptor.getAllValues().get(1);
        assertThat(newToken.getUtilisateur()).isSameAs(utilisateur);
        assertThat(newToken.getTokenHash()).hasSize(64);
        assertThat(newToken.getExpiresAt()).isAfter(LocalDateTime.now());
        verify(emailService).sendPasswordResetEmail(
                eq("user@example.test"),
                org.mockito.ArgumentMatchers.startsWith("http://localhost:4200/auth/reset-password?token="));
    }

    @Test
    void resetPasswordRejectsMismatchedConfirmation() {
        assertThatThrownBy(() -> service.resetPassword("token", "new-password", "different-password"))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Les mots de passe ne correspondent pas.");

        verify(tokenRepository, never()).findByTokenHash(anyString());
        verify(utilisateurRepository, never()).save(any());
    }

    @Test
    void resetPasswordUpdatesUserPasswordAndConsumesToken() {
        String rawToken = "valid-reset-token";
        Utilisateur utilisateur = TestFixtures.utilisateur(2L, "user2@example.test", Role.RH);
        PasswordResetToken token = new PasswordResetToken();
        token.setUtilisateur(utilisateur);
        token.setTokenHash(sha256(rawToken));
        token.setUsed(false);
        token.setExpiresAt(LocalDateTime.now().plusMinutes(10));

        when(tokenRepository.findByTokenHash(sha256(rawToken))).thenReturn(Optional.of(token));
        when(passwordEncoder.encode("new-password")).thenReturn("encoded-new-password");

        service.resetPassword(rawToken, "new-password", "new-password");

        assertThat(utilisateur.getPassword()).isEqualTo("encoded-new-password");
        assertThat(token.isUsed()).isTrue();
        assertThat(token.getUsedAt()).isNotNull();
        verify(utilisateurRepository).save(utilisateur);
        verify(tokenRepository).save(token);
    }

    private String sha256(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception exception) {
            throw new IllegalStateException(exception);
        }
    }
}
