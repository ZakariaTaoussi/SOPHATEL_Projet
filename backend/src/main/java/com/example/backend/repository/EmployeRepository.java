package com.example.backend.repository;

import com.example.backend.model.Employe;
import com.example.backend.model.enums.Role;
import com.example.backend.dto.dashboard.DashboardChartItemDto;
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

    long countByUtilisateurRole(Role role);

    long countByUtilisateurRoleNot(Role role);

    long countByDepartementId(Long departementId);

    @Query("""
            select count(e) from Employe e
            where e.departement.id = :departementId
              and e.idEmp <> :employeId
            """)
    long countByDepartementIdAndIdEmpNot(
            @Param("departementId") Long departementId,
            @Param("employeId") Long employeId);

    List<Employe> findTop5ByOrderByCreatedAtDesc();

    Optional<Employe> findFirstByUtilisateurRole(Role role);

    Optional<Employe> findFirstByDepartementIdAndUtilisateurRole(Long departementId, Role role);

    @Query("""
            select e
            from Employe e
            join fetch e.utilisateur u
            left join fetch e.departement d
            where d.id = :departementId
              and u.role = :role
            """)
    Optional<Employe> findResponsableByDepartementId(
            @Param("departementId") Long departementId,
            @Param("role") Role role);

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

    @Query("""
            select new com.example.backend.dto.dashboard.DashboardChartItemDto(cast(u.role as string), count(e))
            from Employe e
            join e.utilisateur u
            where u.role <> :excludedRole
            group by u.role
            order by count(e) desc
            """)
    List<DashboardChartItemDto> countEmployeesByRoleExcluding(@Param("excludedRole") Role excludedRole);

    @Query("""
            select new com.example.backend.dto.dashboard.DashboardChartItemDto(cast(u.role as string), count(e))
            from Employe e
            join e.utilisateur u
            group by u.role
            order by count(e) desc
            """)
    List<DashboardChartItemDto> countEmployeesByRole();

    @Query("""
            select new com.example.backend.dto.dashboard.DashboardChartItemDto(coalesce(d.nom, 'Non defini'), count(e))
            from Employe e
            left join e.departement d
            join e.utilisateur u
            where u.role <> :excludedRole
            group by d.nom
            order by count(e) desc
            """)
    List<DashboardChartItemDto> countEmployeesByDepartmentExcluding(@Param("excludedRole") Role excludedRole);

    @Query("""
            select new com.example.backend.dto.dashboard.DashboardChartItemDto(coalesce(d.nom, 'Non defini'), count(e))
            from Employe e
            left join e.departement d
            group by d.nom
            order by count(e) desc
            """)
    List<DashboardChartItemDto> countEmployeesByDepartment();

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
