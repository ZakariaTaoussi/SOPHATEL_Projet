package com.example.backend.controller;

import com.example.backend.dto.common.PageResponse;
import com.example.backend.dto.notification.NotificationResponse;
import com.example.backend.exception.ResourceNotFoundException;
import com.example.backend.model.DemandeConge;
import com.example.backend.model.Utilisateur;
import com.example.backend.model.enums.NotificationType;
import com.example.backend.nats.DemandeNotificationEventPublisher;
import com.example.backend.repository.DemandeCongeRepository;
import com.example.backend.repository.UtilisateurRepository;
import com.example.backend.service.interfaces.INotificationService;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final INotificationService notificationService;
    private final DemandeCongeRepository demandeCongeRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final DemandeNotificationEventPublisher notificationEventPublisher;

    public NotificationController(
            INotificationService notificationService,
            DemandeCongeRepository demandeCongeRepository,
            UtilisateurRepository utilisateurRepository,
            DemandeNotificationEventPublisher notificationEventPublisher) {
        this.notificationService = notificationService;
        this.demandeCongeRepository = demandeCongeRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.notificationEventPublisher = notificationEventPublisher;
    }

    @GetMapping
    public ResponseEntity<PageResponse<NotificationResponse>> getNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(notificationService.getMyNotifications(page, size));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCount() {
        return ResponseEntity.ok(Map.of("count", notificationService.getUnreadCount()));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<NotificationResponse> markAsRead(@PathVariable Long id) {
        return ResponseEntity.ok(notificationService.markAsRead(id));
    }

    @PutMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead() {
        notificationService.markAllAsRead();
        return ResponseEntity.ok().build();
    }

    @PostMapping("/debug/publish/{demandeId}")
    public ResponseEntity<Map<String, Object>> debugPublish(@PathVariable Long demandeId) {
        DemandeConge demande = demandeCongeRepository.findById(demandeId)
                .orElseThrow(() -> new ResourceNotFoundException("Demande introuvable."));
        Long actorUserId = getCurrentUserId();
        notificationEventPublisher.publishAfterCommit(
                demande,
                NotificationType.DEMANDE_SUBMITTED_BY_EMPLOYE,
                actorUserId);
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("published", true);
        response.put("demandeId", demandeId);
        response.put("eventType", NotificationType.DEMANDE_SUBMITTED_BY_EMPLOYE.name());
        response.put("actorUserId", actorUserId);
        return ResponseEntity.ok(response);
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        return utilisateurRepository.findByEmail(authentication.getName())
                .map(Utilisateur::getId)
                .orElse(null);
    }
}
