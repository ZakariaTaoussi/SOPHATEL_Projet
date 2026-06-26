package com.example.backend.service.impl;

import com.example.backend.dto.demande.DirecteurGeneralDemandeResponse;
import com.example.backend.dto.demande.DirecteurGeneralValidationDemandeRequest;
import com.example.backend.exception.InvalidBusinessRequestException;
import com.example.backend.exception.ResourceNotFoundException;
import com.example.backend.mapper.DemandeCongeMapper;
import com.example.backend.model.DemandeConge;
import com.example.backend.model.Employe;
import com.example.backend.model.enums.Role;
import com.example.backend.model.enums.StatusDemande;
import com.example.backend.model.enums.TypeDemande;
import com.example.backend.repository.DemandeCongeRepository;
import com.example.backend.service.interfaces.IDirecteurGeneralDemandeService;
import com.example.backend.service.interfaces.ISignatureDemandeService;
import com.example.backend.service.interfaces.ISoldeCongeService;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DirecteurGeneralDemandeServiceImpl implements IDirecteurGeneralDemandeService {

    private final DemandeCongeRepository demandeCongeRepository;
    private final DemandeCongeMapper demandeCongeMapper;
    private final ISoldeCongeService soldeCongeService;
    private final ISignatureDemandeService signatureDemandeService;
    private final EmployeConnecteProvider employeConnecteProvider;

    public DirecteurGeneralDemandeServiceImpl(
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
    @Transactional(readOnly = true)
    public List<DirecteurGeneralDemandeResponse> getDemandesAValider() {
        return demandeCongeRepository.findByStatusOrderByUpdatedAtDesc(StatusDemande.VALIDE_RESPONSABLE).stream()
                .map(demandeCongeMapper::toDirecteurGeneralResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DirecteurGeneralDemandeResponse> getAbsencesAValider() {
        return demandeCongeRepository.findByTypeDemandeAndStatusOrderByUpdatedAtDesc(
                        TypeDemande.ABSENCE,
                        StatusDemande.VALIDE_RESPONSABLE).stream()
                .map(demandeCongeMapper::toDirecteurGeneralResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DirecteurGeneralDemandeResponse> getDemandesValidees() {
        return demandeCongeRepository.findByStatusInOrderByUpdatedAtDesc(Set.of(
                        StatusDemande.VALIDE_DG,
                        StatusDemande.REFUSE_DG,
                        StatusDemande.MODIFICATION_DG)).stream()
                .map(demandeCongeMapper::toDirecteurGeneralResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public DirecteurGeneralDemandeResponse getDemandeAValiderById(Long demandeId) {
        DemandeConge demande = findDemande(demandeId);
        if (demande.getStatus() != StatusDemande.VALIDE_RESPONSABLE) {
            throw new ResourceNotFoundException("Demande a valider introuvable");
        }
        return demandeCongeMapper.toDirecteurGeneralResponse(demande);
    }

    @Override
    @Transactional
    public DirecteurGeneralDemandeResponse validerDemandeParDg(
            Long demandeId,
            DirecteurGeneralValidationDemandeRequest request) {
        Employe directeurGeneral = getDirecteurGeneralConnecte();
        DemandeConge demande = findDemande(demandeId);
        validateStatutValidable(demande);
        validateDatesResponsableDisponibles(demande);

        retirerImpactDemande(demande);
        appliquerDatesDg(demande, request);
        appliquerImpactFinal(demande);

        demande.setStatus(StatusDemande.VALIDE_DG);
        DemandeConge saved = demandeCongeRepository.save(demande);
        signatureDemandeService.signerParDg(saved, directeurGeneral);
        return demandeCongeMapper.toDirecteurGeneralResponse(saved);
    }

    @Override
    @Transactional
    public DirecteurGeneralDemandeResponse refuserDemandeParDg(Long demandeId) {
        getDirecteurGeneralConnecte();
        DemandeConge demande = findDemande(demandeId);
        validateStatutRefusable(demande);

        retirerImpactDemande(demande);
        demande.setStatus(StatusDemande.REFUSE_DG);
        return demandeCongeMapper.toDirecteurGeneralResponse(demandeCongeRepository.save(demande));
    }

    @Override
    @Transactional
    public DirecteurGeneralDemandeResponse passerEnModificationDg(Long demandeId) {
        getDirecteurGeneralConnecte();
        DemandeConge demande = findDemande(demandeId);
        if (demande.getStatus() != StatusDemande.VALIDE_DG) {
            throw new InvalidBusinessRequestException("La modification DG est autorisee uniquement apres validation DG");
        }

        retirerImpactDemande(demande);
        demande.setStatus(StatusDemande.MODIFICATION_DG);
        return demandeCongeMapper.toDirecteurGeneralResponse(demandeCongeRepository.save(demande));
    }

    private Employe getDirecteurGeneralConnecte() {
        Employe directeurGeneral = employeConnecteProvider.getEmployeConnecte();
        if (directeurGeneral.getUtilisateur() == null
                || directeurGeneral.getUtilisateur().getRole() != Role.DIRECTEUR_GENERAL) {
            throw new InvalidBusinessRequestException("L'utilisateur connecte n'a pas le role DIRECTEUR_GENERAL");
        }
        return directeurGeneral;
    }

    private DemandeConge findDemande(Long demandeId) {
        return demandeCongeRepository.findById(demandeId)
                .orElseThrow(() -> new ResourceNotFoundException("Demande introuvable"));
    }

    private void validateStatutValidable(DemandeConge demande) {
        if (demande.getStatus() == StatusDemande.VALIDE_RESPONSABLE
                || demande.getStatus() == StatusDemande.MODIFICATION_DG) {
            return;
        }
        if (demande.getStatus() == StatusDemande.MODIFICATION_RESPONSABLE) {
            throw new InvalidBusinessRequestException("Cette demande est en modification responsable");
        }
        if (demande.getStatus() == StatusDemande.VALIDE_EMPLOYE) {
            throw new InvalidBusinessRequestException("Cette demande n'est pas encore validee par le responsable");
        }
        if (demande.getStatus() == StatusDemande.ANNULE) {
            throw new InvalidBusinessRequestException("Impossible de valider une demande annulee");
        }
        if (demande.getStatus() == StatusDemande.REFUSE_RESPONSABLE
                || demande.getStatus() == StatusDemande.REFUSE_DG) {
            throw new InvalidBusinessRequestException("Impossible de valider une demande refusee");
        }
        if (demande.getStatus() == StatusDemande.VALIDE_DG) {
            throw new InvalidBusinessRequestException("Cette demande est deja validee par le directeur general");
        }
        throw new InvalidBusinessRequestException("Cette demande n'est pas dans un etat validable par le directeur general");
    }

    private void validateStatutRefusable(DemandeConge demande) {
        if (demande.getStatus() == StatusDemande.VALIDE_RESPONSABLE
                || demande.getStatus() == StatusDemande.MODIFICATION_DG) {
            return;
        }
        throw new InvalidBusinessRequestException("Cette demande ne peut pas etre refusee par le directeur general.");
    }

    private void validateDatesResponsableDisponibles(DemandeConge demande) {
        if (demande.getDateDebutResp() == null || demande.getDateFinResp() == null) {
            throw new InvalidBusinessRequestException("Les dates responsable doivent exister avant validation DG");
        }
    }

    private void appliquerDatesDg(DemandeConge demande, DirecteurGeneralValidationDemandeRequest request) {
        LocalDate dateDebutDg = request == null ? null : request.getDateDebutDg();
        LocalDate dateFinDg = request == null ? null : request.getDateFinDg();

        if (dateDebutDg == null && dateFinDg == null) {
            demande.setDateDebutDg(demande.getDateDebutResp());
            demande.setDateFinDg(demande.getDateFinResp());
            return;
        }
        if (dateDebutDg == null || dateFinDg == null) {
            throw new InvalidBusinessRequestException("Les deux dates DG doivent etre renseignees");
        }

        validateDates(dateDebutDg, dateFinDg);
        demande.setDateDebutDg(dateDebutDg);
        demande.setDateFinDg(dateFinDg);
    }

    private void appliquerImpactFinal(DemandeConge demande) {
        if (demande.getTypeDemande() == TypeDemande.ABSENCE) {
            demande.setJoursDeduits(soldeCongeService.calculerJoursOuvres(
                    demande.getDateDebutDg(),
                    demande.getDateFinDg()));
            return;
        }

        double jours = soldeCongeService.calculerJoursOuvres(
                demande.getDateDebutDg(),
                demande.getDateFinDg());
        soldeCongeService.deduireSolde(demande.getEmploye().getIdEmp(), demande.getDateDebutDg().getYear(), jours);
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
        if (demande.getDateDebutDg() != null) {
            return demande.getDateDebutDg().getYear();
        }
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
            throw new InvalidBusinessRequestException("La date de debut ne peut pas etre apres la date de fin");
        }
        if (dateDebut.getYear() != dateFin.getYear()) {
            throw new InvalidBusinessRequestException("Une demande ne peut pas traverser deux annees differentes");
        }
    }
}
