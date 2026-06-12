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
  selector: 'app-rh-declaration-absences',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './declaration-absences.component.html',
  styleUrls: ['./declaration-absences.component.scss'],
})
export class RhDeclarationAbsencesComponent {
  @Output() closed = new EventEmitter<void>();
  @Output() saved = new EventEmitter<DeclarationAbsenceForm>();

  nouvelleAbsence: DeclarationAbsenceForm = this.getEmptyAbsence();

  fermerModal() {
    this.closed.emit();
  }

  choisirJustificatif(event: Event) {
    const input = event.target as HTMLInputElement;
    this.nouvelleAbsence.justificatif = input.files?.[0] ?? null;
  }

  deposerJustificatif(event: DragEvent) {
    event.preventDefault();
    this.nouvelleAbsence.justificatif = event.dataTransfer?.files?.[0] ?? null;
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
