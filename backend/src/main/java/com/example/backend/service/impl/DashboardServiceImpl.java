package com.example.backend.service.impl;

import com.example.backend.dto.dashboard.AdminDashboardResponse;
import com.example.backend.dto.dashboard.DashboardChartItemDto;
import com.example.backend.dto.dashboard.DashboardNotificationDto;
import com.example.backend.dto.dashboard.DashboardRecentDemandeDto;
import com.example.backend.dto.dashboard.DashboardRecentEmployeDto;
import com.example.backend.dto.dashboard.DirecteurGeneralDashboardResponse;
import com.example.backend.dto.dashboard.EmployeDashboardResponse;
import com.example.backend.dto.dashboard.ResponsableDashboardResponse;
import com.example.backend.dto.dashboard.RhDashboardResponse;
import com.example.backend.exception.ForbiddenException;
import com.example.backend.exception.InvalidBusinessRequestException;
import com.example.backend.model.DemandeConge;
import com.example.backend.model.Employe;
import com.example.backend.model.Notification;
import com.example.backend.model.SoldeConge;
import com.example.backend.model.Utilisateur;
import com.example.backend.model.enums.Role;
import com.example.backend.model.enums.StatusDemande;
import com.example.backend.model.enums.TypeDemande;
import com.example.backend.repository.AgendaRepository;
import com.example.backend.repository.DemandeCongeRepository;
import com.example.backend.repository.DepartementRepository;
import com.example.backend.repository.EmployeRepository;
import com.example.backend.repository.JourFerieRepository;
import com.example.backend.repository.NotificationRepository;
import com.example.backend.repository.UtilisateurRepository;
import com.example.backend.service.interfaces.IDashboardService;
import com.example.backend.service.interfaces.ISoldeCongeService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DashboardServiceImpl implements IDashboardService {

    private static final int RECENT_LIMIT = 5;
    private static final Set<StatusDemande> REFUSED_STATUSES = Set.of(
            StatusDemande.REFUSE_RESPONSABLE,
            StatusDemande.REFUSE_DG);
    private static final Set<StatusDemande> RESPONSABLE_PROCESSED_STATUSES = Set.of(
            StatusDemande.VALIDE_RESPONSABLE,
            StatusDemande.REFUSE_RESPONSABLE,
            StatusDemande.MODIFICATION_RESPONSABLE);
    private static final Set<StatusDemande> DG_PROCESSED_STATUSES = Set.of(
            StatusDemande.VALIDE_DG,
            StatusDemande.REFUSE_DG,
            StatusDemande.MODIFICATION_DG);

    private final UtilisateurRepository utilisateurRepository;
    private final EmployeRepository employeRepository;
    private final DemandeCongeRepository demandeCongeRepository;
    private final NotificationRepository notificationRepository;
    private final DepartementRepository departementRepository;
    private final JourFerieRepository jourFerieRepository;
    private final AgendaRepository agendaRepository;
    private final ISoldeCongeService soldeCongeService;

    public DashboardServiceImpl(
            UtilisateurRepository utilisateurRepository,
            EmployeRepository employeRepository,
            DemandeCongeRepository demandeCongeRepository,
            NotificationRepository notificationRepository,
            DepartementRepository departementRepository,
            JourFerieRepository jourFerieRepository,
            AgendaRepository agendaRepository,
            ISoldeCongeService soldeCongeService) {
        this.utilisateurRepository = utilisateurRepository;
        this.employeRepository = employeRepository;
        this.demandeCongeRepository = demandeCongeRepository;
        this.notificationRepository = notificationRepository;
        this.departementRepository = departementRepository;
        this.jourFerieRepository = jourFerieRepository;
        this.agendaRepository = agendaRepository;
        this.soldeCongeService = soldeCongeService;
    }

    @Override
    @Transactional(readOnly = true)
    public AdminDashboardResponse getAdminDashboard() {
        requireRole(Role.ADMINISTRATEUR);
        int currentYear = LocalDate.now().getYear();

        return new AdminDashboardResponse(
                employeRepository.count(),
                utilisateurRepository.countByRole(Role.EMPLOYE),
                utilisateurRepository.countByRole(Role.RESPONSABLE),
                utilisateurRepository.countByRole(Role.RH),
                utilisateurRepository.countByRole(Role.DIRECTEUR_GENERAL),
                utilisateurRepository.countByRole(Role.ADMINISTRATEUR),
                departementRepository.count(),
                jourFerieRepository.countByAgendaAnnee(currentYear),
                agendaRepository.count(),
                agendaRepository.existsByAnnee(currentYear),
                employeRepository.findTop5ByOrderByCreatedAtDesc().stream().map(this::toRecentEmploye).toList(),
                employeRepository.countEmployeesByRole(),
                employeRepository.countEmployeesByDepartment());
    }

    @Override
    @Transactional
    public EmployeDashboardResponse getEmployeDashboard() {
        Utilisateur utilisateur = requireRole(Role.EMPLOYE);
        Employe employe = requireEmploye(utilisateur);
        SoldeConge solde = soldeCongeService.getOrCreateSolde(employe.getIdEmp(), LocalDate.now().getYear());
        DateWindow year = yearWindow(LocalDate.now().getYear());
        DateWindow month = monthWindow(LocalDate.now().getYear(), LocalDate.now().getMonthValue());

        return new EmployeDashboardResponse(
                nvl(solde.getSoldeActuel()),
                nvl(solde.getSoldeTotal()),
                solde.getAnnee(),
                demandeCongeRepository.countByEmployeIdAndTypeDemande(employe.getIdEmp(), TypeDemande.CONGE),
                demandeCongeRepository.countByEmployeIdAndTypeDemande(employe.getIdEmp(), TypeDemande.ABSENCE),
                demandeCongeRepository.countByEmployeIdAndStatus(employe.getIdEmp(), StatusDemande.BROUILLON),
                demandeCongeRepository.countByEmployeIdAndStatus(employe.getIdEmp(), StatusDemande.VALIDE_EMPLOYE),
                demandeCongeRepository.countByEmployeIdAndStatus(employe.getIdEmp(), StatusDemande.VALIDE_DG),
                demandeCongeRepository.countByEmployeIdAndStatusIn(employe.getIdEmp(), REFUSED_STATUSES),
                demandeCongeRepository.countByEmployeIdAndTypeDemandeAndDateDebutEmpBetweenExclusive(
                        employe.getIdEmp(), TypeDemande.ABSENCE, year.start(), year.end()),
                demandeCongeRepository.countByEmployeIdAndTypeDemandeAndDateDebutEmpBetweenExclusive(
                        employe.getIdEmp(), TypeDemande.ABSENCE, month.start(), month.end()),
                demandeCongeRepository.findRecentByEmployeId(employe.getIdEmp(), PageRequest.of(0, RECENT_LIMIT))
                        .stream().map(this::toRecentDemande).toList(),
                latestNotifications(utilisateur.getId()),
                notificationRepository.countByRecipientIdAndReadFalse(utilisateur.getId()));
    }

    @Override
    @Transactional
    public ResponsableDashboardResponse getResponsableDashboard() {
        Utilisateur utilisateur = requireRole(Role.RESPONSABLE);
        Employe responsable = requireEmploye(utilisateur);
        if (responsable.getDepartement() == null) {
            throw new InvalidBusinessRequestException("Utilisateur connecte sans departement lie.");
        }

        Long departementId = responsable.getDepartement().getId();
        Long responsableId = responsable.getIdEmp();
        SoldeConge solde = soldeCongeService.getOrCreateSolde(responsableId, LocalDate.now().getYear());
        DateTimeWindow month = currentMonthDateTimeWindow();

        return new ResponsableDashboardResponse(
                nvl(solde.getSoldeActuel()),
                nvl(solde.getSoldeTotal()),
                solde.getAnnee(),
                employeRepository.countByDepartementIdAndIdEmpNot(departementId, responsableId),
                demandeCongeRepository.countTeamByTypeDemandeAndStatus(
                        departementId, responsableId, TypeDemande.CONGE, StatusDemande.VALIDE_EMPLOYE),
                demandeCongeRepository.countTeamByTypeDemandeAndStatus(
                        departementId, responsableId, TypeDemande.ABSENCE, StatusDemande.VALIDE_EMPLOYE),
                demandeCongeRepository.countTeamByStatusInAndUpdatedAtBetween(
                        departementId, responsableId, RESPONSABLE_PROCESSED_STATUSES, month.start(), month.end()),
                demandeCongeRepository.countTeamByStatusInAndUpdatedAtBetween(
                        departementId, responsableId, Set.of(StatusDemande.REFUSE_RESPONSABLE), month.start(), month.end()),
                demandeCongeRepository.countTeamByStatusInAndUpdatedAtBetween(
                        departementId, responsableId, Set.of(StatusDemande.VALIDE_DG), month.start(), month.end()),
                demandeCongeRepository.countTeamRequestsByStatus(departementId, responsableId).stream()
                        .map(row -> new DashboardChartItemDto(String.valueOf(row[0]), asLong(row[1])))
                        .toList(),
                demandeCongeRepository.findUpcomingApprovedLeavesForTeam(
                        departementId, responsableId, LocalDate.now(), PageRequest.of(0, RECENT_LIMIT))
                        .stream().map(this::toRecentDemande).toList(),
                demandeCongeRepository.findRecentTeamRequests(departementId, responsableId, PageRequest.of(0, RECENT_LIMIT))
                        .stream().map(this::toRecentDemande).toList(),
                latestNotifications(utilisateur.getId()),
                notificationRepository.countByRecipientIdAndReadFalse(utilisateur.getId()));
    }

    @Override
    @Transactional
    public RhDashboardResponse getRhDashboard(Integer annee, Integer mois) {
        Utilisateur utilisateur = requireRole(Role.RH);
        Employe rh = requireEmploye(utilisateur);
        int selectedYear = annee == null ? LocalDate.now().getYear() : annee;
        int selectedMonth = mois == null ? LocalDate.now().getMonthValue() : mois;
        DateWindow year = yearWindow(selectedYear);
        DateWindow month = monthWindow(selectedYear, selectedMonth);
        SoldeConge solde = soldeCongeService.getOrCreateSolde(rh.getIdEmp(), LocalDate.now().getYear());

        return new RhDashboardResponse(
                nvl(solde.getSoldeActuel()),
                nvl(solde.getSoldeTotal()),
                solde.getAnnee(),
                demandeCongeRepository.countValideDgByTypeAndDgDateOverlap(TypeDemande.CONGE, month.start(), month.end()),
                demandeCongeRepository.countValideDgByTypeAndDgDateOverlap(TypeDemande.CONGE, year.start(), year.end()),
                demandeCongeRepository.countValideDgByTypeAndDgDateOverlap(TypeDemande.ABSENCE, month.start(), month.end()),
                demandeCongeRepository.countValideDgByTypeAndDgDateOverlap(TypeDemande.ABSENCE, year.start(), year.end()),
                nvl(demandeCongeRepository.sumJoursValideDgByTypeAndDgDateOverlap(TypeDemande.CONGE, month.start(), month.end())),
                nvl(demandeCongeRepository.sumJoursValideDgByTypeAndDgDateOverlap(TypeDemande.ABSENCE, month.start(), month.end())),
                demandeCongeRepository.findEmployeesWithMostAbsences(year.start(), year.end(), PageRequest.of(0, RECENT_LIMIT)),
                demandeCongeRepository.countValideDgByDepartmentAndDgDateOverlap(year.start(), year.end()),
                demandeCongeRepository.findRecentByTypeDemandeAndStatus(
                        TypeDemande.CONGE, StatusDemande.VALIDE_DG, PageRequest.of(0, RECENT_LIMIT))
                        .stream().map(this::toRecentDemande).toList(),
                demandeCongeRepository.findRecentByTypeDemandeAndStatus(
                        TypeDemande.ABSENCE, StatusDemande.VALIDE_DG, PageRequest.of(0, RECENT_LIMIT))
                        .stream().map(this::toRecentDemande).toList(),
                latestNotifications(utilisateur.getId()),
                notificationRepository.countByRecipientIdAndReadFalse(utilisateur.getId()));
    }

    @Override
    @Transactional(readOnly = true)
    public DirecteurGeneralDashboardResponse getDirecteurGeneralDashboard() {
        Utilisateur utilisateur = requireRole(Role.DIRECTEUR_GENERAL);
        DateTimeWindow month = currentMonthDateTimeWindow();

        return new DirecteurGeneralDashboardResponse(
                demandeCongeRepository.countByStatusAndTypeDemande(StatusDemande.VALIDE_RESPONSABLE, TypeDemande.CONGE),
                demandeCongeRepository.countByStatusAndTypeDemande(StatusDemande.VALIDE_RESPONSABLE, TypeDemande.ABSENCE),
                demandeCongeRepository.countByStatusAndUpdatedAtBetween(StatusDemande.VALIDE_DG, month.start(), month.end()),
                demandeCongeRepository.countByStatusAndUpdatedAtBetween(StatusDemande.REFUSE_DG, month.start(), month.end()),
                demandeCongeRepository.countByStatusAndUpdatedAtBetween(StatusDemande.MODIFICATION_DG, month.start(), month.end()),
                employeRepository.countByUtilisateurRoleNot(Role.ADMINISTRATEUR),
                employeRepository.countEmployeesByDepartmentExcluding(Role.ADMINISTRATEUR),
                employeRepository.countEmployeesByRoleExcluding(Role.ADMINISTRATEUR),
                demandeCongeRepository.findRecentByStatusIn(Set.of(StatusDemande.VALIDE_RESPONSABLE), PageRequest.of(0, RECENT_LIMIT))
                        .stream().map(this::toRecentDemande).toList(),
                demandeCongeRepository.findRecentByStatusIn(DG_PROCESSED_STATUSES, PageRequest.of(0, RECENT_LIMIT))
                        .stream().map(this::toRecentDemande).toList(),
                latestNotifications(utilisateur.getId()),
                notificationRepository.countByRecipientIdAndReadFalse(utilisateur.getId()));
    }

    private Utilisateur requireRole(Role role) {
        Utilisateur utilisateur = currentUser();
        if (utilisateur.getRole() != role) {
            throw new ForbiddenException("Acces non autorise.");
        }
        return utilisateur;
    }

    private Utilisateur currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new InvalidBusinessRequestException("Utilisateur connecte introuvable.");
        }
        return utilisateurRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new InvalidBusinessRequestException("Utilisateur connecte introuvable."));
    }

    private Employe requireEmploye(Utilisateur utilisateur) {
        return employeRepository.findByUtilisateurId(utilisateur.getId())
                .orElseThrow(() -> new InvalidBusinessRequestException("Utilisateur connecte sans employe lie."));
    }

    private List<DashboardNotificationDto> latestNotifications(Long utilisateurId) {
        return notificationRepository.findTop5ByRecipientIdOrderByCreatedAtDesc(utilisateurId).stream()
                .map(this::toNotification)
                .toList();
    }

    private DashboardNotificationDto toNotification(Notification notification) {
        return new DashboardNotificationDto(
                notification.getId(),
                nvl(notification.getTitle(), "Notification"),
                nvl(notification.getMessage(), ""),
                notification.isRead(),
                notification.getTargetUrl(),
                notification.getCreatedAt());
    }

    private DashboardRecentDemandeDto toRecentDemande(DemandeConge demande) {
        return new DashboardRecentDemandeDto(
                demande.getId(),
                "DEM-" + String.format("%05d", demande.getId()),
                demande.getTypeDemande() == null ? "CONGE" : demande.getTypeDemande().name(),
                demande.getStatus() == null ? "BROUILLON" : demande.getStatus().name(),
                nomComplet(demande.getEmploye()),
                demande.getEmploye() == null || demande.getEmploye().getDepartement() == null
                        ? "Non defini"
                        : demande.getEmploye().getDepartement().getNom(),
                firstNonNull(demande.getDateDebutDg(), demande.getDateDebutResp(), demande.getDateDebutEmp()),
                firstNonNull(demande.getDateFinDg(), demande.getDateFinResp(), demande.getDateFinEmp()),
                nvl(demande.getJoursDeduits()),
                demande.getCreatedAt());
    }

    private DashboardRecentEmployeDto toRecentEmploye(Employe employe) {
        return new DashboardRecentEmployeDto(
                employe.getIdEmp(),
                nvl(employe.getMatricule(), ""),
                nomComplet(employe),
                employe.getUtilisateur() == null ? "" : nvl(employe.getUtilisateur().getEmail(), ""),
                employe.getUtilisateur() == null || employe.getUtilisateur().getRole() == null
                        ? "Non defini"
                        : employe.getUtilisateur().getRole().name(),
                employe.getDepartement() == null ? "Non defini" : employe.getDepartement().getNom(),
                employe.getCreatedAt());
    }

    private String nomComplet(Employe employe) {
        if (employe == null) {
            return "Non defini";
        }
        return (nvl(employe.getPrenom(), "") + " " + nvl(employe.getNom(), "")).trim();
    }

    private LocalDate firstNonNull(LocalDate first, LocalDate second, LocalDate third) {
        if (first != null) {
            return first;
        }
        if (second != null) {
            return second;
        }
        return third;
    }

    private DateWindow yearWindow(int annee) {
        return new DateWindow(LocalDate.of(annee, 1, 1), LocalDate.of(annee + 1, 1, 1));
    }

    private DateWindow monthWindow(int annee, int mois) {
        if (mois < 1 || mois > 12) {
            throw new InvalidBusinessRequestException("Mois invalide.");
        }
        LocalDate start = LocalDate.of(annee, mois, 1);
        return new DateWindow(start, start.plusMonths(1));
    }

    private DateTimeWindow currentMonthDateTimeWindow() {
        LocalDate today = LocalDate.now();
        LocalDate start = LocalDate.of(today.getYear(), today.getMonthValue(), 1);
        return new DateTimeWindow(start.atStartOfDay(), start.plusMonths(1).atStartOfDay());
    }

    private Double nvl(Double value) {
        return value == null ? 0D : value;
    }

    private String nvl(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private Long asLong(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        return 0L;
    }

    private record DateWindow(LocalDate start, LocalDate end) {
    }

    private record DateTimeWindow(LocalDateTime start, LocalDateTime end) {
    }
}
