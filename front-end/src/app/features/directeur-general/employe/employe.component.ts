import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';

interface AbsenceEmploye {
  reference: string;
  employe: string;
  matricule: string;
  departement: string;
  dateDebut: string;
  dateFin: string;
  duree: number;
  motif: string;
  justificatif: string;
}

@Component({
  selector: 'app-directeur-general-employe',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './employe.component.html',
  styleUrls: ['./employe.component.scss'],
})
export class DirecteurGeneralEmployeComponent {
  searchTerm = '';
  selectedDepartement = 'Tous';

  absences: AbsenceEmploye[] = [
    { reference: 'ABS-2026-014', employe: 'Ahmed Benali', matricule: 'EMP-0042', departement: 'Informatique', dateDebut: '13/05/2026', dateFin: '13/05/2026', duree: 1, motif: 'Maladie', justificatif: 'certificat-ahmed.pdf' },
    { reference: 'ABS-2026-012', employe: 'Lina Mansouri', matricule: 'EMP-0057', departement: 'Finance', dateDebut: '10/05/2026', dateFin: '11/05/2026', duree: 2, motif: 'Événement familial', justificatif: 'justificatif-lina.png' },
    { reference: 'ABS-2026-009', employe: 'Youssef Idrissi', matricule: 'EMP-0031', departement: 'Commercial', dateDebut: '02/05/2026', dateFin: '02/05/2026', duree: 1, motif: 'Rendez-vous médical', justificatif: 'rdv-medical.pdf' },
    { reference: 'ABS-2026-006', employe: 'Nadia El Fassi', matricule: 'EMP-0024', departement: 'Ressources Humaines', dateDebut: '22/04/2026', dateFin: '22/04/2026', duree: 1, motif: 'Maladie', justificatif: 'certificat-nadia.pdf' },
  ];

  get departements(): string[] {
    return ['Tous', ...Array.from(new Set(this.absences.map(absence => absence.departement))).sort()];
  }

  get absencesFiltrees(): AbsenceEmploye[] {
    const search = this.searchTerm.trim().toLowerCase();
    return this.absences.filter(absence => {
      const matchSearch = !search || absence.employe.toLowerCase().includes(search);
      const matchDepartement = this.selectedDepartement === 'Tous' || absence.departement === this.selectedDepartement;
      return matchSearch && matchDepartement;
    });
  }
}
