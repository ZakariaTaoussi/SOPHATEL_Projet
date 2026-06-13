package com.example.backend.service;

import com.example.backend.dto.auth.AuthUserResponse;

public interface IAuthentificationService {
    AuthUserResponse login(String email, String password);

    void logout();

    AuthUserResponse getCurrentUser();
}
