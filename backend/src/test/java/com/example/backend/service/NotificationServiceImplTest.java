package com.example.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.backend.dto.common.PageResponse;
import com.example.backend.dto.notification.NotificationResponse;
import com.example.backend.exception.BadRequestException;
import com.example.backend.exception.ResourceNotFoundException;
import com.example.backend.mapper.NotificationMapper;
import com.example.backend.model.DemandeConge;
import com.example.backend.model.Notification;
import com.example.backend.model.Utilisateur;
import com.example.backend.model.enums.NotificationType;
import com.example.backend.model.enums.Role;
import com.example.backend.model.enums.StatusDemande;
import com.example.backend.model.enums.TypeDemande;
import com.example.backend.repository.DemandeCongeRepository;
import com.example.backend.repository.NotificationRepository;
import com.example.backend.repository.UtilisateurRepository;
import com.example.backend.service.impl.NotificationServiceImpl;
import com.example.backend.testutil.TestFixtures;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private UtilisateurRepository utilisateurRepository;

    @Mock
    private DemandeCongeRepository demandeCongeRepository;

    private final NotificationMapper notificationMapper = new NotificationMapper();

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getMyNotificationsReturnsMappedPageForConnectedUser() {
        Utilisateur recipient = TestFixtures.utilisateur(1L, "employee@example.test", Role.EMPLOYE);
        Notification notification = notification(recipient, NotificationType.DEMANDE_SUBMITTED_BY_EMPLOYE);
        ReflectionTestUtils.setField(notification, "id", 50L);

        authenticateAs("employee@example.test");
        when(utilisateurRepository.findByEmail("employee@example.test")).thenReturn(Optional.of(recipient));
        when(notificationRepository.findByRecipientIdOrderByCreatedAtDesc(1L, PageRequest.of(0, 10)))
                .thenReturn(new PageImpl<>(List.of(notification), PageRequest.of(0, 10), 1));

        PageResponse<NotificationResponse> response = service().getMyNotifications(0, 10);

        assertThat(response.content()).hasSize(1);
        assertThat(response.content().get(0).id()).isEqualTo(50L);
        assertThat(response.content().get(0).recipientId()).isEqualTo(1L);
        assertThat(response.totalElements()).isEqualTo(1);
    }

    @Test
    void getMyNotificationsRejectsInvalidPagination() {
        assertThatThrownBy(() -> service().getMyNotifications(0, 99))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Parametres de pagination invalides.");
    }

    @Test
    void getUnreadCountUsesConnectedUser() {
        Utilisateur recipient = TestFixtures.utilisateur(1L, "employee@example.test", Role.EMPLOYE);
        authenticateAs("employee@example.test");
        when(utilisateurRepository.findByEmail("employee@example.test")).thenReturn(Optional.of(recipient));
        when(notificationRepository.countByRecipientIdAndReadFalse(1L)).thenReturn(3L);

        assertThat(service().getUnreadCount()).isEqualTo(3L);
    }

    @Test
    void markAsReadSetsReadFlagAndReadDate() {
        Utilisateur recipient = TestFixtures.utilisateur(1L, "employee@example.test", Role.EMPLOYE);
        Notification notification = notification(recipient, NotificationType.DEMANDE_VALIDATED_BY_DG);
        ReflectionTestUtils.setField(notification, "id", 7L);

        authenticateAs("employee@example.test");
        when(utilisateurRepository.findByEmail("employee@example.test")).thenReturn(Optional.of(recipient));
        when(notificationRepository.findByIdAndRecipientId(7L, 1L)).thenReturn(Optional.of(notification));
        when(notificationRepository.save(notification)).thenReturn(notification);

        NotificationResponse response = service().markAsRead(7L);

        assertThat(response.read()).isTrue();
        assertThat(notification.getReadAt()).isNotNull();
        verify(notificationRepository).save(notification);
    }

    @Test
    void markAsReadRejectsNotificationFromAnotherUser() {
        Utilisateur recipient = TestFixtures.utilisateur(1L, "employee@example.test", Role.EMPLOYE);
        authenticateAs("employee@example.test");
        when(utilisateurRepository.findByEmail("employee@example.test")).thenReturn(Optional.of(recipient));
        when(notificationRepository.findByIdAndRecipientId(99L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service().markAsRead(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Notification introuvable.");
    }

    @Test
    void markAllAsReadUpdatesEveryUnreadNotification() {
        Utilisateur recipient = TestFixtures.utilisateur(1L, "employee@example.test", Role.EMPLOYE);
        Notification first = notification(recipient, NotificationType.DEMANDE_SUBMITTED_BY_EMPLOYE);
        Notification second = notification(recipient, NotificationType.DEMANDE_VALIDATED_BY_DG);

        authenticateAs("employee@example.test");
        when(utilisateurRepository.findByEmail("employee@example.test")).thenReturn(Optional.of(recipient));
        when(notificationRepository.findByRecipientIdAndReadFalse(1L)).thenReturn(List.of(first, second));

        service().markAllAsRead();

        assertThat(first.isRead()).isTrue();
        assertThat(second.isRead()).isTrue();
        assertThat(first.getReadAt()).isNotNull();
        assertThat(second.getReadAt()).isNotNull();
        verify(notificationRepository).save(first);
        verify(notificationRepository).save(second);
    }

    @Test
    void createNotificationPersistsValidNotificationWithSenderAndDemande() {
        Utilisateur recipient = TestFixtures.utilisateur(1L, "employee@example.test", Role.EMPLOYE);
        Utilisateur sender = TestFixtures.utilisateur(2L, "rh@example.test", Role.RH);
        DemandeConge demande = TestFixtures.demande(
                12L,
                TestFixtures.employe(10L, Role.EMPLOYE),
                TypeDemande.CONGE,
                StatusDemande.VALIDE_EMPLOYE,
                LocalDate.of(2026, 7, 6),
                LocalDate.of(2026, 7, 7));
        when(utilisateurRepository.findById(1L)).thenReturn(Optional.of(recipient));
        when(utilisateurRepository.findById(2L)).thenReturn(Optional.of(sender));
        when(demandeCongeRepository.findById(12L)).thenReturn(Optional.of(demande));
        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service().createNotification(
                1L,
                2L,
                12L,
                NotificationType.DEMANDE_SUBMITTED_BY_EMPLOYE,
                "Titre",
                "Message",
                "/target");

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());
        Notification saved = captor.getValue();
        assertThat(saved.getRecipient()).isSameAs(recipient);
        assertThat(saved.getSender()).isSameAs(sender);
        assertThat(saved.getDemande()).isSameAs(demande);
        assertThat(saved.getType()).isEqualTo(NotificationType.DEMANDE_SUBMITTED_BY_EMPLOYE);
        assertThat(saved.isRead()).isFalse();
        assertThat(saved.getTargetUrl()).isEqualTo("/target");
    }

    @Test
    void createNotificationIgnoresNullRecipient() {
        service().createNotification(
                null,
                2L,
                12L,
                NotificationType.DEMANDE_SUBMITTED_BY_EMPLOYE,
                "Titre",
                "Message",
                "/target");

        verify(notificationRepository, never()).save(any());
    }

    @Test
    void createNotificationRejectsMissingTypeTitleOrMessage() {
        assertThatThrownBy(() -> service().createNotification(1L, null, null, null, "Titre", "Message", "/target"))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Parametres de notification invalides.");
    }

    private NotificationServiceImpl service() {
        return new NotificationServiceImpl(
                notificationRepository,
                utilisateurRepository,
                demandeCongeRepository,
                notificationMapper);
    }

    private Notification notification(Utilisateur recipient, NotificationType type) {
        Notification notification = new Notification();
        notification.setRecipient(recipient);
        notification.setType(type);
        notification.setTitle("Titre");
        notification.setMessage("Message");
        notification.setTargetUrl("/target");
        notification.setRead(false);
        return notification;
    }

    private void authenticateAs(String email) {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(email, null, List.of()));
    }
}
