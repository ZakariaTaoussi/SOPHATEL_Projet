package com.example.backend.repository.specification;

import com.example.backend.model.DemandeConge;
import com.example.backend.model.Departement;
import com.example.backend.model.Employe;
import com.example.backend.model.Utilisateur;
import com.example.backend.model.enums.Role;
import com.example.backend.model.enums.StatusDemande;
import com.example.backend.model.enums.TypeDemande;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;

public final class DemandeCongeSpecifications {

    private DemandeCongeSpecifications() {
    }

    public static Specification<DemandeConge> rhSuiviDemandes(
            TypeDemande typeDemande,
            StatusDemande status,
            LocalDate dateDebutFiltre,
            LocalDate dateFinFiltre,
            String searchPattern,
            Long departementId) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            Join<DemandeConge, Employe> employe = root.join("employe");
            Join<Employe, Utilisateur> utilisateur = employe.join("utilisateur");
            Join<Employe, Departement> departement = employe.join("departement", JoinType.LEFT);

            predicates.add(cb.equal(root.get("typeDemande"), typeDemande));
            predicates.add(cb.equal(root.get("status"), status));
            predicates.add(cb.notEqual(utilisateur.get("role"), Role.ADMINISTRATEUR));
            predicates.add(cb.isNotNull(root.get("dateDebutDg")));
            predicates.add(cb.isNotNull(root.get("dateFinDg")));

            if (departementId != null) {
                predicates.add(cb.equal(departement.get("id"), departementId));
            }

            if (dateDebutFiltre != null && dateFinFiltre != null) {
                predicates.add(cb.lessThan(root.get("dateDebutDg"), dateFinFiltre));
                predicates.add(cb.greaterThanOrEqualTo(root.get("dateFinDg"), dateDebutFiltre));
            }

            if (searchPattern != null && !searchPattern.isBlank()) {
                predicates.add(cb.or(
                        cb.like(cb.lower(employe.get("nom")), searchPattern),
                        cb.like(cb.lower(employe.get("prenom")), searchPattern),
                        cb.like(cb.lower(employe.get("matricule")), searchPattern),
                        cb.like(cb.lower(utilisateur.get("email")), searchPattern)));
            }

            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }
}
