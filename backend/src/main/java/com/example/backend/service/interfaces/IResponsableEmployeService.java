package com.example.backend.service.interfaces;

import com.example.backend.dto.responsable.ResponsableEmployeResponse;
import java.util.List;

public interface IResponsableEmployeService {
    List<ResponsableEmployeResponse> getMesEmployes();

    ResponsableEmployeResponse getEmployeDeMonDepartement(Long employeId);
}
