package com.example.backend.testutil.model;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.backend.model.PasswordResetToken;
import com.example.backend.model.Utilisateur;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class PasswordResetTokenTest {

    @Test
    void shouldStorePasswordResetTokenFields() {
        Utilisateur utilisateur = new Utilisateur();
        LocalDateTime createdAt = LocalDateTime.of(2026, 7, 1, 8, 0);
        LocalDateTime expiresAt = LocalDateTime.of(2026, 7, 1, 8, 15);
        LocalDateTime usedAt = LocalDateTime.of(2026, 7, 1, 8, 5);

        PasswordResetToken token = new PasswordResetToken();
        token.setId(1L);
        token.setUtilisateur(utilisateur);
        token.setTokenHash("dummy-hash");
        token.setExpiresAt(expiresAt);
        token.setUsed(true);
        token.setCreatedAt(createdAt);
        token.setUsedAt(usedAt);

        assertThat(token.getId()).isEqualTo(1L);
        assertThat(token.getUtilisateur()).isSameAs(utilisateur);
        assertThat(token.getTokenHash()).isEqualTo("dummy-hash");
        assertThat(token.getExpiresAt()).isEqualTo(expiresAt);
        assertThat(token.isUsed()).isTrue();
        assertThat(token.getCreatedAt()).isEqualTo(createdAt);
        assertThat(token.getUsedAt()).isEqualTo(usedAt);
    }
}
