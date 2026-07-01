package com.example.backend.service.interfaces;

public interface IEmailService {

    void sendPasswordResetEmail(String to, String resetLink);
}
