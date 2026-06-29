package com.example.backend.service.impl;

import com.example.backend.dto.common.PageResponse;
import com.example.backend.dto.notification.NotificationResponse;
import com.example.backend.exception.BadRequestException;
import com.example.backend.exception.ResourceNotFoundException;
import com.example.backend.mapper.NotificationMapper;
import com.example.backend.model.DemandeConge;
import com.example.backend.model.Notification;
import com.example.backend.model.Utilisateur;
import com.example.backend.model.enums.NotificationType;
import com.example.backend.repository.DemandeCongeRepository;
import com.example.backend.repository.NotificationRepository;
import com.example.backend.repository.UtilisateurRepository;
import com.example.backend.service.interfaces.INotificationService;
import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationServiceImpl implements INotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationServiceImpl.class);
    private static final int MAX_PAGE_SIZE = 50;

    private final NotificationRepository notificationRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final DemandeCongeRepository demandeCongeRepository;
    private final NotificationMapper notificationMapper;

    public NotificationServiceImpl(
            NotificationRepository notificationRepository,
            UtilisateurRepository utilisateurRepository,
            DemandeCongeRepository demandeCongeRepository,
            NotificationMapper notificationMapper) {
        this.notificationRepository = notificationRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.demandeCongeRepository = demandeCongeRepository;
        this.notificationMapper = notificationMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<NotificationResponse> getMyNotifications(int page, int size) {
        Pageable pageable = pageable(page, size);
        Long userId = getUtilisateurConnecte().getId();
        return PageResponse.from(notificationRepository
                .findByRecipientIdOrderByCreatedAtDesc(userId, pageable)
                .map(notificationMapper::toResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public long getUnreadCount() {
        return notificationRepository.countByRecipientIdAndReadFalse(getUtilisateurConnecte().getId());
    }

    @Override
    @Transactional
    public NotificationResponse markAsRead(Long notificationId) {
        Long userId = getUtilisateurConnecte().getId();
        Notification notification = notificationRepository.findByIdAndRecipientId(notificationId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification introuvable."));
        if (!notification.isRead()) {
            notification.setRead(true);
            notification.setReadAt(LocalDateTime.now());
        }
        return notificationMapper.toResponse(notificationRepository.save(notification));
    }

    @Override
    @Transactional
    public void markAllAsRead() {
        Long userId = getUtilisateurConnecte().getId();
        LocalDateTime now = LocalDateTime.now();
        notificationRepository.findByRecipientIdAndReadFalse(userId).forEach(notification -> {
            notification.setRead(true);
            notification.setReadAt(now);
            notificationRepository.save(notification);
        });
    }

    @Override
    @Transactional
    public void createNotification(
            Long recipientUserId,
            Long senderUserId,
            Long demandeId,
            NotificationType type,
            String title,
            String message,
            String targetUrl) {
        log.info("[NOTIF-SAVE] creating notification recipientUserId={} demandeId={} type={}",
                recipientUserId,
                demandeId,
                type);
        if (recipientUserId == null) {
            log.warn("Notification ignoree: recipientUserId null pour demandeId={} type={}", demandeId, type);
            return;
        }
        if (type == null || title == null || message == null) {
            throw new BadRequestException("Parametres de notification invalides.");
        }
        Utilisateur recipient = utilisateurRepository.findById(recipientUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur destinataire introuvable."));
        Utilisateur sender = senderUserId == null ? null : utilisateurRepository.findById(senderUserId).orElse(null);
        DemandeConge demande = demandeId == null ? null : demandeCongeRepository.findById(demandeId).orElse(null);
        if (demandeId != null && demande == null) {
            log.warn("Notification creee sans demande liee: demandeId={} introuvable", demandeId);
        }

        Notification notification = new Notification();
        notification.setRecipient(recipient);
        notification.setSender(sender);
        notification.setDemande(demande);
        notification.setType(type);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setTargetUrl(targetUrl);
        notification.setRead(false);
        Notification saved = notificationRepository.save(notification);
        log.info("[NOTIF-SAVE] saved notification id={}", saved.getId());
    }

    private Pageable pageable(int page, int size) {
        if (page < 0 || size < 1 || size > MAX_PAGE_SIZE) {
            throw new BadRequestException("Parametres de pagination invalides.");
        }
        return PageRequest.of(page, size);
    }

    private Utilisateur getUtilisateurConnecte() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResourceNotFoundException("Utilisateur connecte introuvable.");
        }
        return utilisateurRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur connecte introuvable."));
    }
}
