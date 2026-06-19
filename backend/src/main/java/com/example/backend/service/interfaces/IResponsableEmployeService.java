package com.example.backend.service.interfaces;

import com.example.backend.dto.common.PageResponse;
import com.example.backend.dto.responsable.ResponsableEmployeResponse;

public interface IResponsableEmployeService {
    PageResponse<ResponsableEmployeResponse> getMesEmployes(int page, int size);

    ResponsableEmployeResponse getEmployeDeMonDepartement(Long employeId);
}
