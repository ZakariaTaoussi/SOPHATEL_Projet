package com.example.backend.service.interfaces;

import com.example.backend.dto.common.PageResponse;
import com.example.backend.dto.directeurgeneral.DirecteurGeneralEmployeResponse;

public interface IDirecteurGeneralEmployeService {
    PageResponse<DirecteurGeneralEmployeResponse> getEmployes(
            int page,
            int size,
            String search,
            String role,
            Long departementId);
}
