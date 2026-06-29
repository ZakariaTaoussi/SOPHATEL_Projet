package com.example.backend.nats;

import com.example.backend.model.enums.NotificationType;
import java.time.LocalDateTime;

public class DemandeNotificationEvent {

    private Long demandeId;
    private Long actorUserId;
    private Long employeUserId;
    private Long responsableUserId;
    private Long directeurGeneralUserId;
    private String typeDemande;
    private String status;
    private NotificationType eventType;
    private LocalDateTime occurredAt;
    private String employeNomComplet;
    private Long departementId;

    public Long getDemandeId() {
        return demandeId;
    }

    public void setDemandeId(Long demandeId) {
        this.demandeId = demandeId;
    }

    public Long getActorUserId() {
        return actorUserId;
    }

    public void setActorUserId(Long actorUserId) {
        this.actorUserId = actorUserId;
    }

    public Long getEmployeUserId() {
        return employeUserId;
    }

    public void setEmployeUserId(Long employeUserId) {
        this.employeUserId = employeUserId;
    }

    public Long getResponsableUserId() {
        return responsableUserId;
    }

    public void setResponsableUserId(Long responsableUserId) {
        this.responsableUserId = responsableUserId;
    }

    public Long getDirecteurGeneralUserId() {
        return directeurGeneralUserId;
    }

    public void setDirecteurGeneralUserId(Long directeurGeneralUserId) {
        this.directeurGeneralUserId = directeurGeneralUserId;
    }

    public String getTypeDemande() {
        return typeDemande;
    }

    public void setTypeDemande(String typeDemande) {
        this.typeDemande = typeDemande;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public NotificationType getEventType() {
        return eventType;
    }

    public void setEventType(NotificationType eventType) {
        this.eventType = eventType;
    }

    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }

    public void setOccurredAt(LocalDateTime occurredAt) {
        this.occurredAt = occurredAt;
    }

    public String getEmployeNomComplet() {
        return employeNomComplet;
    }

    public void setEmployeNomComplet(String employeNomComplet) {
        this.employeNomComplet = employeNomComplet;
    }

    public Long getDepartementId() {
        return departementId;
    }

    public void setDepartementId(Long departementId) {
        this.departementId = departementId;
    }
}
