import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';

type StatutEquipe = 'En attente' | 'Acceptée' | 'Rejetée';

interface DemandeEquipe {
  id: string;
  employe: string;
  poste: string;
  type: 'Congé' | 'Rattrapage';
  dateDebut: string;
  dateFin: string;
  mois: string;
  statut: StatutEquipe;
}

interface AbsenceEquipe {
  id: string;
  employe: string;
  dateDebut: string;
  dateFin: string;
  motif: string;
}

interface EmployeEquipe {
  nom: string;
  poste: string;
  departement: string;
}

@Component({
  selector: 'app-responsable-mes-employes',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './mes-employes.component.html',
  styleUrls: ['./mes-employes.component.scss'],
})
export class ResponsableMesEmployesComponent {
  searchTerm = '';
  selectedMonth = 'Tous';
  employeHistorique?: EmployeEquipe;

  employes: EmployeEquipe[] = [
    { nom: 'Ahmed Benali', poste: 'Développeur Full-Stack', departement: 'Informatique' },
    { nom: 'Lina Mansouri', poste: 'Analyste QA', departement: 'Informatique' },
    { nom: 'Youssef Idrissi', poste: 'Chef de projet', departement: 'Informatique' },
    { nom: 'Nadia El Fassi', poste: 'UX Designer', departement: 'Informatique' },
  ];

  demandes: DemandeEquipe[] = [
    { id: 'EQ-001', employe: 'Ahmed Benali', poste: 'Développeur Full-Stack', type: 'Congé', dateDebut: '12/06/2026', dateFin: '14/06/2026', mois: 'Juin', statut: 'En attente' },
    { id: 'EQ-002', employe: 'Lina Mansouri', poste: 'Analyste QA', type: 'Rattrapage', dateDebut: '20/06/2026', dateFin: '20/06/2026', mois: 'Juin', statut: 'En attente' },
    { id: 'EQ-003', employe: 'Youssef Idrissi', poste: 'Chef de projet', type: 'Congé', dateDebut: '04/07/2026', dateFin: '08/07/2026', mois: 'Juillet', statut: 'Acceptée' },
  ];

  absences: AbsenceEquipe[] = [
    { id: 'ABS-001', employe: 'Ahmed Benali', dateDebut: '05/05/2026', dateFin: '05/05/2026', motif: 'Maladie' },
    { id: 'ABS-002', employe: 'Ahmed Benali', dateDebut: '17/05/2026', dateFin: '17/05/2026', motif: 'Rendez-vous médical' },
    { id: 'ABS-003', employe: 'Lina Mansouri', dateDebut: '09/05/2026', dateFin: '10/05/2026', motif: 'Événement familial' },
    { id: 'ABS-004', employe: 'Nadia El Fassi', dateDebut: '12/04/2026', dateFin: '12/04/2026', motif: 'Maladie' },
  ];

  months = ['Tous', 'Janvier', 'Février', 'Mars', 'Avril', 'Mai', 'Juin', 'Juillet', 'Août', 'Septembre', 'Octobre', 'Novembre', 'Décembre'];

  get demandesFiltrees(): DemandeEquipe[] {
    const search = this.searchTerm.trim().toLowerCase();
    return this.demandes.filter(demande => {
      const matchSearch = !search || demande.employe.toLowerCase().includes(search);
      const matchMonth = this.selectedMonth === 'Tous' || demande.mois === this.selectedMonth;
      return matchSearch && matchMonth;
    });
  }

  get employesFiltres(): EmployeEquipe[] {
    const search = this.searchTerm.trim().toLowerCase();
    return this.employes.filter(employe => !search || employe.nom.toLowerCase().includes(search));
  }

  accepter(demande: DemandeEquipe) {
    demande.statut = 'Acceptée';
  }

  rejeter(demande: DemandeEquipe) {
    demande.statut = 'Rejetée';
  }

  compterAbsences(employe: string): number {
    return this.absences.filter(absence => absence.employe === employe).length;
  }

  historiqueAbsences(employe: string): AbsenceEquipe[] {
    return this.absences.filter(absence => absence.employe === employe);
  }

  ouvrirHistorique(employe: EmployeEquipe) {
    this.employeHistorique = employe;
  }

  fermerHistorique() {
    this.employeHistorique = undefined;
  }
}
