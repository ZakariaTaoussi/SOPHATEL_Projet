package com.example.backend.service.interfaces;

import com.example.backend.dto.admin.CreateDepartementRequest;
import com.example.backend.dto.admin.DepartementResponse;
import com.example.backend.dto.admin.UpdateDepartementRequest;
import java.util.List;

public interface IGestionDepartement {
    DepartementResponse creerDepartement(CreateDepartementRequest request);

    DepartementResponse modifierDepartement(Long id, UpdateDepartementRequest request);

    void supprimerDepartement(Long id);

    List<DepartementResponse> consulterDepartements();

    DepartementResponse consulterDepartement(Long id);
}
