package com.example.backend.repository;

import com.example.backend.model.Departement;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DepartementRepository extends JpaRepository<Departement, Long> {
    Optional<Departement> findByNomIgnoreCase(String nom);

    boolean existsByNomIgnoreCase(String nom);

    boolean existsByNomIgnoreCaseAndIdNot(String nom, Long id);

    @Modifying(flushAutomatically = true)
    @Query("update Departement d set d.responsable = null where d.responsable.idEmp = :responsableId")
    int clearResponsable(@Param("responsableId") Long responsableId);
}
