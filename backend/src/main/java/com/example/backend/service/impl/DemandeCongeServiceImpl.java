package com.example.backend.service.impl;

import com.example.backend.dto.demande.DemandeCongeCreateRequest;
import com.example.backend.dto.demande.DemandeCongeResponse;
import com.example.backend.dto.demande.DemandeCongeUpdateRequest;
import com.example.backend.dto.demande.SoldeCongeResponse;
import com.example.backend.exception.InvalidBusinessRequestException;
import com.example.backend.exception.ResourceNotFoundException;
import com.example.backend.mapper.DemandeCongeMapper;
import com.example.backend.model.DemandeConge;
import com.example.backend.model.Employe;
import com.example.backend.model.enums.Role;
import com.example.backend.model.enums.StatusDemande;
import com.example.backend.model.enums.TypeDemande;
import com.example.backend.repository.DemandeCongeRepository;
import com.example.backend.service.interfaces.IDemandeCongeService;
import com.example.backend.service.interfaces.ISignatureDemandeService;
import com.example.backend.service.interfaces.ISoldeCongeService;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DemandeCongeServiceImpl implements IDemandeCongeService {

    private static final Set<StatusDemande> STATUTS_MODIFICATION_DIRECTE = Set.of(
            StatusDemande.BROUILLON,
            StatusDemande.MODIFICATION_EMPLOYE);

    private final DemandeCongeRepository demandeCongeRepository;
    private final DemandeCongeMapper demandeCongeMapper;
    private final ISoldeCongeService soldeCongeService;
    private final ISignatureDemandeService signatureDemandeService;
    private final EmployeConnecteProvider employeConnecteProvider;

    public DemandeCongeServiceImpl(
            DemandeCongeRepository demandeCongeRepository,
            DemandeCongeMapper demandeCongeMapper,
            ISoldeCongeService soldeCongeService,
            ISignatureDemandeService signatureDemandeService,
            EmployeConnecteProvider employeConnecteProvider) {
        this.demandeCongeRepository = demandeCongeRepository;
        this.demandeCongeMapper = demandeCongeMapper;
        this.soldeCongeService = soldeCongeService;
        this.signatureDemandeService = signatureDemandeService;
        this.employeConnecteProvider = employeConnecteProvider;
    }

    @Override
    @Transactional
    public DemandeCongeResponse creerBrouillon(DemandeCongeCreateRequest request) {
        Employe employe = employeConnecteProvider.getEmployeConnecte();
        validateCreateRequest(request);
        validateDates(request.getDateDebutEmp(), request.getDateFinEmp());
        validateTypeDemande(request.getTypeDemande());

        DemandeConge demande = demandeCongeMapper.toEntity(request, employe);
        return demandeCongeMapper.toResponse(demandeCongeRepository.save(demande));
    }

    @Override
    @Transactional
    public DemandeCongeResponse modifierDemande(Long demandeId, DemandeCongeUpdateRequest request) {
        Employe employe = employeConnecteProvider.getEmployeConnecte();
        DemandeConge demande = findMaDemande(demandeId, employe);
        validateUpdateRequest(request);
        validateDates(request.getDateDebutEmp(), request.getDateFinEmp());
        validateTypeDemande(request.getTypeDemande());
        Role role = getRole(employe);
        validateModificationAutorisee(demande, role);

        demandeCongeMapper.updateEntity(demande, request);
        appliquerDatesEffectivesApresModification(demande, role);
        return demandeCongeMapper.toResponse(demandeCongeRepository.save(demande));
    }

    @Override
    @Transactional
    public DemandeCongeResponse submitDemande(Long demandeId) {
        Employe employe = employeConnecteProvider.getEmployeConnecte();
        DemandeConge demande = findMaDemande(demandeId, employe);
        Role role = getRole(employe);
        validateSubmitAutorise(demande, role);
        validateDates(demande.getDateDebutEmp(), demande.getDateFinEmp());

        if (role == Role.RESPONSABLE) {
            appliquerDatesResponsableSelfAvantSubmit(demande);
            validateDates(demande.getDateDebutResp(), demande.getDateFinResp());
            deduireSoldeSiNecessaire(demande, employe, demande.getDateDebutResp(), demande.getDateFinResp());
            demande.setStatus(StatusDemande.VALIDE_RESPONSABLE);
            DemandeConge saved = demandeCongeRepository.save(demande);
            signatureDemandeService.signerParEmploye(saved, employe);
            signatureDemandeService.signerParResponsable(saved, employe);
            return demandeCongeMapper.toResponse(saved);
        }

        if (role == Role.DIRECTEUR_GENERAL) {
            appliquerDatesDgSelfAvantSubmit(demande);
            validateDates(demande.getDateDebutDg(), demande.getDateFinDg());
            deduireSoldeSiNecessaire(demande, employe, demande.getDateDebutDg(), demande.getDateFinDg());
            demande.setStatus(StatusDemande.VALIDE_DG);
            DemandeConge saved = demandeCongeRepository.save(demande);
            signatureDemandeService.signerParDg(saved, employe);
            return demandeCongeMapper.toResponse(saved);
        }

        deduireSoldeSiNecessaire(demande, employe, demande.getDateDebutEmp(), demande.getDateFinEmp());
        demande.setStatus(StatusDemande.VALIDE_EMPLOYE);
        DemandeConge saved = demandeCongeRepository.save(demande);
        signatureDemandeService.signerParEmploye(saved, employe);
        return demandeCongeMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public DemandeCongeResponse passerEnModification(Long demandeId) {
        Employe employe = employeConnecteProvider.getEmployeConnecte();
        DemandeConge demande = findMaDemande(demandeId, employe);
        Role role = getRole(employe);

        if (role == Role.RESPONSABLE) {
            return passerEnModificationResponsableSelf(demande, employe);
        }
        if (role == Role.DIRECTEUR_GENERAL) {
            return passerEnModificationDgSelf(demande, employe);
        }
        return passerEnModificationEmploye(demande, employe);
    }

    @Override
    @Transactional
    public DemandeCongeResponse passerEnModificationEmploye(Long demandeId) {
        Employe employe = employeConnecteProvider.getEmployeConnecte();
        DemandeConge demande = findMaDemande(demandeId, employe);
        return passerEnModificationEmploye(demande, employe);
    }

    private DemandeCongeResponse passerEnModificationEmploye(DemandeConge demande, Employe employe) {
        if (demande.getStatus() == StatusDemande.VALIDE_RESPONSABLE
                || demande.getStatus() == StatusDemande.VALIDE_DG
                || demande.getStatus() == StatusDemande.MODIFICATION_DG) {
            throw new InvalidBusinessRequestException("Cette demande est deja validee par le directeur general");
        }
        if (demande.getStatus() == StatusDemande.ANNULE
                || demande.getStatus() == StatusDemande.REFUSE_RESPONSABLE
                || demande.getStatus() == StatusDemande.REFUSE_DG) {
            throw new InvalidBusinessRequestException("Cette demande ne peut pas etre modifiee dans son etat actuel");
        }
        if (demande.getStatus() == StatusDemande.VALIDE_EMPLOYE) {
            restaurerSoldeSiNecessaire(demande, employe);
            demande.setStatus(StatusDemande.MODIFICATION_EMPLOYE);
        } else {
            throw new InvalidBusinessRequestException("Cette demande ne peut pas etre modifiee dans son etat actuel");
        }

        return demandeCongeMapper.toResponse(demandeCongeRepository.save(demande));
    }

    private DemandeCongeResponse passerEnModificationResponsableSelf(DemandeConge demande, Employe responsable) {
        if (demande.getStatus() == StatusDemande.VALIDE_DG) {
            throw new InvalidBusinessRequestException("Cette demande est deja validee par le directeur general");
        }
        if (demande.getStatus() != StatusDemande.VALIDE_RESPONSABLE) {
            throw new InvalidBusinessRequestException("Cette demande ne peut pas etre modifiee dans son etat actuel");
        }

        restaurerSoldeSiNecessaire(demande, responsable);
        demande.setStatus(StatusDemande.MODIFICATION_RESPONSABLE);
        return demandeCongeMapper.toResponse(demandeCongeRepository.save(demande));
    }

    private DemandeCongeResponse passerEnModificationDgSelf(DemandeConge demande, Employe directeurGeneral) {
        if (demande.getStatus() != StatusDemande.VALIDE_DG) {
            throw new InvalidBusinessRequestException("Cette demande ne peut pas etre modifiee dans son etat actuel");
        }

        restaurerSoldeSiNecessaire(demande, directeurGeneral);
        demande.setStatus(StatusDemande.MODIFICATION_DG);
        return demandeCongeMapper.toResponse(demandeCongeRepository.save(demande));
    }

    @Override
    @Transactional
    public DemandeCongeResponse annulerDemande(Long demandeId) {
        Employe employe = employeConnecteProvider.getEmployeConnecte();
        DemandeConge demande = findMaDemande(demandeId, employe);
        validateAnnulationAutorisee(demande, getRole(employe));

        restaurerSoldeSiNecessaire(demande, employe);
        demande.setStatus(StatusDemande.ANNULE);
        return demandeCongeMapper.toResponse(demandeCongeRepository.save(demande));
    }

    @Override
    public List<DemandeCongeResponse> mesDemandes() {
        Employe employe = employeConnecteProvider.getEmployeConnecte();
        return demandeCongeRepository.findByEmployeIdOrderByCreatedAtDesc(employe.getIdEmp()).stream()
                .map(demandeCongeMapper::toResponse)
                .toList();
    }

    @Override
    public DemandeCongeResponse getMaDemande(Long demandeId) {
        Employe employe = employeConnecteProvider.getEmployeConnecte();
        return demandeCongeMapper.toResponse(findMaDemande(demandeId, employe));
    }

    @Override
    public SoldeCongeResponse getSoldeCourant() {
        return soldeCongeService.getSoldeCourant();
    }

    private DemandeConge findMaDemande(Long demandeId, Employe employe) {
        return demandeCongeRepository.findByIdAndEmployeId(demandeId, employe.getIdEmp())
                .orElseThrow(() -> new ResourceNotFoundException("Demande introuvable"));
    }

    private void validateDates(LocalDate dateDebut, LocalDate dateFin) {
        if (dateDebut == null || dateFin == null) {
            throw new InvalidBusinessRequestException("Les dates de debut et de fin sont obligatoires");
        }
        if (dateDebut.isAfter(dateFin)) {
            throw new InvalidBusinessRequestException("Dates invalides");
        }
        if (dateDebut.getYear() != dateFin.getYear()) {
            throw new InvalidBusinessRequestException("Dates invalides");
        }
    }

    private void validateCreateRequest(DemandeCongeCreateRequest request) {
        if (request == null) {
            throw new InvalidBusinessRequestException("Requete invalide");
        }
    }

    private void validateUpdateRequest(DemandeCongeUpdateRequest request) {
        if (request == null) {
            throw new InvalidBusinessRequestException("Requete invalide");
        }
    }

    private void validateTypeDemande(TypeDemande typeDemande) {
        if (typeDemande == null) {
            throw new InvalidBusinessRequestException("Le type de demande est obligatoire");
        }
    }

    private void appliquerDatesEffectivesApresModification(DemandeConge demande, Role role) {
        if (role == Role.RESPONSABLE && demande.getStatus() == StatusDemande.MODIFICATION_RESPONSABLE) {
            demande.setDateDebutResp(demande.getDateDebutEmp());
            demande.setDateFinResp(demande.getDateFinEmp());
        }
        if (role == Role.DIRECTEUR_GENERAL && demande.getStatus() == StatusDemande.MODIFICATION_DG) {
            demande.setDateDebutDg(demande.getDateDebutEmp());
            demande.setDateFinDg(demande.getDateFinEmp());
        }
    }

    private void appliquerDatesResponsableSelfAvantSubmit(DemandeConge demande) {
        if (demande.getStatus() == StatusDemande.BROUILLON
                || demande.getDateDebutResp() == null
                || demande.getDateFinResp() == null) {
            demande.setDateDebutResp(demande.getDateDebutEmp());
            demande.setDateFinResp(demande.getDateFinEmp());
        }
    }

    private void appliquerDatesDgSelfAvantSubmit(DemandeConge demande) {
        if (demande.getStatus() == StatusDemande.BROUILLON
                || demande.getDateDebutDg() == null
                || demande.getDateFinDg() == null) {
            demande.setDateDebutDg(demande.getDateDebutEmp());
            demande.setDateFinDg(demande.getDateFinEmp());
        }
    }

    private void validateSubmitAutorise(DemandeConge demande, Role role) {
        if (role == Role.RESPONSABLE
                && (demande.getStatus() == StatusDemande.BROUILLON
                || demande.getStatus() == StatusDemande.MODIFICATION_RESPONSABLE)) {
            return;
        }
        if (role == Role.DIRECTEUR_GENERAL
                && (demande.getStatus() == StatusDemande.BROUILLON
                || demande.getStatus() == StatusDemande.MODIFICATION_DG)) {
            return;
        }
        if ((role == Role.EMPLOYE || role == Role.RH)
                && (demande.getStatus() == StatusDemande.BROUILLON
                || demande.getStatus() == StatusDemande.MODIFICATION_EMPLOYE)) {
            return;
        }
        throw new InvalidBusinessRequestException("Cette demande ne peut pas etre modifiee dans son etat actuel");
    }

    private void validateModificationAutorisee(DemandeConge demande, Role role) {
        if (role == Role.RESPONSABLE
                && (demande.getStatus() == StatusDemande.BROUILLON
                || demande.getStatus() == StatusDemande.MODIFICATION_RESPONSABLE)) {
            return;
        }
        if (role == Role.DIRECTEUR_GENERAL
                && (demande.getStatus() == StatusDemande.BROUILLON
                || demande.getStatus() == StatusDemande.MODIFICATION_DG)) {
            return;
        }
        if ((role == Role.EMPLOYE || role == Role.RH) && STATUTS_MODIFICATION_DIRECTE.contains(demande.getStatus())) {
            return;
        }
        if (demande.getStatus() == StatusDemande.VALIDE_RESPONSABLE
                || demande.getStatus() == StatusDemande.VALIDE_DG
                || demande.getStatus() == StatusDemande.MODIFICATION_DG) {
            throw new InvalidBusinessRequestException("Cette demande est deja validee par le directeur general");
        }
        if (demande.getStatus() == StatusDemande.VALIDE_EMPLOYE) {
            throw new InvalidBusinessRequestException("Passez d'abord la demande en modification employe");
        }
        throw new InvalidBusinessRequestException("Cette demande ne peut pas etre modifiee dans son etat actuel");
    }

    private void validateAnnulationAutorisee(DemandeConge demande, Role role) {
        if (role == Role.DIRECTEUR_GENERAL) {
            LocalDate dateDebutEffective = demande.getDateDebutDg() != null
                    ? demande.getDateDebutDg()
                    : demande.getDateDebutEmp();
            if (!LocalDate.now().isBefore(dateDebutEffective)) {
                throw new InvalidBusinessRequestException("Le conge a deja commence, annulation impossible.");
            }
        } else if (demande.getStatus() == StatusDemande.VALIDE_DG) {
            throw new InvalidBusinessRequestException("Cette demande est deja validee par le directeur general");
        }
        if (role != Role.DIRECTEUR_GENERAL && demande.getStatus() == StatusDemande.MODIFICATION_DG) {
            throw new InvalidBusinessRequestException("Cette demande ne peut pas etre modifiee dans son etat actuel");
        }
        if (demande.getStatus() == StatusDemande.REFUSE_RESPONSABLE
                || demande.getStatus() == StatusDemande.REFUSE_DG) {
            throw new InvalidBusinessRequestException("Cette demande ne peut pas etre modifiee dans son etat actuel");
        }
        if (demande.getStatus() == StatusDemande.ANNULE) {
            throw new InvalidBusinessRequestException("Cette demande ne peut pas etre modifiee dans son etat actuel");
        }
    }

    private void deduireSoldeSiNecessaire(
            DemandeConge demande,
            Employe employe,
            LocalDate dateDebut,
            LocalDate dateFin) {
        if (demande.getTypeDemande() != TypeDemande.CONGE) {
            demande.setJoursDeduits(0D);
            return;
        }

        restaurerSoldeSiNecessaire(demande, employe);
        double jours = soldeCongeService.calculerJoursCongeOuvres(dateDebut, dateFin);
        soldeCongeService.deduireSolde(employe.getIdEmp(), dateDebut.getYear(), jours);
        demande.setJoursDeduits(jours);
    }

    private void restaurerSoldeSiNecessaire(DemandeConge demande, Employe employe) {
        if (demande.getTypeDemande() == TypeDemande.CONGE && demande.getJoursDeduits() != null && demande.getJoursDeduits() > 0D) {
            int annee = getAnneeSolde(demande);
            demande.setJoursDeduits(0D);
            demandeCongeRepository.saveAndFlush(demande);
            soldeCongeService.getOrCreateSolde(employe.getIdEmp(), annee);
        }
    }

    private Role getRole(Employe employe) {
        if (employe.getUtilisateur() == null || employe.getUtilisateur().getRole() == null) {
            throw new InvalidBusinessRequestException("Utilisateur connecte sans Employe lie");
        }
        return employe.getUtilisateur().getRole();
    }

    private int getAnneeSolde(DemandeConge demande) {
        if (demande.getDateDebutDg() != null) {
            return demande.getDateDebutDg().getYear();
        }
        if (demande.getDateDebutResp() != null) {
            return demande.getDateDebutResp().getYear();
        }
        return demande.getDateDebutEmp().getYear();
    }
}
