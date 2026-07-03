package com.example.backend.testutil.model;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.backend.model.DemandeConge;
import com.example.backend.model.Notification;
import com.example.backend.model.Utilisateur;
import com.example.backend.model.enums.NotificationType;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class NotificationTest {

    @Test
    void shouldStoreNotificationFields() {
        Utilisateur recipient = new Utilisateur();
        Utilisateur sender = new Utilisateur();
        DemandeConge demande = new DemandeConge();
        LocalDateTime readAt = LocalDateTime.of(2026, 7, 1, 10, 0);

        Notification notification = new Notification();
        notification.setRecipient(recipient);
        notification.setSender(sender);
        notification.setDemande(demande);
        notification.setType(NotificationType.DEMANDE_VALIDATED_BY_DG);
        notification.setTitle("Titre");
        notification.setMessage("Message");
        notification.setRead(true);
        notification.setReadAt(readAt);
        notification.setTargetUrl("/demandes/20");

        assertThat(notification.getRecipient()).isSameAs(recipient);
        assertThat(notification.getSender()).isSameAs(sender);
        assertThat(notification.getDemande()).isSameAs(demande);
        assertThat(notification.getType()).isEqualTo(NotificationType.DEMANDE_VALIDATED_BY_DG);
        assertThat(notification.getTitle()).isEqualTo("Titre");
        assertThat(notification.getMessage()).isEqualTo("Message");
        assertThat(notification.isRead()).isTrue();
        assertThat(notification.getReadAt()).isEqualTo(readAt);
        assertThat(notification.getTargetUrl()).isEqualTo("/demandes/20");
    }

    @Test
    void prePersistShouldSetCreatedAtAndForceUnread() {
        Notification notification = new Notification();
        notification.setRead(true);

        ReflectionTestUtils.invokeMethod(notification, "prePersist");

        assertThat(notification.getCreatedAt()).isNotNull();
        assertThat(notification.isRead()).isFalse();
    }
}
