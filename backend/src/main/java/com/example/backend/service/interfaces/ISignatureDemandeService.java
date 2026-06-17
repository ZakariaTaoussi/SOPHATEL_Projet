package com.example.backend.service.interfaces;

import com.example.backend.model.DemandeConge;
import com.example.backend.model.Employe;

public interface ISignatureDemandeService {
    void signerParEmploye(DemandeConge demande, Employe employe);

    void signerParResponsable(DemandeConge demande, Employe responsable);

    void signerParDg(DemandeConge demande, Employe directeurGeneral);

    void signerParDirecteurGeneral(DemandeConge demande, Employe directeurGeneral);
}
