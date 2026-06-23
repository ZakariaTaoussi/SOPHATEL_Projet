package com.example.backend.mapper;

import com.example.backend.dto.demande.DemandeCongeCreateRequest;
import com.example.backend.dto.demande.DemandeCongeResponse;
import com.example.backend.dto.demande.DemandeCongeUpdateRequest;
import com.example.backend.dto.demande.DirecteurGeneralDemandeResponse;
import com.example.backend.dto.demande.ResponsableDemandeResponse;
import com.example.backend.model.Departement;
import com.example.backend.model.DemandeConge;
import com.example.backend.model.Employe;
import com.example.backend.model.enums.StatusDemande;
import org.springframework.stereotype.Component;

@Component
public class DemandeCongeMapper {

    public DemandeConge toEntity(DemandeCongeCreateRequest request, Employe employe) {
        DemandeConge demande = new DemandeConge();
        demande.setEmploye(employe);
        demande.setDateDebutEmp(request.getDateDebutEmp());
        demande.setDateFinEmp(request.getDateFinEmp());
        demande.setTypeDemande(request.getTypeDemande());
        demande.setNatureConge(request.getNatureConge());
        demande.setStatus(StatusDemande.BROUILLON);
        demande.setJoursDeduits(0D);
        return demande;
    }

    public void updateEntity(DemandeConge demande, DemandeCongeUpdateRequest request) {
        demande.setDateDebutEmp(request.getDateDebutEmp());
        demande.setDateFinEmp(request.getDateFinEmp());
        demande.setTypeDemande(request.getTypeDemande());
        demande.setNatureConge(request.getNatureConge());
    }

    public DemandeCongeResponse toResponse(DemandeConge demande) {
        Employe employe = demande.getEmploye();
        return new DemandeCongeResponse(
                demande.getId(),
                employe == null ? null : employe.getIdEmp(),
                employe == null ? null : employe.getPrenom() + " " + employe.getNom(),
                demande.getDateDebutEmp(),
                demande.getDateFinEmp(),
                demande.getDateDebutResp(),
                demande.getDateFinResp(),
                demande.getDateDebutDg(),
                demande.getDateFinDg(),
                demande.getTypeDemande(),
                demande.getNatureConge(),
                demande.getStatus(),
                demande.getJoursDeduits(),
                demande.getCreatedAt(),
                demande.getUpdatedAt());
    }

    public ResponsableDemandeResponse toResponsableResponse(DemandeConge demande) {
        Employe employe = demande.getEmploye();
        Departement departement = employe == null ? null : employe.getDepartement();
        return new ResponsableDemandeResponse(
                demande.getId(),
                employe == null ? null : employe.getIdEmp(),
                employe == null ? null : employe.getPrenom() + " " + employe.getNom(),
                departement == null ? null : departement.getId(),
                departement == null ? null : departement.getNom(),
                demande.getDateDebutEmp(),
                demande.getDateFinEmp(),
                demande.getDateDebutResp(),
                demande.getDateFinResp(),
                demande.getDateDebutDg(),
                demande.getDateFinDg(),
                demande.getTypeDemande(),
                demande.getNatureConge(),
                demande.getStatus(),
                demande.getJoursDeduits(),
                demande.getCreatedAt(),
                demande.getUpdatedAt());
    }

    public DirecteurGeneralDemandeResponse toDirecteurGeneralResponse(DemandeConge demande) {
        Employe employe = demande.getEmploye();
        Departement departement = employe == null ? null : employe.getDepartement();
        Employe responsable = departement == null ? null : departement.getResponsable();
        return new DirecteurGeneralDemandeResponse(
                demande.getId(),
                employe == null ? null : employe.getIdEmp(),
                nomComplet(employe),
                departement == null ? null : departement.getId(),
                departement == null ? null : departement.getNom(),
                nomComplet(responsable),
                demande.getDateDebutEmp(),
                demande.getDateFinEmp(),
                demande.getDateDebutResp(),
                demande.getDateFinResp(),
                demande.getDateDebutDg(),
                demande.getDateFinDg(),
                demande.getTypeDemande(),
                demande.getNatureConge(),
                demande.getStatus(),
                demande.getJoursDeduits(),
                demande.getCreatedAt(),
                demande.getUpdatedAt());
    }

    private String nomComplet(Employe employe) {
        return employe == null ? null : employe.getPrenom() + " " + employe.getNom();
    }
}
