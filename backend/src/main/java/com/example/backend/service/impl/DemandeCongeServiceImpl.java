package com.example.backend.service.impl;

import com.example.backend.dto.demande.DemandeCongeCreateRequest;
import com.example.backend.dto.demande.DemandeCongeResponse;
import com.example.backend.dto.demande.DemandeCongeUpdateRequest;
import com.example.backend.exception.InvalidBusinessRequestException;
import com.example.backend.exception.ResourceNotFoundException;
import com.example.backend.mapper.DemandeCongeMapper;
import com.example.backend.model.DemandeConge;
import com.example.backend.model.Employe;
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
        validateDates(request.getDateDebutEmp(), request.getDateFinEmp());
        validateTypeDemande(request.getTypeDemande());
        validateModificationAutorisee(demande);

        demandeCongeMapper.updateEntity(demande, request);
        return demandeCongeMapper.toResponse(demandeCongeRepository.save(demande));
    }

    @Override
    @Transactional
    public DemandeCongeResponse submitDemande(Long demandeId) {
        Employe employe = employeConnecteProvider.getEmployeConnecte();
        DemandeConge demande = findMaDemande(demandeId, employe);
        validateSubmitAutorise(demande);
        validateDates(demande.getDateDebutEmp(), demande.getDateFinEmp());

        if (demande.getTypeDemande() == TypeDemande.CONGE) {
            double jours = soldeCongeService.calculerJoursCongeOuvres(
                    demande.getDateDebutEmp(),
                    demande.getDateFinEmp());
            soldeCongeService.deduireSolde(employe.getIdEmp(), demande.getDateDebutEmp().getYear(), jours);
            demande.setJoursDeduits(jours);
        } else {
            demande.setJoursDeduits(0D);
        }

        demande.setStatus(StatusDemande.VALIDE_EMPLOYE);
        DemandeConge saved = demandeCongeRepository.save(demande);
        signatureDemandeService.signerParEmploye(saved, employe);
        return demandeCongeMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public DemandeCongeResponse passerEnModificationEmploye(Long demandeId) {
        Employe employe = employeConnecteProvider.getEmployeConnecte();
        DemandeConge demande = findMaDemande(demandeId, employe);

        if (demande.getStatus() == StatusDemande.VALIDE_RESPONSABLE || demande.getStatus() == StatusDemande.VALIDE_DG) {
            throw new InvalidBusinessRequestException(
                    "Cette demande a deja ete validee par le responsable ou le directeur general, modification impossible.");
        }
        if (demande.getStatus() == StatusDemande.ANNULE || demande.getStatus() == StatusDemande.REFUSE) {
            throw new InvalidBusinessRequestException("Cette demande ne peut plus etre modifiee.");
        }
        if (demande.getStatus() == StatusDemande.VALIDE_EMPLOYE) {
            restaurerSoldeSiNecessaire(demande, employe);
            demande.setStatus(StatusDemande.MODIFICATION_EMPLOYE);
        }

        return demandeCongeMapper.toResponse(demandeCongeRepository.save(demande));
    }

    @Override
    @Transactional
    public DemandeCongeResponse annulerDemande(Long demandeId) {
        Employe employe = employeConnecteProvider.getEmployeConnecte();
        DemandeConge demande = findMaDemande(demandeId, employe);

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
    public List<DemandeCongeResponse> demandesAValiderPourResponsable() {
        Employe responsable = employeConnecteProvider.getEmployeConnecte();
        return demandeCongeRepository.findDemandesVisiblesForResponsable(responsable.getIdEmp()).stream()
                .map(demandeCongeMapper::toResponse)
                .toList();
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
            throw new InvalidBusinessRequestException("La date de debut ne peut pas etre apres la date de fin");
        }
        if (dateDebut.getYear() != dateFin.getYear()) {
            throw new InvalidBusinessRequestException("Une demande ne peut pas traverser deux annees differentes");
        }
    }

    private void validateTypeDemande(TypeDemande typeDemande) {
        if (typeDemande == null) {
            throw new InvalidBusinessRequestException("Le type de demande est obligatoire");
        }
    }

    private void validateSubmitAutorise(DemandeConge demande) {
        if (demande.getStatus() != StatusDemande.BROUILLON
                && demande.getStatus() != StatusDemande.MODIFICATION_EMPLOYE) {
            throw new InvalidBusinessRequestException("Cette demande ne peut pas etre soumise dans son statut actuel");
        }
    }

    private void validateModificationAutorisee(DemandeConge demande) {
        if (STATUTS_MODIFICATION_DIRECTE.contains(demande.getStatus())) {
            return;
        }
        if (demande.getStatus() == StatusDemande.VALIDE_RESPONSABLE || demande.getStatus() == StatusDemande.VALIDE_DG) {
            throw new InvalidBusinessRequestException(
                    "Cette demande a deja ete validee par le responsable ou le directeur general, modification impossible.");
        }
        if (demande.getStatus() == StatusDemande.VALIDE_EMPLOYE) {
            throw new InvalidBusinessRequestException("Passez d'abord la demande en modification employe");
        }
        throw new InvalidBusinessRequestException("Cette demande ne peut pas etre modifiee dans son statut actuel");
    }

    private void restaurerSoldeSiNecessaire(DemandeConge demande, Employe employe) {
        if (demande.getTypeDemande() == TypeDemande.CONGE && demande.getJoursDeduits() != null && demande.getJoursDeduits() > 0D) {
            soldeCongeService.restaurerSolde(
                    employe.getIdEmp(),
                    demande.getDateDebutEmp().getYear(),
                    demande.getJoursDeduits());
            demande.setJoursDeduits(0D);
        }
    }
}
