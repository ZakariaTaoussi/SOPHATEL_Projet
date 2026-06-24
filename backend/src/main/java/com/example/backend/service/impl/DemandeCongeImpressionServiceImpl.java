package com.example.backend.service.impl;

import com.example.backend.dto.demande.DemandeCongeImpressionResponse;
import com.example.backend.exception.ForbiddenException;
import com.example.backend.exception.InvalidBusinessRequestException;
import com.example.backend.exception.ResourceNotFoundException;
import com.example.backend.model.DemandeConge;
import com.example.backend.model.Departement;
import com.example.backend.model.Employe;
import com.example.backend.model.SoldeConge;
import com.example.backend.model.enums.Role;
import com.example.backend.model.enums.StatusDemande;
import com.example.backend.model.enums.TypeDemande;
import com.example.backend.repository.DemandeCongeRepository;
import com.example.backend.repository.SoldeCongeRepository;
import com.example.backend.service.interfaces.IDemandeCongeImpressionService;
import java.time.LocalDate;
import java.util.EnumSet;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DemandeCongeImpressionServiceImpl implements IDemandeCongeImpressionService {

    private static final Set<StatusDemande> STATUTS_IMPRIMABLES = EnumSet.of(
            StatusDemande.VALIDE_DG,
            StatusDemande.ANNULE,
            StatusDemande.REFUSE_RESPONSABLE,
            StatusDemande.REFUSE_DG);

    private final DemandeCongeRepository demandeCongeRepository;
    private final SoldeCongeRepository soldeCongeRepository;
    private final EmployeConnecteProvider employeConnecteProvider;

    public DemandeCongeImpressionServiceImpl(
            DemandeCongeRepository demandeCongeRepository,
            SoldeCongeRepository soldeCongeRepository,
            EmployeConnecteProvider employeConnecteProvider) {
        this.demandeCongeRepository = demandeCongeRepository;
        this.soldeCongeRepository = soldeCongeRepository;
        this.employeConnecteProvider = employeConnecteProvider;
    }

    @Override
    @Transactional(readOnly = true)
    public DemandeCongeImpressionResponse getDemandePourImpression(Long demandeId) {
        DemandeConge demande = demandeCongeRepository.findById(demandeId)
                .orElseThrow(() -> new ResourceNotFoundException("Demande introuvable"));

        if (demande.getTypeDemande() != TypeDemande.CONGE) {
            throw new InvalidBusinessRequestException("Une absence ne peut pas etre imprimee avec ce formulaire.");
        }
        if (!STATUTS_IMPRIMABLES.contains(demande.getStatus())) {
            throw new InvalidBusinessRequestException("Cette demande ne peut pas encore etre imprimee.");
        }

        Employe connecte = employeConnecteProvider.getEmployeConnecte();
        verifierDroitAcces(demande, connecte);

        LocalDate dateAnneeDebut = demande.getDateDebutDg() != null ? demande.getDateDebutDg() : demande.getDateDebutEmp();
        LocalDate dateAnneeFin = demande.getDateFinDg() != null ? demande.getDateFinDg() : demande.getDateFinEmp();
        Double reliquat = soldeCongeRepository.findByEmployeIdAndAnnee(
                        demande.getEmploye().getIdEmp(),
                        dateAnneeDebut.getYear())
                .map(SoldeConge::getSoldeActuel)
                .orElse(null);

        Departement departement = demande.getEmploye().getDepartement();
        Role roleEmploye = demande.getEmploye().getUtilisateur() == null ? null : demande.getEmploye().getUtilisateur().getRole();

        return new DemandeCongeImpressionResponse(
                demande.getId(),
                demande.getEmploye().getNom(),
                demande.getEmploye().getPrenom(),
                roleEmploye == null ? "-" : roleEmploye.name(),
                departement == null ? "-" : departement.getNom(),
                demande.getTypeDemande(),
                demande.getNatureConge(),
                demande.getDateDebutEmp(),
                demande.getDateFinEmp(),
                demande.getDateDebutDg(),
                demande.getDateFinDg(),
                demande.getJoursDeduits(),
                dateAnneeDebut.getYear(),
                dateAnneeFin.getYear(),
                reliquat,
                demande.getStatus(),
                statutDemandeur(demande.getStatus()),
                statutResponsable(demande.getStatus()),
                statutDirecteurGeneral(demande.getStatus()),
                demande.getCreatedAt(),
                demande.getUpdatedAt());
    }

    private void verifierDroitAcces(DemandeConge demande, Employe connecte) {
        Role role = connecte.getUtilisateur() == null ? null : connecte.getUtilisateur().getRole();
        if (role == Role.DIRECTEUR_GENERAL) {
            return;
        }
        if (demande.getEmploye().getIdEmp().equals(connecte.getIdEmp())) {
            return;
        }
        if (role == Role.RESPONSABLE
                && demande.getEmploye().getDepartement() != null
                && connecte.getDepartement() != null
                && demande.getEmploye().getDepartement().getId().equals(connecte.getDepartement().getId())) {
            return;
        }
        throw new ForbiddenException("Vous n'avez pas le droit d'imprimer cette demande.");
    }

    private String statutDemandeur(StatusDemande status) {
        if (status == StatusDemande.ANNULE) {
            return "ANNULE";
        }
        return "VALIDE_EMPLOYE";
    }

    private String statutResponsable(StatusDemande status) {
        if (status == StatusDemande.REFUSE_RESPONSABLE) {
            return "REFUSE_RESPONSABLE";
        }
        if (status == StatusDemande.ANNULE) {
            return "ANNULE";
        }
        return "VALIDE_RESPONSABLE";
    }

    private String statutDirecteurGeneral(StatusDemande status) {
        if (status == StatusDemande.REFUSE_RESPONSABLE) {
            return "-";
        }
        if (status == StatusDemande.REFUSE_DG) {
            return "REFUSE_DG";
        }
        if (status == StatusDemande.ANNULE) {
            return "ANNULE";
        }
        return "VALIDE_DG";
    }
}
