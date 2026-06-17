package com.example.backend.service.interfaces;

import com.example.backend.dto.demande.ResponsableDemandeResponse;
import java.util.List;

public interface IDirecteurGeneralDemandeService {

    List<ResponsableDemandeResponse> getDemandesAValider();
}
