package com.example.backend.repository;

import com.example.backend.model.DemandeConge;
import com.example.backend.model.enums.StatusDemande;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DemandeCongeRepository extends JpaRepository<DemandeConge, Long> {

    @Query("select d from DemandeConge d where d.employe.idEmp = :employeId order by d.createdAt desc")
    List<DemandeConge> findByEmployeIdOrderByCreatedAtDesc(@Param("employeId") Long employeId);

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

    @Query("select d from DemandeConge d where d.status = :status order by d.updatedAt desc")
    List<DemandeConge> findByStatusOrderByUpdatedAtDesc(@Param("status") StatusDemande status);

    @Query("select d from DemandeConge d where d.id = :demandeId and d.employe.departement.id = :departementId")
    Optional<DemandeConge> findByIdAndEmployeDepartementId(
            @Param("demandeId") Long demandeId,
            @Param("departementId") Long departementId);

    @Query("""
            select coalesce(sum(d.joursDeduits), 0)
            from DemandeConge d
            where d.employe.idEmp = :employeId
              and d.dateDebutEmp between :dateDebut and :dateFin
              and d.typeDemande = com.example.backend.model.enums.TypeDemande.CONGE
              and d.joursDeduits > 0
            """)
    Double sumJoursDeduitsByEmployeAndDateDebutBetween(
            @Param("employeId") Long employeId,
            @Param("dateDebut") LocalDate dateDebut,
            @Param("dateFin") LocalDate dateFin);
}
