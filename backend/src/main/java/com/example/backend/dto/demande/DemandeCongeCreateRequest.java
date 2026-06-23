package com.example.backend.dto.demande;

import com.example.backend.model.enums.NatureConge;
import com.example.backend.model.enums.TypeDemande;
import java.time.LocalDate;

public class DemandeCongeCreateRequest {
    private LocalDate dateDebutEmp;
    private LocalDate dateFinEmp;
    private TypeDemande typeDemande;
    private NatureConge natureConge;

    public LocalDate getDateDebutEmp() {
        return dateDebutEmp;
    }

    public void setDateDebutEmp(LocalDate dateDebutEmp) {
        this.dateDebutEmp = dateDebutEmp;
    }

    public LocalDate getDateFinEmp() {
        return dateFinEmp;
    }

    public void setDateFinEmp(LocalDate dateFinEmp) {
        this.dateFinEmp = dateFinEmp;
    }

    public TypeDemande getTypeDemande() {
        return typeDemande;
    }

    public void setTypeDemande(TypeDemande typeDemande) {
        this.typeDemande = typeDemande;
    }

    public NatureConge getNatureConge() {
        return natureConge;
    }

    public void setNatureConge(NatureConge natureConge) {
        this.natureConge = natureConge;
    }
}
