package com.example.backend.service.impl;

import com.example.backend.dto.admin.CreateDepartementRequest;
import com.example.backend.dto.admin.DepartementResponse;
import com.example.backend.dto.admin.UpdateDepartementRequest;
import com.example.backend.exception.BusinessException;
import com.example.backend.model.Departement;
import com.example.backend.model.Employe;
import com.example.backend.repository.DepartementRepository;
import com.example.backend.repository.EmployeRepository;
import com.example.backend.service.interfaces.IGestionDepartement;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GestionDepartementService implements IGestionDepartement {

    private final DepartementRepository departementRepository;
    private final EmployeRepository employeRepository;

    public GestionDepartementService(DepartementRepository departementRepository, EmployeRepository employeRepository) {
        this.departementRepository = departementRepository;
        this.employeRepository = employeRepository;
    }

    @Override
    @Transactional
    public DepartementResponse creerDepartement(CreateDepartementRequest request) {
        String nom = validateNom(request.getNom());
        if (departementRepository.existsByNomIgnoreCase(nom)) {
            throw new BusinessException("Un departement avec ce nom existe deja", HttpStatus.CONFLICT);
        }

        Departement departement = new Departement();
        departement.setNom(nom);
        return toResponse(departementRepository.save(departement));
    }

    @Override
    @Transactional
    public DepartementResponse modifierDepartement(Long id, UpdateDepartementRequest request) {
        Departement departement = findDepartement(id);
        String nom = validateNom(request.getNom());
        if (departementRepository.existsByNomIgnoreCaseAndIdNot(nom, id)) {
            throw new BusinessException("Un departement avec ce nom existe deja", HttpStatus.CONFLICT);
        }

        departement.setNom(nom);
        return toResponse(departementRepository.save(departement));
    }

    @Override
    @Transactional
    public void supprimerDepartement(Long id) {
        Departement departement = findDepartement(id);
        if (employeRepository.existsByDepartementId(id)) {
            throw new BusinessException("Impossible de supprimer un departement contenant des employes", HttpStatus.CONFLICT);
        }
        departementRepository.delete(departement);
    }

    @Override
    public List<DepartementResponse> consulterDepartements() {
        return departementRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public DepartementResponse consulterDepartement(Long id) {
        return toResponse(findDepartement(id));
    }

    private Departement findDepartement(Long id) {
        return departementRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Departement introuvable", HttpStatus.NOT_FOUND));
    }

    private String validateNom(String nom) {
        if (nom == null || nom.isBlank()) {
            throw new BusinessException("Le nom du departement est obligatoire", HttpStatus.BAD_REQUEST);
        }
        return nom.trim();
    }

    private DepartementResponse toResponse(Departement departement) {
        Employe responsable = departement.getResponsable();
        return new DepartementResponse(
                departement.getId(),
                departement.getNom(),
                responsable == null ? null : responsable.getIdEmp(),
                responsable == null ? null : responsable.getPrenom() + " " + responsable.getNom(),
                departement.getEmployes() == null ? 0 : departement.getEmployes().size());
    }
}
