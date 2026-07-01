package com.example.backend.repository;

import com.example.backend.model.DemandeConge;
import com.example.backend.model.enums.StatusDemande;
import com.example.backend.model.enums.TypeDemande;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DemandeCongeRepository extends JpaRepository<DemandeConge, Long>, JpaSpecificationExecutor<DemandeConge> {

    @Query("select d from DemandeConge d where d.employe.idEmp = :employeId order by d.createdAt desc")
    List<DemandeConge> findByEmployeIdOrderByCreatedAtDesc(@Param("employeId") Long employeId);

    @Query("select count(d) from DemandeConge d where d.employe.idEmp = :employeId and d.typeDemande = :typeDemande")
    long countByEmployeIdAndTypeDemande(
            @Param("employeId") Long employeId,
            @Param("typeDemande") TypeDemande typeDemande);

    @Query("select count(d) from DemandeConge d where d.employe.idEmp = :employeId and d.status = :status")
    long countByEmployeIdAndStatus(
            @Param("employeId") Long employeId,
            @Param("status") StatusDemande status);

    @Query("select count(d) from DemandeConge d where d.employe.idEmp = :employeId and d.status in :statuses")
    long countByEmployeIdAndStatusIn(
            @Param("employeId") Long employeId,
            @Param("statuses") Collection<StatusDemande> statuses);

    @Query("""
            select count(d) from DemandeConge d
            where d.employe.idEmp = :employeId
              and d.typeDemande = :typeDemande
              and d.dateDebutEmp >= :dateDebut
              and d.dateDebutEmp < :dateFinExclusive
            """)
    long countByEmployeIdAndTypeDemandeAndDateDebutEmpBetweenExclusive(
            @Param("employeId") Long employeId,
            @Param("typeDemande") TypeDemande typeDemande,
            @Param("dateDebut") LocalDate dateDebut,
            @Param("dateFinExclusive") LocalDate dateFinExclusive);

    @Query("""
            select d from DemandeConge d
            where d.employe.idEmp = :employeId
            order by d.createdAt desc
            """)
    List<DemandeConge> findRecentByEmployeId(
            @Param("employeId") Long employeId,
            Pageable pageable);

    @Query("""
            select d from DemandeConge d
            where d.employe.idEmp = :employeId
              and d.typeDemande = :typeDemande
            order by d.createdAt desc
            """)
    List<DemandeConge> findByEmployeIdAndTypeDemandeOrderByCreatedAtDesc(
            @Param("employeId") Long employeId,
            @Param("typeDemande") TypeDemande typeDemande);

    @Query("""
            select d from DemandeConge d
            where d.employe.idEmp = :employeId
              and d.typeDemande = :typeDemande
              and d.status = :status
              and d.dateDebutEmp between :dateDebut and :dateFin
            order by d.dateDebutEmp asc
            """)
    List<DemandeConge> findByEmployeIdAndTypeDemandeAndStatusAndDateDebutEmpBetween(
            @Param("employeId") Long employeId,
            @Param("typeDemande") TypeDemande typeDemande,
            @Param("status") StatusDemande status,
            @Param("dateDebut") LocalDate dateDebut,
            @Param("dateFin") LocalDate dateFin);

    @Query("select d from DemandeConge d where d.id = :id and d.employe.idEmp = :employeId")
    Optional<DemandeConge> findByIdAndEmployeId(@Param("id") Long id, @Param("employeId") Long employeId);

    @Query("select d from DemandeConge d where d.employe.idEmp = :employeId and d.status = :status")
    List<DemandeConge> findByEmployeIdAndStatus(
            @Param("employeId") Long employeId,
            @Param("status") StatusDemande status);

    @Query("select d from DemandeConge d where d.employe.idEmp = :employeId and d.status in :statuses")
    List<DemandeConge> findByEmployeIdAndStatusIn(
            @Param("employeId") Long employeId,
            @Param("statuses") Collection<StatusDemande> statuses);

    @Query("""
            select d from DemandeConge d
            where d.employe.departement.id = :departementId
              and d.status = :status
            order by d.createdAt desc
            """)
    List<DemandeConge> findByEmployeDepartementIdAndStatusOrderByCreatedAtDesc(
            @Param("departementId") Long departementId,
            @Param("status") StatusDemande status);

    @Query("""
            select d from DemandeConge d
            where d.employe.departement.id = :departementId
              and d.status in :statuses
            order by d.updatedAt desc
            """)
    List<DemandeConge> findByEmployeDepartementIdAndStatusInOrderByUpdatedAtDesc(
            @Param("departementId") Long departementId,
            @Param("statuses") Collection<StatusDemande> statuses);

    @Query("""
            select d from DemandeConge d
            where d.employe.departement.id = :departementId
              and d.employe.idEmp <> :employeId
              and d.status in :statuses
            order by d.updatedAt desc
            """)
    List<DemandeConge> findByEmployeDepartementIdAndEmployeIdNotAndStatusInOrderByUpdatedAtDesc(
            @Param("departementId") Long departementId,
            @Param("employeId") Long employeId,
            @Param("statuses") Collection<StatusDemande> statuses);

    @Query("""
            select count(d) from DemandeConge d
            where d.employe.departement.id = :departementId
              and d.employe.idEmp <> :employeId
              and d.typeDemande = :typeDemande
              and d.status = :status
            """)
    long countTeamByTypeDemandeAndStatus(
            @Param("departementId") Long departementId,
            @Param("employeId") Long employeId,
            @Param("typeDemande") TypeDemande typeDemande,
            @Param("status") StatusDemande status);

    @Query("""
            select count(d) from DemandeConge d
            where d.employe.departement.id = :departementId
              and d.employe.idEmp <> :employeId
              and d.status in :statuses
              and d.updatedAt >= :start
              and d.updatedAt < :end
            """)
    long countTeamByStatusInAndUpdatedAtBetween(
            @Param("departementId") Long departementId,
            @Param("employeId") Long employeId,
            @Param("statuses") Collection<StatusDemande> statuses,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    @Query("""
            select d.status, count(d)
            from DemandeConge d
            where d.employe.departement.id = :departementId
              and d.employe.idEmp <> :employeId
            group by d.status
            order by count(d) desc
            """)
    List<Object[]> countTeamRequestsByStatus(
            @Param("departementId") Long departementId,
            @Param("employeId") Long employeId);

    @Query("""
            select d from DemandeConge d
            where d.employe.departement.id = :departementId
              and d.employe.idEmp <> :employeId
            order by d.updatedAt desc
            """)
    List<DemandeConge> findRecentTeamRequests(
            @Param("departementId") Long departementId,
            @Param("employeId") Long employeId,
            Pageable pageable);

    @Query("""
            select d from DemandeConge d
            where d.employe.departement.id = :departementId
              and d.employe.idEmp <> :employeId
              and d.typeDemande = com.example.backend.model.enums.TypeDemande.CONGE
              and d.status = com.example.backend.model.enums.StatusDemande.VALIDE_DG
              and d.dateFinDg >= :today
            order by d.dateDebutDg asc
            """)
    List<DemandeConge> findUpcomingApprovedLeavesForTeam(
            @Param("departementId") Long departementId,
            @Param("employeId") Long employeId,
            @Param("today") LocalDate today,
            Pageable pageable);

    @Query("""
            select d from DemandeConge d
            where d.employe.departement.id = :departementId
              and d.employe.idEmp <> :employeId
              and d.typeDemande = :typeDemande
              and d.status in :statuses
            order by d.updatedAt desc
            """)
    List<DemandeConge> findByEmployeDepartementIdAndEmployeIdNotAndTypeDemandeAndStatusInOrderByUpdatedAtDesc(
            @Param("departementId") Long departementId,
            @Param("employeId") Long employeId,
            @Param("typeDemande") TypeDemande typeDemande,
            @Param("statuses") Collection<StatusDemande> statuses);

    @Query("select d from DemandeConge d where d.status = :status order by d.updatedAt desc")
    List<DemandeConge> findByStatusOrderByUpdatedAtDesc(@Param("status") StatusDemande status);

    @Query("select count(d) from DemandeConge d where d.status = :status")
    long countByStatus(@Param("status") StatusDemande status);

    @Query("""
            select count(d) from DemandeConge d
            where d.status = :status
              and d.typeDemande = :typeDemande
            """)
    long countByStatusAndTypeDemande(
            @Param("status") StatusDemande status,
            @Param("typeDemande") TypeDemande typeDemande);

    @Query("""
            select count(d) from DemandeConge d
            where d.status = :status
              and d.updatedAt >= :start
              and d.updatedAt < :end
            """)
    long countByStatusAndUpdatedAtBetween(
            @Param("status") StatusDemande status,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    @Query("""
            select d from DemandeConge d
            where d.typeDemande = :typeDemande
              and d.status = :status
            order by d.updatedAt desc
            """)
    List<DemandeConge> findByTypeDemandeAndStatusOrderByUpdatedAtDesc(
            @Param("typeDemande") TypeDemande typeDemande,
            @Param("status") StatusDemande status);

    @Query("""
            select d from DemandeConge d
            where d.typeDemande = :typeDemande
              and d.status = :status
            order by d.updatedAt desc
            """)
    List<DemandeConge> findRecentByTypeDemandeAndStatus(
            @Param("typeDemande") TypeDemande typeDemande,
            @Param("status") StatusDemande status,
            Pageable pageable);

    @Query("select d from DemandeConge d where d.status in :statuses order by d.updatedAt desc")
    List<DemandeConge> findByStatusInOrderByUpdatedAtDesc(@Param("statuses") Collection<StatusDemande> statuses);

    @Query("""
            select d from DemandeConge d
            where d.status in :statuses
            order by d.updatedAt desc
            """)
    List<DemandeConge> findRecentByStatusIn(
            @Param("statuses") Collection<StatusDemande> statuses,
            Pageable pageable);

    @Query("""
            select count(d) from DemandeConge d
            where d.status = com.example.backend.model.enums.StatusDemande.VALIDE_DG
              and d.typeDemande = :typeDemande
              and d.dateDebutDg < :end
              and d.dateFinDg >= :start
            """)
    long countValideDgByTypeAndDgDateOverlap(
            @Param("typeDemande") TypeDemande typeDemande,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end);

    @Query("""
            select coalesce(sum(d.joursDeduits), 0)
            from DemandeConge d
            where d.status = com.example.backend.model.enums.StatusDemande.VALIDE_DG
              and d.typeDemande = :typeDemande
              and d.dateDebutDg < :end
              and d.dateFinDg >= :start
            """)
    Double sumJoursValideDgByTypeAndDgDateOverlap(
            @Param("typeDemande") TypeDemande typeDemande,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end);

    @Query("""
            select new com.example.backend.dto.dashboard.DashboardChartItemDto(coalesce(d.employe.departement.nom, 'Non defini'), count(d))
            from DemandeConge d
            where d.status = com.example.backend.model.enums.StatusDemande.VALIDE_DG
              and d.dateDebutDg < :end
              and d.dateFinDg >= :start
            group by d.employe.departement.nom
            order by count(d) desc
            """)
    List<com.example.backend.dto.dashboard.DashboardChartItemDto> countValideDgByDepartmentAndDgDateOverlap(
            @Param("start") LocalDate start,
            @Param("end") LocalDate end);

    @Query("""
            select new com.example.backend.dto.dashboard.DashboardChartItemDto(concat(d.employe.prenom, ' ', d.employe.nom), count(d))
            from DemandeConge d
            where d.status = com.example.backend.model.enums.StatusDemande.VALIDE_DG
              and d.typeDemande = com.example.backend.model.enums.TypeDemande.ABSENCE
              and d.dateDebutDg < :end
              and d.dateFinDg >= :start
            group by d.employe.idEmp, d.employe.prenom, d.employe.nom
            order by count(d) desc
            """)
    List<com.example.backend.dto.dashboard.DashboardChartItemDto> findEmployeesWithMostAbsences(
            @Param("start") LocalDate start,
            @Param("end") LocalDate end,
            Pageable pageable);

    @Query("select d from DemandeConge d where d.id = :id and d.status = :status")
    Optional<DemandeConge> findByIdAndStatus(
            @Param("id") Long id,
            @Param("status") StatusDemande status);

    @Query("select d from DemandeConge d where d.id = :id and d.status in :statuses")
    Optional<DemandeConge> findByIdAndStatusIn(
            @Param("id") Long id,
            @Param("statuses") Collection<StatusDemande> statuses);

    @Query("select d from DemandeConge d where d.id = :demandeId and d.employe.departement.id = :departementId")
    Optional<DemandeConge> findByIdAndEmployeDepartementId(
            @Param("demandeId") Long demandeId,
            @Param("departementId") Long departementId);

    @Query("""
            select coalesce(sum(d.joursDeduits), 0)
            from DemandeConge d
            where d.employe.idEmp = :employeId
              and coalesce(d.dateDebutDg, d.dateDebutResp, d.dateDebutEmp) between :dateDebut and :dateFin
              and d.typeDemande = com.example.backend.model.enums.TypeDemande.CONGE
              and d.joursDeduits > 0
            """)
    Double sumJoursDeduitsByEmployeAndDateDebutBetween(
            @Param("employeId") Long employeId,
            @Param("dateDebut") LocalDate dateDebut,
            @Param("dateFin") LocalDate dateFin);
}
