package com.example.backend.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.backend.dto.notification.NotificationResponse;
import com.example.backend.model.DemandeConge;
import com.example.backend.model.Notification;
import com.example.backend.model.Utilisateur;
import com.example.backend.model.enums.NotificationType;
import com.example.backend.model.enums.Role;
import com.example.backend.model.enums.StatusDemande;
import com.example.backend.model.enums.TypeDemande;
import com.example.backend.testutil.TestFixtures;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class NotificationMapperTest {

    @Test
    void toResponseMapsAllNotificationFields() {
        Utilisateur recipient = TestFixtures.utilisateur(1L, "employee@example.test", Role.EMPLOYE);
        Utilisateur sender = TestFixtures.utilisateur(2L, "rh@example.test", Role.RH);
        DemandeConge demande = TestFixtures.demande(
                12L,
                TestFixtures.employe(10L, Role.EMPLOYE),
                TypeDemande.CONGE,
                StatusDemande.VALIDE_EMPLOYE,
                LocalDate.of(2026, 7, 6),
                LocalDate.of(2026, 7, 7));
        Notification notification = new Notification();
        ReflectionTestUtils.setField(notification, "id", 7L);
        notification.setRecipient(recipient);
        notification.setSender(sender);
        notification.setDemande(demande);
        notification.setType(NotificationType.DEMANDE_SUBMITTED_BY_EMPLOYE);
        notification.setTitle("Titre");
        notification.setMessage("Message");
        notification.setTargetUrl("/target");
        notification.setRead(true);

        NotificationResponse response = new NotificationMapper().toResponse(notification);

        assertThat(response.id()).isEqualTo(7L);
        assertThat(response.recipientId()).isEqualTo(1L);
        assertThat(response.senderId()).isEqualTo(2L);
        assertThat(response.demandeId()).isEqualTo(12L);
        assertThat(response.type()).isEqualTo("DEMANDE_SUBMITTED_BY_EMPLOYE");
        assertThat(response.title()).isEqualTo("Titre");
        assertThat(response.read()).isTrue();
        assertThat(response.targetUrl()).isEqualTo("/target");
    }

    @Test
    void toResponseReturnsNullForNullNotification() {
        assertThat(new NotificationMapper().toResponse(null)).isNull();
    }
}
