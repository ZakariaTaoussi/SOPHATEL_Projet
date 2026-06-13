package com.example.backend.config;

import com.example.backend.model.Admin;
import com.example.backend.model.Employe;
import com.example.backend.model.Utilisateur;
import com.example.backend.model.enums.Role;
import com.example.backend.model.enums.StatutEmploye;
import com.example.backend.repository.AdminRepository;
import com.example.backend.repository.EmployeRepository;
import com.example.backend.repository.UtilisateurRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initUsers(
            UtilisateurRepository utilisateurRepository,
            EmployeRepository employeRepository,
            AdminRepository adminRepository,
            PasswordEncoder passwordEncoder) {
        return args -> {
            createAdmin(utilisateurRepository, adminRepository, passwordEncoder);
            createEmploye("employe@sophatel.com", "EMP-0001", "Benali", "Ahmed", Role.EMPLOYE, utilisateurRepository, employeRepository, passwordEncoder);
            createEmploye("rh@sophatel.com", "RH-0001", "Haddad", "Samar", Role.RH, utilisateurRepository, employeRepository, passwordEncoder);
            createEmploye("responsable@sophatel.com", "RESP-0001", "Ouni", "Karim", Role.RESPONSABLE, utilisateurRepository, employeRepository, passwordEncoder);
            createEmploye("dg@sophatel.com", "DG-0001", "General", "Directeur", Role.DIRECTEUR_GENERAL, utilisateurRepository, employeRepository, passwordEncoder);
        };
    }

    private void createAdmin(
            UtilisateurRepository utilisateurRepository,
            AdminRepository adminRepository,
            PasswordEncoder passwordEncoder) {
        String email = "admin@sophatel.com";
        if (utilisateurRepository.existsByEmail(email)) {
            return;
        }

        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setEmail(email);
        utilisateur.setPassword(passwordEncoder.encode("admin123"));
        utilisateur.setRole(Role.ADMINISTRATEUR);
        Utilisateur saved = utilisateurRepository.save(utilisateur);

        Admin admin = new Admin();
        admin.setUtilisateur(saved);
        adminRepository.save(admin);
    }

    private void createEmploye(
            String email,
            String matricule,
            String nom,
            String prenom,
            Role role,
            UtilisateurRepository utilisateurRepository,
            EmployeRepository employeRepository,
            PasswordEncoder passwordEncoder) {
        if (utilisateurRepository.existsByEmail(email)) {
            return;
        }

        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setEmail(email);
        utilisateur.setPassword(passwordEncoder.encode("password123"));
        utilisateur.setRole(role);
        Utilisateur saved = utilisateurRepository.save(utilisateur);

        Employe employe = new Employe();
        employe.setMatricule(matricule);
        employe.setUtilisateur(saved);
        employe.setNom(nom);
        employe.setPrenom(prenom);
        employe.setStatut(StatutEmploye.ACTIF);
        employeRepository.save(employe);
    }
}
