import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import {
  DeclarationAbsenceForm,
  RhDeclarationAbsencesComponent,
} from '../declaration-absences/declaration-absences.component';

interface Absence {
  numero: number;
  periode: string;
  duree: number;
  motif: string;
}

@Component({
  selector: 'app-rh-mes-absences',
  standalone: true,
  imports: [CommonModule, RhDeclarationAbsencesComponent],
  templateUrl: './mes-absences.component.html',
  styleUrls: ['./mes-absences.component.scss'],
})
export class RhMesAbsencesComponent {
  isModalOpen = false;

  absences: Absence[] = [
    {
      numero: 901,
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
    return Math.max(...this.absences.map(absence => absence.numero), 900) + 1;
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
