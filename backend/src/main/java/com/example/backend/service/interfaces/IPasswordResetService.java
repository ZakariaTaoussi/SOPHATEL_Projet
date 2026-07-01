package com.example.backend.service.interfaces;

public interface IPasswordResetService {

    void forgotPassword(String email);

    void resetPassword(String token, String newPassword, String confirmPassword);
}
