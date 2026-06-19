package com.example.backend.mapper;

import com.example.backend.dto.directeurgeneral.DirecteurGeneralEmployeResponse;
import com.example.backend.model.Departement;
import com.example.backend.model.Employe;
import com.example.backend.model.SoldeConge;
import com.example.backend.model.Utilisateur;
import org.springframework.stereotype.Component;

@Component
public class DirecteurGeneralEmployeMapper {

    public DirecteurGeneralEmployeResponse toResponse(Employe employe, SoldeConge solde, Integer anneeSolde) {
        Utilisateur utilisateur = employe == null ? null : employe.getUtilisateur();
        Departement departement = employe == null ? null : employe.getDepartement();

        return new DirecteurGeneralEmployeResponse(
                employe == null ? null : employe.getIdEmp(),
                utilisateur == null ? null : utilisateur.getId(),
                employe == null ? null : employe.getNom(),
                employe == null ? null : employe.getPrenom(),
                utilisateur == null ? null : utilisateur.getEmail(),
                utilisateur == null || utilisateur.getRole() == null ? null : utilisateur.getRole().name(),
                employe == null ? null : employe.getMatricule(),
                departement == null ? null : departement.getId(),
                departement == null ? null : departement.getNom(),
                employe == null ? null : employe.getCreatedAt(),
                solde == null ? null : solde.getSoldeActuel(),
                solde == null ? null : solde.getSoldeTotal(),
                solde == null ? anneeSolde : solde.getAnnee());
    }
}
