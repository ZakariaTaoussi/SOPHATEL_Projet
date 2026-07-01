package com.example.backend.service.impl;

import com.example.backend.exception.BadRequestException;
import com.example.backend.model.PasswordResetToken;
import com.example.backend.model.Utilisateur;
import com.example.backend.repository.PasswordResetTokenRepository;
import com.example.backend.repository.UtilisateurRepository;
import com.example.backend.service.interfaces.IEmailService;
import com.example.backend.service.interfaces.IPasswordResetService;
import jakarta.transaction.Transactional;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;
import org.springframework.beans.factory.annotation.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class PasswordResetServiceImpl implements IPasswordResetService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PasswordResetServiceImpl.class);
    private static final String INVALID_LINK_MESSAGE = "Lien de reinitialisation invalide ou expire.";
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final UtilisateurRepository utilisateurRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final IEmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final Environment environment;
    private final String frontendUrl;
    private final long expirationMinutes;

    public PasswordResetServiceImpl(
            UtilisateurRepository utilisateurRepository,
            PasswordResetTokenRepository tokenRepository,
            IEmailService emailService,
            PasswordEncoder passwordEncoder,
            Environment environment,
            @Value("${app.frontend-url}") String frontendUrl,
            @Value("${app.reset-password-token-expiration-minutes:30}") long expirationMinutes) {
        this.utilisateurRepository = utilisateurRepository;
        this.tokenRepository = tokenRepository;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
        this.environment = environment;
        this.frontendUrl = frontendUrl;
        this.expirationMinutes = expirationMinutes;
    }

    @Override
    @Transactional
    public void forgotPassword(String email) {
        String normalizedEmail = normalizeEmail(email);
        if (normalizedEmail == null || !normalizedEmail.contains("@")) {
            return;
        }

        utilisateurRepository.findByEmail(normalizedEmail).ifPresent(utilisateur -> {
            LocalDateTime now = LocalDateTime.now();
            markOldTokensAsUsed(utilisateur.getId(), now);

            String rawToken = generateToken();
            PasswordResetToken resetToken = new PasswordResetToken();
            resetToken.setUtilisateur(utilisateur);
            resetToken.setTokenHash(hashToken(rawToken));
            resetToken.setCreatedAt(now);
            resetToken.setExpiresAt(now.plusMinutes(expirationMinutes));
            resetToken.setUsed(false);
            PasswordResetToken savedToken = tokenRepository.save(resetToken);

            String resetLink = frontendUrl + "/auth/reset-password?token=" + rawToken;
            LOGGER.info(
                    "[RESET-PASSWORD] token created id={} used={} expiresAt={}",
                    savedToken.getId(),
                    savedToken.isUsed(),
                    savedToken.getExpiresAt());
            LOGGER.info("[RESET-PASSWORD] sending email to={}", utilisateur.getEmail());
            if (!isProductionProfile()) {
                LOGGER.info("[RESET-PASSWORD-DEV] reset link={}", resetLink);
            }
            emailService.sendPasswordResetEmail(utilisateur.getEmail(), resetLink);
        });
    }

    @Override
    @Transactional
    public void resetPassword(String token, String newPassword, String confirmPassword) {
        if (isBlank(token)) {
            throw new BadRequestException(INVALID_LINK_MESSAGE);
        }
        if (isBlank(newPassword) || newPassword.length() < 8) {
            throw new BadRequestException("Mot de passe invalide.");
        }
        if (confirmPassword == null || !newPassword.equals(confirmPassword)) {
            throw new BadRequestException("Les mots de passe ne correspondent pas.");
        }

        PasswordResetToken resetToken = tokenRepository.findByTokenHash(hashToken(token))
                .orElseThrow(() -> new BadRequestException(INVALID_LINK_MESSAGE));

        LocalDateTime now = LocalDateTime.now();
        if (resetToken.isUsed() || !resetToken.getExpiresAt().isAfter(now)) {
            throw new BadRequestException(INVALID_LINK_MESSAGE);
        }

        Utilisateur utilisateur = resetToken.getUtilisateur();
        utilisateur.setPassword(passwordEncoder.encode(newPassword));
        utilisateurRepository.save(utilisateur);

        resetToken.setUsed(true);
        resetToken.setUsedAt(now);
        tokenRepository.save(resetToken);
    }

    private void markOldTokensAsUsed(Long utilisateurId, LocalDateTime now) {
        tokenRepository.findByUtilisateurIdAndUsedFalse(utilisateurId).forEach(token -> {
            token.setUsed(true);
            token.setUsedAt(now);
            tokenRepository.save(token);
        });
    }

    private String normalizeEmail(String email) {
        if (email == null) {
            return null;
        }
        return email.trim().toLowerCase();
    }

    private String generateToken() {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 indisponible", exception);
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private boolean isProductionProfile() {
        return environment.acceptsProfiles(Profiles.of("prod", "production"));
    }
}
