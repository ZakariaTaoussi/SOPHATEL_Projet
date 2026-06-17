package com.example.backend.repository;

import com.example.backend.model.SignatureDemande;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SignatureDemandeRepository extends JpaRepository<SignatureDemande, Long> {
    Optional<SignatureDemande> findByDemandeId(Long demandeId);
}
