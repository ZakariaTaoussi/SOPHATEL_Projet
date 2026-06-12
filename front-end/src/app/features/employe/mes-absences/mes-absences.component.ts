import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import {
  DeclarationAbsenceComponent,
  DeclarationAbsenceForm,
} from '../decalaration-Absence/declaration-absence';

interface Absence {
  numero: number;
  periode: string;
  duree: number;
  motif: string;
}

@Component({
  selector: 'app-mes-absences',
  standalone: true,
  imports: [CommonModule, DeclarationAbsenceComponent],
  templateUrl: './mes-absences.component.html',
  styleUrls: ['./mes-absences.component.scss'],
})
export class MesAbsencesComponent {
  isModalOpen = false;

  absences: Absence[] = [
    {
      numero: 501,
      periode: '12 mars 2026 - 13 mars 2026',
      duree: 2,
      motif: 'Maladie - arrêt médical',
    },
  ];

  ouvrirModal() {
    this.isModalOpen = true;
  }

  fermerModal() {
    this.isModalOpen = false;
  }

  enregistrerAbsence(nouvelleAbsence: DeclarationAbsenceForm) {
    this.absences = [
      ...this.absences,
      {
        numero: this.getNextNumero(),
        periode: `${this.formatDate(nouvelleAbsence.dateDebut)} - ${this.formatDate(nouvelleAbsence.dateFin)}`,
        duree: this.getDuree(nouvelleAbsence.dateDebut, nouvelleAbsence.dateFin),
        motif: nouvelleAbsence.motif,
      },
    ];

    this.fermerModal();
  }

  private getNextNumero() {
    return Math.max(...this.absences.map(absence => absence.numero), 500) + 1;
  }

  private getDuree(dateDebut: string, dateFin: string) {
    const debut = new Date(dateDebut);
    const fin = new Date(dateFin);
    const diff = fin.getTime() - debut.getTime();
    return Math.max(1, Math.round(diff / 86_400_000) + 1);
  }

  private formatDate(value: string) {
    return new Intl.DateTimeFormat('fr-FR', {
      day: 'numeric',
      month: 'long',
      year: 'numeric',
    }).format(new Date(value));
  }
}
