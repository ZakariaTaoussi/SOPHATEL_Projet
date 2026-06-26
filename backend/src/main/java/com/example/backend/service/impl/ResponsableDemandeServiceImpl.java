package com.example.backend.service.impl;

import com.example.backend.dto.demande.ResponsableDemandeResponse;
import com.example.backend.dto.demande.ResponsableValidationDemandeRequest;
import com.example.backend.exception.InvalidBusinessRequestException;
import com.example.backend.exception.ResourceNotFoundException;
import com.example.backend.mapper.DemandeCongeMapper;
import com.example.backend.model.Departement;
import com.example.backend.model.DemandeConge;
import com.example.backend.model.Employe;
import com.example.backend.model.enums.Role;
import com.example.backend.model.enums.StatusDemande;
import com.example.backend.model.enums.TypeDemande;
import com.example.backend.repository.DemandeCongeRepository;
import com.example.backend.service.interfaces.IResponsableDemandeService;
import com.example.backend.service.interfaces.ISignatureDemandeService;
import com.example.backend.service.interfaces.ISoldeCongeService;
import java.time.LocalDate;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ResponsableDemandeServiceImpl implements IResponsableDemandeService {

    private static final Set<StatusDemande> STATUTS_WORKFLOW_RESPONSABLE = EnumSet.of(
            StatusDemande.VALIDE_EMPLOYE,
            StatusDemande.VALIDE_RESPONSABLE,
            StatusDemande.MODIFICATION_RESPONSABLE);

    private static final Set<StatusDemande> STATUTS_TRAITES_RESPONSABLE = EnumSet.of(
            StatusDemande.VALIDE_RESPONSABLE,
            StatusDemande.REFUSE_RESPONSABLE,
            StatusDemande.MODIFICATION_RESPONSABLE);

    private final DemandeCongeRepository demandeCongeRepository;
    private final DemandeCongeMapper demandeCongeMapper;
    private final ISoldeCongeService soldeCongeService;
    private final ISignatureDemandeService signatureDemandeService;
    private final EmployeConnecteProvider employeConnecteProvider;

    public ResponsableDemandeServiceImpl(
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
    public List<ResponsableDemandeResponse> getDemandesAValider() {
        Employe responsable = getResponsableConnecte();
        Departement departement = getDepartementResponsableConnecte(responsable);
        return demandeCongeRepository.findByEmployeDepartementIdAndEmployeIdNotAndStatusInOrderByUpdatedAtDesc(
                        departement.getId(),
                        responsable.getIdEmp(),
                        STATUTS_WORKFLOW_RESPONSABLE).stream()
                .map(demandeCongeMapper::toResponsableResponse)
                .toList();
    }

    @Override
    public List<ResponsableDemandeResponse> getAbsencesAValider() {
        Employe responsable = getResponsableConnecte();
        Departement departement = getDepartementResponsableConnecte(responsable);
        return demandeCongeRepository.findByEmployeDepartementIdAndEmployeIdNotAndTypeDemandeAndStatusInOrderByUpdatedAtDesc(
                        departement.getId(),
                        responsable.getIdEmp(),
                        TypeDemande.ABSENCE,
                        STATUTS_WORKFLOW_RESPONSABLE).stream()
                .map(demandeCongeMapper::toResponsableResponse)
                .toList();
    }

    @Override
    public List<ResponsableDemandeResponse> getDemandesValidees() {
        Employe responsable = getResponsableConnecte();
        Departement departement = getDepartementResponsableConnecte(responsable);
        return demandeCongeRepository.findByEmployeDepartementIdAndEmployeIdNotAndStatusInOrderByUpdatedAtDesc(
                        departement.getId(),
                        responsable.getIdEmp(),
                        STATUTS_TRAITES_RESPONSABLE).stream()
                .map(demandeCongeMapper::toResponsableResponse)
                .toList();
    }

    @Override
    public ResponsableDemandeResponse getDemandeAValiderById(Long demandeId) {
        Employe responsable = getResponsableConnecte();
        Departement departement = getDepartementResponsableConnecte(responsable);
        DemandeConge demande = findDemandeDuDepartement(demandeId, departement, responsable.getIdEmp());
        if (!STATUTS_WORKFLOW_RESPONSABLE.contains(demande.getStatus())) {
            throw new ResourceNotFoundException("Demande responsable introuvable");
        }
        return demandeCongeMapper.toResponsableResponse(demande);
    }

    @Override
    @Transactional
    public ResponsableDemandeResponse validerDemandeParResponsable(
            Long demandeId,
            ResponsableValidationDemandeRequest request) {
        Employe responsable = getResponsableConnecte();
        Departement departement = getDepartementResponsableConnecte(responsable);
        DemandeConge demande = findDemandeDuDepartement(demandeId, departement, responsable.getIdEmp());

        if (demande.getStatus() == StatusDemande.VALIDE_RESPONSABLE) {
            return demandeCongeMapper.toResponsableResponse(demande);
        }
        if (demande.getStatus() == StatusDemande.VALIDE_DG) {
            throw new InvalidBusinessRequestException("Cette demande a deja ete validee par le directeur general");
        }
        if (demande.getStatus() == StatusDemande.ANNULE) {
            throw new InvalidBusinessRequestException("Impossible de valider une demande annulee");
        }
        if (demande.getStatus() != StatusDemande.VALIDE_EMPLOYE
                && demande.getStatus() != StatusDemande.MODIFICATION_RESPONSABLE) {
            throw new InvalidBusinessRequestException("Cette demande n'est pas dans un etat validable par le responsable");
        }

        appliquerDatesResponsable(demande, request);
        appliquerImpactResponsable(demande);
        demande.setStatus(StatusDemande.VALIDE_RESPONSABLE);
        DemandeConge saved = demandeCongeRepository.save(demande);
        signatureDemandeService.signerParResponsable(saved, responsable);
        return demandeCongeMapper.toResponsableResponse(saved);
    }

    @Override
    @Transactional
    public ResponsableDemandeResponse refuserDemandeParResponsable(Long demandeId) {
        Employe responsable = getResponsableConnecte();
        Departement departement = getDepartementResponsableConnecte(responsable);
        DemandeConge demande = findDemandeDuDepartement(demandeId, departement, responsable.getIdEmp());

        if (demande.getStatus() != StatusDemande.VALIDE_EMPLOYE
                && demande.getStatus() != StatusDemande.MODIFICATION_RESPONSABLE) {
            throw new InvalidBusinessRequestException("Cette demande ne peut pas etre refusee par le responsable.");
        }

        retirerImpactDemande(demande);
        demande.setStatus(StatusDemande.REFUSE_RESPONSABLE);
        return demandeCongeMapper.toResponsableResponse(demandeCongeRepository.save(demande));
    }

    @Override
    @Transactional
    public ResponsableDemandeResponse passerEnModificationResponsable(Long demandeId) {
        Employe responsable = getResponsableConnecte();
        Departement departement = getDepartementResponsableConnecte(responsable);
        DemandeConge demande = findDemandeDuDepartement(demandeId, departement, responsable.getIdEmp());

        if (demande.getStatus() == StatusDemande.VALIDE_DG
                || demande.getStatus() == StatusDemande.MODIFICATION_DG) {
            throw new InvalidBusinessRequestException("Le responsable ne peut plus modifier une demande validee par le directeur general");
        }
        if (demande.getStatus() == StatusDemande.ANNULE
                || demande.getStatus() == StatusDemande.REFUSE_RESPONSABLE
                || demande.getStatus() == StatusDemande.REFUSE_DG) {
            throw new InvalidBusinessRequestException("Cette demande ne peut plus etre modifiee par le responsable");
        }
        if (demande.getStatus() != StatusDemande.VALIDE_RESPONSABLE) {
            throw new InvalidBusinessRequestException("La modification responsable est autorisee uniquement apres validation responsable");
        }

        retirerImpactDemande(demande);
        demande.setStatus(StatusDemande.MODIFICATION_RESPONSABLE);
        return demandeCongeMapper.toResponsableResponse(demandeCongeRepository.save(demande));
    }

    private Employe getResponsableConnecte() {
        Employe responsable = employeConnecteProvider.getEmployeConnecte();
        if (responsable.getUtilisateur() == null || responsable.getUtilisateur().getRole() != Role.RESPONSABLE) {
            throw new InvalidBusinessRequestException("L'utilisateur connecte n'a pas le role RESPONSABLE");
        }
        if (responsable.getDepartement() == null) {
            throw new InvalidBusinessRequestException("Le responsable connecte n'est rattache a aucun departement");
        }
        return responsable;
    }

    private Departement getDepartementResponsableConnecte() {
        return getDepartementResponsableConnecte(getResponsableConnecte());
    }

    private Departement getDepartementResponsableConnecte(Employe responsable) {
        return responsable.getDepartement();
    }

    private DemandeConge findDemandeDuDepartement(Long demandeId, Departement departement, Long responsableId) {
        DemandeConge demande = demandeCongeRepository.findById(demandeId)
                .orElseThrow(() -> new ResourceNotFoundException("Demande introuvable"));
        if (demande.getEmploye() == null
                || demande.getEmploye().getDepartement() == null
                || !departement.getId().equals(demande.getEmploye().getDepartement().getId())) {
            throw new InvalidBusinessRequestException("Vous n'avez pas le droit d'acceder a cette demande");
        }
        if (demande.getEmploye().getIdEmp().equals(responsableId)) {
            throw new InvalidBusinessRequestException("Vous n'avez pas le droit d'acceder a cette demande");
        }
        return demande;
    }

    private void appliquerDatesResponsable(DemandeConge demande, ResponsableValidationDemandeRequest request) {
        LocalDate dateDebutResp = request == null ? null : request.getDateDebutResp();
        LocalDate dateFinResp = request == null ? null : request.getDateFinResp();

        if (dateDebutResp == null && dateFinResp == null) {
            demande.setDateDebutResp(demande.getDateDebutEmp());
            demande.setDateFinResp(demande.getDateFinEmp());
            return;
        }
        if (dateDebutResp == null || dateFinResp == null) {
            throw new InvalidBusinessRequestException("Les deux dates responsable doivent etre renseignees");
        }

        validateDates(dateDebutResp, dateFinResp);
        demande.setDateDebutResp(dateDebutResp);
        demande.setDateFinResp(dateFinResp);
    }

    private void appliquerImpactResponsable(DemandeConge demande) {
        if (demande.getTypeDemande() == TypeDemande.ABSENCE) {
            demande.setJoursDeduits(soldeCongeService.calculerJoursOuvres(
                    demande.getDateDebutResp(),
                    demande.getDateFinResp()));
            return;
        }

        retirerImpactDemande(demande);
        double jours = soldeCongeService.calculerJoursOuvres(
                demande.getDateDebutResp(),
                demande.getDateFinResp());
        soldeCongeService.deduireSolde(demande.getEmploye().getIdEmp(), demande.getDateDebutResp().getYear(), jours);
        demande.setJoursDeduits(jours);
    }

    private void retirerImpactDemande(DemandeConge demande) {
        if (demande.getTypeDemande() == TypeDemande.CONGE
                && demande.getJoursDeduits() != null
                && demande.getJoursDeduits() > 0D) {
            int annee = getAnneeSolde(demande);
            demande.setJoursDeduits(0D);
            demandeCongeRepository.saveAndFlush(demande);
            soldeCongeService.getOrCreateSolde(demande.getEmploye().getIdEmp(), annee);
            return;
        }
        if (demande.getTypeDemande() == TypeDemande.ABSENCE) {
            demande.setJoursDeduits(0D);
        }
    }

    private int getAnneeSolde(DemandeConge demande) {
        if (demande.getDateDebutResp() != null) {
            return demande.getDateDebutResp().getYear();
        }
        return demande.getDateDebutEmp().getYear();
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
}
