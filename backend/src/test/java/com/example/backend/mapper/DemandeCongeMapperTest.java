package com.example.backend.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.backend.dto.demande.DemandeCongeCreateRequest;
import com.example.backend.dto.demande.DemandeCongeResponse;
import com.example.backend.dto.demande.DemandeCongeUpdateRequest;
import com.example.backend.dto.demande.DirecteurGeneralDemandeResponse;
import com.example.backend.dto.demande.ResponsableDemandeResponse;
import com.example.backend.model.Departement;
import com.example.backend.model.DemandeConge;
import com.example.backend.model.Employe;
import com.example.backend.model.enums.NatureConge;
import com.example.backend.model.enums.Role;
import com.example.backend.model.enums.StatusDemande;
import com.example.backend.model.enums.TypeDemande;
import com.example.backend.testutil.TestFixtures;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class DemandeCongeMapperTest {

    private final DemandeCongeMapper mapper = new DemandeCongeMapper();

    @Test
    void toEntityCreatesDraftFromCreateRequest() {
        Employe employe = TestFixtures.employe(10L, Role.EMPLOYE);
        DemandeCongeCreateRequest request = new DemandeCongeCreateRequest();
        request.setDateDebutEmp(LocalDate.of(2026, 7, 6));
        request.setDateFinEmp(LocalDate.of(2026, 7, 7));
        request.setTypeDemande(TypeDemande.CONGE);
        request.setNatureConge(NatureConge.ANNUEL);

        DemandeConge demande = mapper.toEntity(request, employe);

        assertThat(demande.getEmploye()).isSameAs(employe);
        assertThat(demande.getStatus()).isEqualTo(StatusDemande.BROUILLON);
        assertThat(demande.getJoursDeduits()).isZero();
        assertThat(demande.getNatureConge()).isEqualTo(NatureConge.ANNUEL);
    }

    @Test
    void updateEntityCopiesMutableFields() {
        DemandeConge demande = TestFixtures.demande(
                1L,
                TestFixtures.employe(10L, Role.EMPLOYE),
                TypeDemande.CONGE,
                StatusDemande.BROUILLON,
                LocalDate.of(2026, 7, 6),
                LocalDate.of(2026, 7, 7));
        DemandeCongeUpdateRequest request = new DemandeCongeUpdateRequest();
        request.setDateDebutEmp(LocalDate.of(2026, 8, 3));
        request.setDateFinEmp(LocalDate.of(2026, 8, 4));
        request.setTypeDemande(TypeDemande.ABSENCE);
        request.setNatureConge(null);

        mapper.updateEntity(demande, request);

        assertThat(demande.getDateDebutEmp()).isEqualTo(LocalDate.of(2026, 8, 3));
        assertThat(demande.getDateFinEmp()).isEqualTo(LocalDate.of(2026, 8, 4));
        assertThat(demande.getTypeDemande()).isEqualTo(TypeDemande.ABSENCE);
        assertThat(demande.getNatureConge()).isNull();
    }

    @Test
    void toResponseMapsSelfDemandFields() {
        DemandeConge demande = TestFixtures.demande(
                1L,
                TestFixtures.employe(10L, Role.EMPLOYE),
                TypeDemande.CONGE,
                StatusDemande.BROUILLON,
                LocalDate.of(2026, 7, 6),
                LocalDate.of(2026, 7, 7));

        DemandeCongeResponse response = mapper.toResponse(demande);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getEmployeNomComplet()).isEqualTo("Prenom Nom");
        assertThat(response.getTypeDemande()).isEqualTo(TypeDemande.CONGE);
        assertThat(response.getStatus()).isEqualTo(StatusDemande.BROUILLON);
    }

    @Test
    void toResponsableResponseIncludesDepartment() {
        Employe employe = TestFixtures.employe(10L, Role.EMPLOYE);
        employe.setDepartement(TestFixtures.departement(3L, "RH"));
        DemandeConge demande = TestFixtures.demande(
                1L, employe, TypeDemande.CONGE, StatusDemande.VALIDE_EMPLOYE,
                LocalDate.of(2026, 7, 6), LocalDate.of(2026, 7, 7));

        ResponsableDemandeResponse response = mapper.toResponsableResponse(demande);

        assertThat(response.getDepartementId()).isEqualTo(3L);
        assertThat(response.getDepartementNom()).isEqualTo("RH");
    }

    @Test
    void toDirecteurGeneralResponseIncludesResponsableName() {
        Employe employe = TestFixtures.employe(10L, Role.EMPLOYE);
        Employe responsable = TestFixtures.employe(20L, Role.RESPONSABLE);
        responsable.setPrenom("Sara");
        responsable.setNom("Manager");
        Departement departement = TestFixtures.departement(3L, "RH");
        departement.setResponsable(responsable);
        employe.setDepartement(departement);
        DemandeConge demande = TestFixtures.demande(
                1L, employe, TypeDemande.CONGE, StatusDemande.VALIDE_RESPONSABLE,
                LocalDate.of(2026, 7, 6), LocalDate.of(2026, 7, 7));

        DirecteurGeneralDemandeResponse response = mapper.toDirecteurGeneralResponse(demande);

        assertThat(response.getResponsableNomComplet()).isEqualTo("Sara Manager");
        assertThat(response.getDepartementNom()).isEqualTo("RH");
    }
}
