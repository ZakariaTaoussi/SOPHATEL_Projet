package com.example.backend.testutil;

import com.example.backend.dto.demande.DemandeCongeResponse;
import com.example.backend.model.DemandeConge;
import com.example.backend.model.Departement;
import com.example.backend.model.Employe;
import com.example.backend.model.SoldeConge;
import com.example.backend.model.Utilisateur;
import com.example.backend.model.enums.NatureConge;
import com.example.backend.model.enums.Role;
import com.example.backend.model.enums.StatusDemande;
import com.example.backend.model.enums.TypeDemande;
import java.time.LocalDate;
import java.time.LocalDateTime;

public final class TestFixtures {

    private TestFixtures() {
    }

    public static Utilisateur utilisateur(Long id, String email, Role role) {
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setId(id);
        utilisateur.setEmail(email);
        utilisateur.setPassword("encoded-password");
        utilisateur.setRole(role);
        return utilisateur;
    }

    public static Employe employe(Long id, Role role) {
        Employe employe = new Employe();
        employe.setIdEmp(id);
        employe.setMatricule("EMP-%03d".formatted(id));
        employe.setNom("Nom");
        employe.setPrenom("Prenom");
        employe.setUtilisateur(utilisateur(id, "user%d@example.test".formatted(id), role));
        return employe;
    }

    public static Departement departement(Long id, String nom) {
        Departement departement = new Departement();
        departement.setId(id);
        departement.setNom(nom);
        return departement;
    }

    public static SoldeConge solde(Employe employe, Integer annee, Double soldeActuel, Double soldeTotal) {
        SoldeConge solde = new SoldeConge();
        solde.setId(1L);
        solde.setEmploye(employe);
        solde.setAnnee(annee);
        solde.setSoldeActuel(soldeActuel);
        solde.setSoldeTotal(soldeTotal);
        return solde;
    }

    public static DemandeConge demande(
            Long id,
            Employe employe,
            TypeDemande typeDemande,
            StatusDemande status,
            LocalDate dateDebut,
            LocalDate dateFin) {
        DemandeConge demande = new DemandeConge();
        demande.setId(id);
        demande.setEmploye(employe);
        demande.setDateDebutEmp(dateDebut);
        demande.setDateFinEmp(dateFin);
        demande.setTypeDemande(typeDemande);
        demande.setNatureConge(typeDemande == TypeDemande.CONGE ? NatureConge.ANNUEL : null);
        demande.setStatus(status);
        demande.setJoursDeduits(0D);
        demande.setCreatedAt(LocalDateTime.of(2026, 1, 2, 9, 0));
        demande.setUpdatedAt(LocalDateTime.of(2026, 1, 2, 9, 0));
        return demande;
    }

    public static DemandeCongeResponse demandeResponse(
            Long id,
            TypeDemande typeDemande,
            StatusDemande status) {
        return new DemandeCongeResponse(
                id,
                10L,
                "Prenom Nom",
                LocalDate.of(2026, 7, 6),
                LocalDate.of(2026, 7, 7),
                null,
                null,
                null,
                null,
                typeDemande,
                typeDemande == TypeDemande.CONGE ? NatureConge.ANNUEL : null,
                status,
                typeDemande == TypeDemande.CONGE ? 2D : 0D,
                LocalDateTime.of(2026, 7, 1, 8, 30),
                LocalDateTime.of(2026, 7, 1, 8, 30));
    }
}
