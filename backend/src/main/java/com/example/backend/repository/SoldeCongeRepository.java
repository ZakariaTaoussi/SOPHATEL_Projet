package com.example.backend.repository;

import com.example.backend.model.SoldeConge;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SoldeCongeRepository extends JpaRepository<SoldeConge, Long> {

    @Query("select s from SoldeConge s where s.employe.idEmp = :employeId and s.annee = :annee")
    Optional<SoldeConge> findByEmployeIdAndAnnee(
            @Param("employeId") Long employeId,
            @Param("annee") Integer annee);
}
