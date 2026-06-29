package com.example.backend.nats;

import com.example.backend.model.DemandeConge;
import com.example.backend.model.Departement;
import com.example.backend.model.Employe;
import com.example.backend.model.Utilisateur;
import com.example.backend.model.enums.NotificationType;
import com.example.backend.model.enums.Role;
import com.example.backend.model.enums.TypeDemande;
import com.example.backend.repository.DemandeCongeRepository;
import com.example.backend.repository.EmployeRepository;
import com.example.backend.repository.UtilisateurRepository;
import com.example.backend.service.interfaces.INotificationService;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class NotificationEventHandler {

    private static final Logger log = LoggerFactory.getLogger(NotificationEventHandler.class);

    private final DemandeCongeRepository demandeCongeRepository;
    private final EmployeRepository employeRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final INotificationService notificationService;

    public NotificationEventHandler(
            DemandeCongeRepository demandeCongeRepository,
            EmployeRepository employeRepository,
            UtilisateurRepository utilisateurRepository,
            INotificationService notificationService) {
        this.demandeCongeRepository = demandeCongeRepository;
        this.employeRepository = employeRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.notificationService = notificationService;
    }

    @Transactional
    public void handle(DemandeNotificationEvent event) {
        if (event == null || event.getDemandeId() == null || event.getEventType() == null) {
            log.warn("Evenement notification ignore: donnees incompletes.");
            return;
        }
        log.info("[NOTIF-HANDLER] handling event={} demandeId={}", event.getEventType(), event.getDemandeId());

        DemandeConge demande = demandeCongeRepository.findById(event.getDemandeId()).orElse(null);
        if (demande == null) {
            log.warn("Evenement notification ignore: demande {} introuvable.", event.getDemandeId());
            return;
        }
        log.info("[NOTIF-HANDLER] demande found id={} status={} typeDemande={}",
                demande.getId(),
                demande.getStatus(),
                demande.getTypeDemande());
        Employe employe = demande.getEmploye();
        Utilisateur employeUser = user(employe);
        Long departementId = employe == null || employe.getDepartement() == null
                ? null
                : employe.getDepartement().getId();
        log.info("[NOTIF-HANDLER] employeUserId={}", employeUser == null ? null : employeUser.getId());
        log.info("[NOTIF-HANDLER] departementId={}", departementId);

        Set<Long> createdFor = switch (event.getEventType()) {
            case DEMANDE_SUBMITTED_BY_EMPLOYE -> notifyResponsable(event, demande,
                    "Nouvelle demande a valider",
                    nomEmploye(demande) + " a cree une nouvelle demande de " + typeLabel(demande) + ".",
                    "/responsable/demandes-a-valider");
            case DEMANDE_MODIFIED_BY_EMPLOYE -> notifyResponsable(event, demande,
                    "Demande modifiee",
                    nomEmploye(demande) + " a modifie sa demande de " + typeLabel(demande) + ".",
                    "/responsable/demandes-a-valider");
            case DEMANDE_CANCELLED_BY_EMPLOYE -> notifyResponsable(event, demande,
                    "Demande annulee",
                    nomEmploye(demande) + " a annule sa demande de " + typeLabel(demande) + ".",
                    "/responsable/demandes-a-valider");
            case DEMANDE_VALIDATED_BY_RESPONSABLE -> notifyResponsableValidation(event, demande);
            case DEMANDE_MODIFIED_BY_RESPONSABLE -> notifyResponsableModification(event, demande);
            case DEMANDE_REFUSED_BY_RESPONSABLE -> notifyEmploye(event, demande,
                    "Demande refusee par le responsable",
                    "Votre demande de " + typeLabel(demande) + " a ete refusee par le responsable.",
                    targetSelf(demande));
            case DEMANDE_VALIDATED_BY_DG -> notifyDgValidation(event, demande);
            case DEMANDE_REFUSED_BY_DG -> notifyDgRefus(event, demande);
            case DEMANDE_VALIDATED_DG_FOR_RH -> notifyRh(event, demande);
        };
        log.info("[NOTIF-HANDLER] notifications to create count={} demandeId={}", createdFor.size(), demande.getId());
    }

    private Set<Long> notifyResponsable(DemandeNotificationEvent event, DemandeConge demande, String title, String message, String targetUrl) {
        Set<Long> recipients = new LinkedHashSet<>();
        add(recipients, event, demande, findResponsable(demande), event.getEventType(), title, message, targetUrl);
        return recipients;
    }

    private Set<Long> notifyEmploye(DemandeNotificationEvent event, DemandeConge demande, String title, String message, String targetUrl) {
        Set<Long> recipients = new LinkedHashSet<>();
        add(recipients, event, demande, user(demande.getEmploye()), event.getEventType(), title, message, targetUrl);
        return recipients;
    }

    private Set<Long> notifyResponsableValidation(DemandeNotificationEvent event, DemandeConge demande) {
        Set<Long> recipients = new LinkedHashSet<>();
        if (!isActor(demande.getEmploye(), event)) {
            add(recipients, event, demande, user(demande.getEmploye()), event.getEventType(),
                    "Demande validee par le responsable",
                    "Votre demande de " + typeLabel(demande) + " a ete validee par le responsable.",
                    targetSelf(demande));
        }
        add(recipients, event, demande, findDg(), event.getEventType(),
                "Nouvelle demande a valider",
                "Une demande de " + typeLabel(demande) + " de " + nomEmploye(demande) + " attend votre validation.",
                "/directeur-general/demandes-a-valider");
        return recipients;
    }

    private Set<Long> notifyResponsableModification(DemandeNotificationEvent event, DemandeConge demande) {
        Set<Long> recipients = new LinkedHashSet<>();
        add(recipients, event, demande, findDg(), event.getEventType(),
                "Demande modifiee par le responsable",
                "Le responsable a modifie la demande de " + nomEmploye(demande) + ".",
                "/directeur-general/demandes-a-valider");
        add(recipients, event, demande, user(demande.getEmploye()), event.getEventType(),
                "Demande modifiee par le responsable",
                "Votre demande de " + typeLabel(demande) + " a ete modifiee par le responsable.",
                targetSelf(demande));
        return recipients;
    }

    private Set<Long> notifyDgValidation(DemandeNotificationEvent event, DemandeConge demande) {
        Set<Long> recipients = new LinkedHashSet<>();
        if (!isActor(demande.getEmploye(), event)) {
            add(recipients, event, demande, user(demande.getEmploye()), event.getEventType(),
                    "Demande validee definitivement",
                    "Votre demande de " + typeLabel(demande) + " a ete validee par le Directeur General.",
                    targetSelf(demande));
        }
        Utilisateur responsable = findResponsable(demande);
        if (responsable == null || !responsable.getId().equals(event.getActorUserId())) {
            add(recipients, event, demande, responsable, event.getEventType(),
                    "Demande validee par le Directeur General",
                    "La demande de " + nomEmploye(demande) + " a ete validee par le Directeur General.",
                    "/responsable/historique");
        }
        findRh().forEach(rh -> add(recipients, event, demande, rh,
                NotificationType.DEMANDE_VALIDATED_DG_FOR_RH,
                "Nouvelle demande validee DG",
                "Une demande de " + typeLabel(demande) + " de " + nomEmploye(demande) + " a ete validee par le Directeur General.",
                targetRhValidated(demande)));
        return recipients;
    }

    private Set<Long> notifyDgRefus(DemandeNotificationEvent event, DemandeConge demande) {
        Set<Long> recipients = new LinkedHashSet<>();
        add(recipients, event, demande, user(demande.getEmploye()), event.getEventType(),
                "Demande refusee par le Directeur General",
                "La demande de " + typeLabel(demande) + " de " + nomEmploye(demande) + " a ete refusee par le Directeur General.",
                targetSelf(demande));
        add(recipients, event, demande, findResponsable(demande), event.getEventType(),
                "Demande refusee par le Directeur General",
                "La demande de " + typeLabel(demande) + " de " + nomEmploye(demande) + " a ete refusee par le Directeur General.",
                "/responsable/historique");
        return recipients;
    }

    private Set<Long> notifyRh(DemandeNotificationEvent event, DemandeConge demande) {
        Set<Long> recipients = new LinkedHashSet<>();
        findRh().forEach(rh -> add(recipients, event, demande, rh,
                NotificationType.DEMANDE_VALIDATED_DG_FOR_RH,
                "Nouvelle demande validee DG",
                "Une demande de " + typeLabel(demande) + " de " + nomEmploye(demande) + " a ete validee par le Directeur General.",
                targetRhValidated(demande)));
        return recipients;
    }

    private void add(
            Set<Long> recipients,
            DemandeNotificationEvent event,
            DemandeConge demande,
            Utilisateur recipient,
            NotificationType type,
            String title,
            String message,
            String targetUrl) {
        if (recipient == null || recipient.getId() == null) {
            log.warn("Notification ignoree: destinataire introuvable pour demandeId={} type={}", demande.getId(), type);
            return;
        }
        if (!recipients.add(recipient.getId())) {
            log.info("Notification dedupliquee pour recipientUserId={} demandeId={} type={}", recipient.getId(), demande.getId(), type);
            return;
        }
        notificationService.createNotification(
                recipient.getId(),
                event.getActorUserId(),
                demande.getId(),
                type,
                title,
                message,
                targetUrl);
    }

    private Utilisateur findResponsable(DemandeConge demande) {
        Departement departement = demande.getEmploye() == null ? null : demande.getEmploye().getDepartement();
        Long departementId = departement == null ? null : departement.getId();
        if (departementId == null) {
            log.warn("Aucun departement trouve pour la demande {}", demande.getId());
            return null;
        }
        return employeRepository.findResponsableByDepartementId(departementId, Role.RESPONSABLE)
                .map(Employe::getUtilisateur)
                .map(utilisateur -> {
                    log.info("[NOTIF-HANDLER] responsable found userId={}", utilisateur.getId());
                    return utilisateur;
                })
                .orElseGet(() -> {
                    log.warn("[NOTIF-HANDLER] aucun responsable trouve pour departementId={}", departementId);
                    return null;
                });
    }

    private Utilisateur findDg() {
        return utilisateurRepository.findFirstByRole(Role.DIRECTEUR_GENERAL)
                .map(utilisateur -> {
                    log.info("[NOTIF-HANDLER] directeur general found userId={}", utilisateur.getId());
                    return utilisateur;
                })
                .orElseGet(() -> {
                    log.warn("Aucun Directeur General trouve pour notification.");
                    return null;
                });
    }

    private List<Utilisateur> findRh() {
        List<Utilisateur> rhUsers = utilisateurRepository.findAllByRole(Role.RH);
        log.info("[NOTIF-HANDLER] RH count={}", rhUsers.size());
        if (rhUsers.isEmpty()) {
            log.warn("Aucun RH trouve pour notification.");
        }
        return rhUsers;
    }

    private Utilisateur user(Employe employe) {
        return employe == null ? null : employe.getUtilisateur();
    }

    private boolean isActor(Employe employe, DemandeNotificationEvent event) {
        Utilisateur utilisateur = user(employe);
        return utilisateur != null && utilisateur.getId() != null && utilisateur.getId().equals(event.getActorUserId());
    }

    private String nomEmploye(DemandeConge demande) {
        Employe employe = demande.getEmploye();
        if (employe == null) {
            return "Un employe";
        }
        return (employe.getPrenom() + " " + employe.getNom()).trim();
    }

    private String typeLabel(DemandeConge demande) {
        return demande.getTypeDemande() == TypeDemande.ABSENCE ? "ABSENCE" : "CONGE";
    }

    private String targetSelf(DemandeConge demande) {
        Role role = user(demande.getEmploye()) == null ? Role.EMPLOYE : user(demande.getEmploye()).getRole();
        String suffix = demande.getTypeDemande() == TypeDemande.ABSENCE ? "mes-absences" : "mes-demandes";
        return switch (role) {
            case RH -> "/rh/" + suffix;
            case RESPONSABLE -> "/responsable/" + suffix;
            case DIRECTEUR_GENERAL -> "/directeur-general/" + suffix;
            default -> "/employe/" + suffix;
        };
    }

    private String targetRhValidated(DemandeConge demande) {
        return demande.getTypeDemande() == TypeDemande.ABSENCE
                ? "/rh/employes/absences-valides"
                : "/rh/employes/conges-valides";
    }
}
