package com.example.backend.repository;

import com.example.backend.model.Utilisateur;
import com.example.backend.model.enums.Role;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UtilisateurRepository extends JpaRepository<Utilisateur, Long> {
    Optional<Utilisateur> findByEmail(String email);

    long countByRole(Role role);

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, Long id);

    List<Utilisateur> findByRole(Role role);

    Optional<Utilisateur> findFirstByRole(Role role);

    List<Utilisateur> findAllByRole(Role role);
}
