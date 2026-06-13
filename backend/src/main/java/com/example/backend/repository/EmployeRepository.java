package com.example.backend.repository;

import com.example.backend.model.Employe;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeRepository extends JpaRepository<Employe, Long> {
    Optional<Employe> findByUserId(Long userId);
}
