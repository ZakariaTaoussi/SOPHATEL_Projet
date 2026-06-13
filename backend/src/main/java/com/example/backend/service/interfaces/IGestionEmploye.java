package com.example.backend.service.interfaces;

import com.example.backend.dto.admin.CreateEmployeRequest;
import com.example.backend.dto.admin.EmployeResponse;
import com.example.backend.dto.admin.PageResponse;
import com.example.backend.dto.admin.UpdateEmployeRequest;
import org.springframework.data.domain.Pageable;

public interface IGestionEmploye {
    EmployeResponse creerEmploye(CreateEmployeRequest request);

    EmployeResponse modifierEmploye(Long id, UpdateEmployeRequest request);

    void supprimerEmploye(Long id);

    EmployeResponse consulterEmploye(Long id);

    PageResponse<EmployeResponse> consulterEmployes(String search, Pageable pageable);
}
