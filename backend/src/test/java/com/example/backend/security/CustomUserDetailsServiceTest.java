package com.example.backend.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.example.backend.model.Utilisateur;
import com.example.backend.model.enums.Role;
import com.example.backend.repository.UtilisateurRepository;
import com.example.backend.testutil.TestFixtures;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UtilisateurRepository utilisateurRepository;

    @Test
    void loadUserByUsernameReturnsSpringUserWithRoleAuthority() {
        Utilisateur utilisateur = TestFixtures.utilisateur(1L, "rh@example.test", Role.RH);
        when(utilisateurRepository.findByEmail("rh@example.test")).thenReturn(Optional.of(utilisateur));

        UserDetails userDetails = new CustomUserDetailsService(utilisateurRepository)
                .loadUserByUsername("rh@example.test");

        assertThat(userDetails.getUsername()).isEqualTo("rh@example.test");
        assertThat(userDetails.getPassword()).isEqualTo("encoded-password");
        assertThat(userDetails.getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_RH");
    }

    @Test
    void loadUserByUsernameThrowsWhenUserDoesNotExist() {
        when(utilisateurRepository.findByEmail("missing@example.test")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> new CustomUserDetailsService(utilisateurRepository)
                .loadUserByUsername("missing@example.test"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("Utilisateur introuvable");
    }
}
