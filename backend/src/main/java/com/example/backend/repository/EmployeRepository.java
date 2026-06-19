package com.example.backend.repository;

import com.example.backend.model.Employe;
import com.example.backend.model.enums.Role;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EmployeRepository extends JpaRepository<Employe, Long> {
    Optional<Employe> findByUtilisateurId(Long utilisateurId);

    Optional<Employe> findFirstByUtilisateurRole(Role role);

    Optional<Employe> findFirstByDepartementIdAndUtilisateurRole(Long departementId, Role role);

    @Query("""
            select e from Employe e
            join e.utilisateur u
            where e.departement.id = :departementId
              and e.idEmp <> :responsableEmployeId
              and u.role in :roles
            order by e.nom asc, e.prenom asc
            """)
    List<Employe> findEmployesEquipeResponsable(
            @Param("departementId") Long departementId,
            @Param("responsableEmployeId") Long responsableEmployeId,
            @Param("roles") Set<Role> roles);

    @Query("""
            select e from Employe e
            join e.utilisateur u
            where e.departement.id = :departementId
              and e.idEmp <> :responsableEmployeId
              and u.role in :roles
            order by e.nom asc, e.prenom asc
            """)
    Page<Employe> findEmployesEquipeResponsable(
            @Param("departementId") Long departementId,
            @Param("responsableEmployeId") Long responsableEmployeId,
            @Param("roles") Set<Role> roles,
            Pageable pageable);

    @Query("""
            select e from Employe e
            join e.utilisateur u
            left join e.departement d
            where u.role in :roles
              and (:role is null or u.role = :role)
              and (:departementId is null or d.id = :departementId)
              and (
                    :searchPattern is null
                    or lower(e.nom) like :searchPattern
                    or lower(e.prenom) like :searchPattern
                    or lower(u.email) like :searchPattern
                    or lower(e.matricule) like :searchPattern
              )
            order by e.nom asc, e.prenom asc
            """)
    Page<Employe> findEmployesForDirecteurGeneral(
            @Param("roles") Set<Role> roles,
            @Param("role") Role role,
            @Param("departementId") Long departementId,
            @Param("searchPattern") String searchPattern,
            Pageable pageable);

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
