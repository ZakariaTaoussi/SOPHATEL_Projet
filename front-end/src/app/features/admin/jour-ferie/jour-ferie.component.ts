import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';

interface JourFerie {
  id: number;
  nom: string;
  dateDebut: string;
  dateFin: string;
  annee: number;
}

@Component({
  selector: 'app-admin-jour-ferie',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './jour-ferie.component.html',
  styleUrls: ['./jour-ferie.component.scss'],
})
export class AdminJourFerieComponent {
  editingId?: number;
  form = this.emptyForm();
  joursFeries: JourFerie[] = [
    { id: 1, nom: 'Fete du Travail', dateDebut: '2026-05-01', dateFin: '2026-05-01', annee: 2026 },
    { id: 2, nom: 'Fete du Trone', dateDebut: '2026-07-30', dateFin: '2026-07-31', annee: 2026 },
    { id: 3, nom: 'Marche Verte', dateDebut: '2026-11-06', dateFin: '2026-11-06', annee: 2026 },
  ];

  get joursTries(): JourFerie[] {
    return [...this.joursFeries].sort((a, b) => a.dateDebut.localeCompare(b.dateDebut));
  }

  enregistrer(): void {
    if (!this.form.nom.trim() || !this.form.dateDebut || !this.form.dateFin) return;
    if (new Date(this.form.dateFin) < new Date(this.form.dateDebut)) return;

    const value = { ...this.form, annee: new Date(this.form.dateDebut).getFullYear() };

    if (this.editingId) {
      const jour = this.joursFeries.find(item => item.id === this.editingId);
      if (jour) Object.assign(jour, value);
    } else {
      this.joursFeries = [...this.joursFeries, { ...value, id: Date.now() }];
    }
    this.reinitialiser();
  }

  modifier(jour: JourFerie): void {
    this.editingId = jour.id;
    this.form = { nom: jour.nom, dateDebut: jour.dateDebut, dateFin: jour.dateFin, annee: jour.annee };
  }

  supprimer(jour: JourFerie): void {
    this.joursFeries = this.joursFeries.filter(item => item.id !== jour.id);
    if (this.editingId === jour.id) this.reinitialiser();
  }

  reinitialiser(): void {
    this.editingId = undefined;
    this.form = this.emptyForm();
  }

  formatDate(date: string): string {
    return new Intl.DateTimeFormat('fr-FR', { day: '2-digit', month: 'long', year: 'numeric' }).format(new Date(date));
  }

  getDuree(jour: Pick<JourFerie, 'dateDebut' | 'dateFin'>): number {
    const debut = new Date(jour.dateDebut);
    const fin = new Date(jour.dateFin);
    return Math.max(Math.floor((fin.getTime() - debut.getTime()) / 86_400_000) + 1, 1);
  }

  private emptyForm(): Omit<JourFerie, 'id'> {
    return { nom: '', dateDebut: '', dateFin: '', annee: new Date().getFullYear() };
  }
}
