package com.example.backend.service.impl;

import com.example.backend.model.DemandeConge;
import com.example.backend.model.Employe;
import com.example.backend.model.SignatureDemande;
import com.example.backend.repository.SignatureDemandeRepository;
import com.example.backend.service.interfaces.ISignatureDemandeService;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SignatureDemandeServiceImpl implements ISignatureDemandeService {

    private final SignatureDemandeRepository signatureDemandeRepository;

    public SignatureDemandeServiceImpl(SignatureDemandeRepository signatureDemandeRepository) {
        this.signatureDemandeRepository = signatureDemandeRepository;
    }

    @Override
    @Transactional
    public void signerParEmploye(DemandeConge demande, Employe employe) {
        SignatureDemande signature = signatureDemandeRepository.findByDemandeId(demande.getId())
                .orElseGet(SignatureDemande::new);
        signature.setDemande(demande);
        signature.setEmploye(employe);
        signature.setDateSignatureEmp(LocalDateTime.now());
        signatureDemandeRepository.save(signature);
    }

    @Override
    @Transactional
    public void signerParResponsable(DemandeConge demande, Employe responsable) {
        SignatureDemande signature = signatureDemandeRepository.findByDemandeId(demande.getId())
                .orElseGet(SignatureDemande::new);
        signature.setDemande(demande);
        signature.setResponsable(responsable);
        signature.setDateSignatureResp(LocalDateTime.now());
        signatureDemandeRepository.save(signature);
    }
}
