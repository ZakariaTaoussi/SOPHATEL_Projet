package com.example.backend.nats;

import com.example.backend.model.DemandeConge;
import com.example.backend.model.Employe;
import com.example.backend.model.enums.NotificationType;
import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Component
public class DemandeNotificationEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(DemandeNotificationEventPublisher.class);

    private final NatsPublisher natsPublisher;

    public DemandeNotificationEventPublisher(NatsPublisher natsPublisher) {
        this.natsPublisher = natsPublisher;
    }

    public void publishAfterCommit(DemandeConge demande, NotificationType eventType, Long actorUserId) {
        DemandeNotificationEvent event = toEvent(demande, eventType, actorUserId);
        log.info(
                "[NOTIF] About to publish event={} demandeId={} actorUserId={}",
                event.getEventType(),
                event.getDemandeId(),
                event.getActorUserId());

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    natsPublisher.publishDemandeEvent(event);
                }
            });
            return;
        }

        log.warn("[NOTIF] No active transaction, publishing directly");
        natsPublisher.publishDemandeEvent(event);
    }

    private DemandeNotificationEvent toEvent(DemandeConge demande, NotificationType eventType, Long actorUserId) {
        DemandeNotificationEvent event = new DemandeNotificationEvent();
        if (demande == null) {
            event.setEventType(eventType);
            event.setActorUserId(actorUserId);
            event.setOccurredAt(LocalDateTime.now());
            return event;
        }
        Employe employe = demande.getEmploye();
        event.setDemandeId(demande.getId());
        event.setActorUserId(actorUserId);
        event.setEmployeUserId(employe == null || employe.getUtilisateur() == null
                ? null
                : employe.getUtilisateur().getId());
        event.setTypeDemande(demande.getTypeDemande() == null ? null : demande.getTypeDemande().name());
        event.setStatus(demande.getStatus() == null ? null : demande.getStatus().name());
        event.setEventType(eventType);
        event.setOccurredAt(LocalDateTime.now());
        event.setEmployeNomComplet(employe == null ? null : (employe.getPrenom() + " " + employe.getNom()).trim());
        event.setDepartementId(employe == null || employe.getDepartement() == null
                ? null
                : employe.getDepartement().getId());
        return event;
    }
}
