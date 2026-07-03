package com.example.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.backend.dto.profil.ProfilResponse;
import com.example.backend.dto.profil.ProfilUpdateRequest;
import com.example.backend.exception.InvalidBusinessRequestException;
import com.example.backend.model.Departement;
import com.example.backend.model.Employe;
import com.example.backend.model.SoldeConge;
import com.example.backend.model.Utilisateur;
import com.example.backend.model.enums.Role;
import com.example.backend.repository.EmployeRepository;
import com.example.backend.repository.UtilisateurRepository;
import com.example.backend.service.impl.ProfilServiceImpl;
import com.example.backend.service.interfaces.ISoldeCongeService;
import com.example.backend.testutil.TestFixtures;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class ProfilServiceImplTest {

    @Mock
    private UtilisateurRepository utilisateurRepository;

    @Mock
    private EmployeRepository employeRepository;

    @Mock
    private ISoldeCongeService soldeCongeService;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getProfilConnecteReturnsEmployeeProfileWithDepartmentManagerAndBalance() {
        Utilisateur utilisateur = TestFixtures.utilisateur(1L, "employee@example.test", Role.EMPLOYE);
        Employe employe = TestFixtures.employe(10L, Role.EMPLOYE);
        employe.setUtilisateur(utilisateur);
        employe.setCreatedAt(LocalDateTime.of(2025, 1, 15, 8, 0));
        Departement departement = TestFixtures.departement(4L, "Informatique");
        employe.setDepartement(departement);
        Employe manager = TestFixtures.employe(20L, Role.RESPONSABLE);
        manager.setPrenom("Sarah");
        manager.setNom("Responsable");
        SoldeConge solde = TestFixtures.solde(employe, LocalDate.now().getYear(), 12D, 18D);

        authenticateAs("employee@example.test");
        when(utilisateurRepository.findByEmail("employee@example.test")).thenReturn(Optional.of(utilisateur));
        when(employeRepository.findByUtilisateurId(1L)).thenReturn(Optional.of(employe));
        when(employeRepository.findFirstByDepartementIdAndUtilisateurRole(4L, Role.RESPONSABLE))
                .thenReturn(Optional.of(manager));
        when(soldeCongeService.getOrCreateSolde(10L, LocalDate.now().getYear())).thenReturn(solde);

        ProfilResponse response = service().getProfilConnecte();

        assertThat(response.getUtilisateurId()).isEqualTo(1L);
        assertThat(response.getEmployeId()).isEqualTo(10L);
        assertThat(response.getDepartementNom()).isEqualTo("Informatique");
        assertThat(response.getManagerId()).isEqualTo(20L);
        assertThat(response.getManagerNomComplet()).isEqualTo("Sarah Responsable");
        assertThat(response.getSoldeActuel()).isEqualTo(12D);
        assertThat(response.getSoldeTotal()).isEqualTo(18D);
    }

    @Test
    void getProfilConnecteRejectsAdministratorProfile() {
        authenticateAs("admin@example.test");
        when(utilisateurRepository.findByEmail("admin@example.test"))
                .thenReturn(Optional.of(TestFixtures.utilisateur(99L, "admin@example.test", Role.ADMINISTRATEUR)));

        assertThatThrownBy(() -> service().getProfilConnecte())
                .isInstanceOf(InvalidBusinessRequestException.class)
                .hasMessage("Le profil employe n'est pas disponible pour l'administrateur");
    }

    @Test
    void updateProfilConnecteTrimsNamesAndPersistsEmployee() {
        Utilisateur utilisateur = TestFixtures.utilisateur(1L, "employee@example.test", Role.EMPLOYE);
        Employe employe = TestFixtures.employe(10L, Role.EMPLOYE);
        employe.setUtilisateur(utilisateur);
        SoldeConge solde = TestFixtures.solde(employe, LocalDate.now().getYear(), 10D, 18D);
        ProfilUpdateRequest request = new ProfilUpdateRequest();
        request.setNom("  UpdatedNom  ");
        request.setPrenom("  UpdatedPrenom  ");

        authenticateAs("employee@example.test");
        when(utilisateurRepository.findByEmail("employee@example.test")).thenReturn(Optional.of(utilisateur));
        when(employeRepository.findByUtilisateurId(1L)).thenReturn(Optional.of(employe));
        when(employeRepository.save(employe)).thenReturn(employe);
        when(soldeCongeService.getOrCreateSolde(10L, LocalDate.now().getYear())).thenReturn(solde);

        ProfilResponse response = service().updateProfilConnecte(request);

        assertThat(response.getNom()).isEqualTo("UpdatedNom");
        assertThat(response.getPrenom()).isEqualTo("UpdatedPrenom");
        verify(employeRepository).save(employe);
    }

    @Test
    void updateProfilConnecteRejectsBlankName() {
        authenticateAs("employee@example.test");
        Utilisateur utilisateur = TestFixtures.utilisateur(1L, "employee@example.test", Role.EMPLOYE);
        when(utilisateurRepository.findByEmail("employee@example.test")).thenReturn(Optional.of(utilisateur));
        when(employeRepository.findByUtilisateurId(1L)).thenReturn(Optional.of(TestFixtures.employe(10L, Role.EMPLOYE)));
        ProfilUpdateRequest request = new ProfilUpdateRequest();
        request.setNom(" ");
        request.setPrenom("Prenom");

        assertThatThrownBy(() -> service().updateProfilConnecte(request))
                .isInstanceOf(InvalidBusinessRequestException.class)
                .hasMessage("Nom obligatoire");
    }

    private ProfilServiceImpl service() {
        return new ProfilServiceImpl(utilisateurRepository, employeRepository, soldeCongeService);
    }

    private void authenticateAs(String email) {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(email, null, List.of()));
    }
}
