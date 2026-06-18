package com.example.backend.service.interfaces;

import com.example.backend.dto.profil.ProfilResponse;
import com.example.backend.dto.profil.ProfilUpdateRequest;

public interface IProfilService {
    ProfilResponse getProfilConnecte();

    ProfilResponse updateProfilConnecte(ProfilUpdateRequest request);
}
