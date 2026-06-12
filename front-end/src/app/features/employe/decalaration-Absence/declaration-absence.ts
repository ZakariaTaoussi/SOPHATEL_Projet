import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Output } from '@angular/core';
import { FormsModule } from '@angular/forms';

export interface DeclarationAbsenceForm {
  dateDebut: string;
  dateFin: string;
  motif: string;
  justificatif: File | null;
}

@Component({
  selector: 'app-declaration-absence',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './declaration-absence.html',
  styleUrls: ['./declaration-absence.scss'],
})
export class DeclarationAbsenceComponent {
  @Output() closed = new EventEmitter<void>();
  @Output() saved = new EventEmitter<DeclarationAbsenceForm>();

  nouvelleAbsence: DeclarationAbsenceForm = this.getEmptyAbsence();

  fermerModal() {
    this.closed.emit();
  }

  choisirJustificatif(event: Event) {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0] ?? null;
    this.nouvelleAbsence.justificatif = file;
  }

  deposerJustificatif(event: DragEvent) {
    event.preventDefault();
    const file = event.dataTransfer?.files?.[0] ?? null;
    this.nouvelleAbsence.justificatif = file;
  }

  enregistrerAbsence() {
    if (!this.nouvelleAbsence.dateDebut || !this.nouvelleAbsence.dateFin || !this.nouvelleAbsence.motif.trim()) {
      return;
    }

    this.saved.emit({
      ...this.nouvelleAbsence,
      motif: this.nouvelleAbsence.motif.trim(),
    });

    this.nouvelleAbsence = this.getEmptyAbsence();
  }

  private getEmptyAbsence(): DeclarationAbsenceForm {
    return {
      dateDebut: '',
      dateFin: '',
      motif: '',
      justificatif: null,
    };
  }
}
