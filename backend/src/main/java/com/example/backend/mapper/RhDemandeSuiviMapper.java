package com.example.backend.mapper;

import com.example.backend.dto.rh.RhDemandeSuiviResponse;
import com.example.backend.model.DemandeConge;
import com.example.backend.model.Departement;
import com.example.backend.model.Employe;
import com.example.backend.model.Utilisateur;
import org.springframework.stereotype.Component;

@Component
public class RhDemandeSuiviMapper {

    public RhDemandeSuiviResponse toResponse(DemandeConge demande) {
        Employe employe = demande == null ? null : demande.getEmploye();
        Utilisateur utilisateur = employe == null ? null : employe.getUtilisateur();
        Departement departement = employe == null ? null : employe.getDepartement();
        Long id = demande == null ? null : demande.getId();

        return new RhDemandeSuiviResponse(
                id,
                id == null ? null : "DEM-" + id,
                employe == null ? null : employe.getIdEmp(),
                utilisateur == null ? null : utilisateur.getId(),
                nomComplet(employe),
                employe == null ? null : employe.getMatricule(),
                utilisateur == null ? null : utilisateur.getEmail(),
                departement == null ? null : departement.getId(),
                departement == null ? null : departement.getNom(),
                demande == null || demande.getTypeDemande() == null ? null : demande.getTypeDemande().name(),
                demande == null || demande.getNatureConge() == null ? null : demande.getNatureConge().name(),
                demande == null || demande.getStatus() == null ? null : demande.getStatus().name(),
                demande == null ? null : demande.getDateDebutDg(),
                demande == null ? null : demande.getDateFinDg(),
                demande == null || demande.getJoursDeduits() == null ? 0D : demande.getJoursDeduits(),
                demande == null ? null : demande.getCreatedAt(),
                demande == null ? null : demande.getUpdatedAt());
    }

    private String nomComplet(Employe employe) {
        if (employe == null) {
            return null;
        }
        String prenom = employe.getPrenom() == null ? "" : employe.getPrenom().trim();
        String nom = employe.getNom() == null ? "" : employe.getNom().trim();
        String fullName = (prenom + " " + nom).trim();
        return fullName.isEmpty() ? null : fullName;
    }
}
