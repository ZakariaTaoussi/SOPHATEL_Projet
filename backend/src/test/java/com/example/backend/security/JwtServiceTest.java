package com.example.backend.security;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.backend.model.Utilisateur;
import com.example.backend.model.enums.Role;
import com.example.backend.testutil.TestFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class JwtServiceTest {

    private JwtService service;

    @BeforeEach
    void setUp() {
        service = new JwtService();
        ReflectionTestUtils.setField(service, "secret", "dummy-test-secret-key-dummy-test-secret-key");
        ReflectionTestUtils.setField(service, "expiration", 3_600_000L);
    }

    @Test
    void generateTokenUsesEmailAsSubjectAndCanBeValidatedForSameUser() {
        Utilisateur utilisateur = TestFixtures.utilisateur(1L, "employee@example.test", Role.EMPLOYE);

        String token = service.generateToken(utilisateur);

        assertThat(service.extractUsername(token)).isEqualTo("employee@example.test");
        assertThat(service.isTokenValid(token, utilisateur)).isTrue();
        assertThat(service.getExpiration()).isEqualTo(3_600_000L);
    }

    @Test
    void tokenIsInvalidForDifferentUserEmail() {
        Utilisateur utilisateur = TestFixtures.utilisateur(1L, "employee@example.test", Role.EMPLOYE);
        Utilisateur otherUser = TestFixtures.utilisateur(2L, "other@example.test", Role.RH);

        String token = service.generateToken(utilisateur);

        assertThat(service.isTokenValid(token, otherUser)).isFalse();
    }
}
