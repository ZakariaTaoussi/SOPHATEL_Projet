package com.example.backend.repository;

import com.example.backend.model.Employe;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EmployeRepository extends JpaRepository<Employe, Long> {
    Optional<Employe> findByUtilisateurId(Long utilisateurId);

    boolean existsByMatricule(String matricule);

    boolean existsByMatriculeAndIdEmpNot(String matricule, Long idEmp);

    boolean existsByDepartementId(Long departementId);

    @Query("""
            select e from Employe e
            join e.utilisateur u
            left join e.departement d
            where lower(e.nom) like lower(concat('%', :search, '%'))
               or lower(e.prenom) like lower(concat('%', :search, '%'))
               or lower(e.matricule) like lower(concat('%', :search, '%'))
               or lower(u.email) like lower(concat('%', :search, '%'))
            """)
    Page<Employe> search(@Param("search") String search, Pageable pageable);
}
