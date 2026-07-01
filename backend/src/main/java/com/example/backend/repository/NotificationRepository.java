package com.example.backend.repository;

import com.example.backend.model.Notification;
import com.example.backend.model.enums.NotificationType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    Page<Notification> findByRecipientIdOrderByCreatedAtDesc(Long recipientId, Pageable pageable);

    List<Notification> findTop5ByRecipientIdOrderByCreatedAtDesc(Long recipientId);

    long countByRecipientIdAndReadFalse(Long recipientId);

    Optional<Notification> findByIdAndRecipientId(Long id, Long recipientId);

    List<Notification> findByRecipientIdAndReadFalse(Long recipientId);

    boolean existsByRecipientIdAndDemandeIdAndTypeAndCreatedAtAfter(
            Long recipientId,
            Long demandeId,
            NotificationType type,
            LocalDateTime createdAt);
}
