import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';

type TypeDemande = 'Congé' | 'Rattrapage';

interface DemandeValidee {
  reference: string;
  employe: string;
  matricule: string;
  departement: string;
  type: TypeDemande;
  dateDepot: string;
  dateDebut: string;
  dateFin: string;
  duree: number;
  annee: number;
  statut: 'Validée DG';
}

interface AbsenceEmploye {
  reference: string;
  employe: string;
  departement: string;
  dateDebut: string;
  dateFin: string;
  duree: number;
  motif: string;
  justificatif: string;
}

@Component({
  selector: 'app-rh-employes',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './employes.component.html',
  styleUrls: ['./employes.component.scss'],
})
export class RhEmployesComponent {
  selectedYear = 'Toutes';
  selectedType = 'Tous';
  selectedDepartement = 'Tous';
  searchTerm = '';
  demandeAImprimer?: DemandeValidee;

  demandes: DemandeValidee[] = [
    { reference: 'DG-2026-041', employe: 'Ahmed Benali', matricule: 'EMP-0042', departement: 'Informatique', type: 'Congé', dateDepot: '12/04/2026', dateDebut: '22/04/2026', dateFin: '26/04/2026', duree: 5, annee: 2026, statut: 'Validée DG' },
    { reference: 'DG-2026-038', employe: 'Lina Mansouri', matricule: 'EMP-0057', departement: 'Finance', type: 'Rattrapage', dateDepot: '08/04/2026', dateDebut: '15/04/2026', dateFin: '15/04/2026', duree: 1, annee: 2026, statut: 'Validée DG' },
    { reference: 'DG-2026-029', employe: 'Youssef Idrissi', matricule: 'EMP-0031', departement: 'Commercial', type: 'Congé', dateDepot: '21/03/2026', dateDebut: '02/05/2026', dateFin: '09/05/2026', duree: 6, annee: 2026, statut: 'Validée DG' },
    { reference: 'DG-2025-118', employe: 'Nadia El Fassi', matricule: 'EMP-0024', departement: 'Ressources Humaines', type: 'Congé', dateDepot: '10/11/2025', dateDebut: '22/12/2025', dateFin: '26/12/2025', duree: 5, annee: 2025, statut: 'Validée DG' },
    { reference: 'DG-2025-097', employe: 'Karim Alaoui', matricule: 'EMP-0018', departement: 'Informatique', type: 'Rattrapage', dateDepot: '05/09/2025', dateDebut: '12/09/2025', dateFin: '12/09/2025', duree: 1, annee: 2025, statut: 'Validée DG' },
  ];

  absencesEmployes: AbsenceEmploye[] = [
    { reference: 'ABS-2026-014', employe: 'Youssef Idrissi', departement: 'Commercial', dateDebut: '13/05/2026', dateFin: '13/05/2026', duree: 1, motif: 'Maladie', justificatif: 'certificat-youssef.pdf' },
    { reference: 'ABS-2026-012', employe: 'Nadia El Fassi', departement: 'Ressources Humaines', dateDebut: '10/05/2026', dateFin: '11/05/2026', duree: 2, motif: 'Événement familial', justificatif: 'justificatif-nadia.png' },
    { reference: 'ABS-2026-009', employe: 'Karim Alaoui', departement: 'Informatique', dateDebut: '02/05/2026', dateFin: '02/05/2026', duree: 1, motif: 'Rendez-vous médical', justificatif: 'rdv-medical.pdf' },
  ];

  get annees(): Array<number | 'Toutes'> {
    return ['Toutes', ...Array.from(new Set(this.demandes.map(demande => demande.annee))).sort((a, b) => b - a)];
  }

  get departements(): string[] {
    return ['Tous', ...Array.from(new Set(this.demandes.map(demande => demande.departement))).sort()];
  }

  get types(): string[] {
    return ['Tous', 'Congé', 'Rattrapage'];
  }

  get demandesFiltrees(): DemandeValidee[] {
    const normalizedSearch = this.searchTerm.trim().toLowerCase();

    return this.demandes.filter(demande => {
      const matchYear = this.selectedYear === 'Toutes' || demande.annee === Number(this.selectedYear);
      const matchType = this.selectedType === 'Tous' || demande.type === this.selectedType;
      const matchDepartement = this.selectedDepartement === 'Tous' || demande.departement === this.selectedDepartement;
      const matchSearch = !normalizedSearch || demande.employe.toLowerCase().includes(normalizedSearch);

      return matchYear && matchType && matchDepartement && matchSearch;
    });
  }

  get absencesFiltrees(): AbsenceEmploye[] {
    const normalizedSearch = this.searchTerm.trim().toLowerCase();

    return this.absencesEmployes.filter(absence => {
      const matchDepartement = this.selectedDepartement === 'Tous' || absence.departement === this.selectedDepartement;
      const matchSearch = !normalizedSearch || absence.employe.toLowerCase().includes(normalizedSearch);

      return matchDepartement && matchSearch;
    });
  }

  imprimerDemande(demande: DemandeValidee): void {
    this.demandeAImprimer = demande;
    setTimeout(() => window.print());
  }

  exporterExcel(): void {
    const header = ['Reference', 'Employe', 'Matricule', 'Departement', 'Type', 'Depot', 'Debut', 'Fin', 'Duree', 'Statut'];
    const rows = this.demandesFiltrees.map(demande => [
      demande.reference,
      demande.employe,
      demande.matricule,
      demande.departement,
      demande.type,
      demande.dateDepot,
      demande.dateDebut,
      demande.dateFin,
      `${demande.duree}`,
      demande.statut,
    ]);

    const csv = [header, ...rows].map(row => row.map(cell => `"${cell.replace(/"/g, '""')}"`).join(';')).join('\n');
    const blob = new Blob([`\uFEFF${csv}`], { type: 'text/csv;charset=utf-8;' });
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = 'demandes-validees-rh.csv';
    link.click();
    URL.revokeObjectURL(url);
  }
}
