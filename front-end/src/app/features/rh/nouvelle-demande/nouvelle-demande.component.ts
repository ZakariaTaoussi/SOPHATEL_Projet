import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-rh-nouvelle-demande',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './nouvelle-demande.component.html',
  styleUrls: ['./nouvelle-demande.component.scss'],
})
export class RhNouvelleDemandeComponent {
  typesDemande = ['Congé', 'Rattrapage'];

  form = {
    type: 'Congé',
    demandeur: 'Samar Haddad',
    dateDebut: '',
    dateFin: '',
    motif: '',
  };

  joursFeries = [
    { nom: 'Fête du Travail', date: '01 mai 2026' },
    { nom: 'Fête du Trône', date: '30 juil. 2026' },
    { nom: 'Oued Ed-Dahab', date: '14 août 2026' },
    { nom: 'Marche Verte', date: '06 nov. 2026' },
  ];

  readonly soldeActuel = 16;
  readonly soldeTotal = 22;

  get periode() {
    if (!this.form.dateDebut || !this.form.dateFin) return '-';
    return `${this.formatDate(this.form.dateDebut)} - ${this.formatDate(this.form.dateFin)}`;
  }

  get joursOuvres() {
    if (!this.form.dateDebut || !this.form.dateFin) return null;
    const debut = new Date(this.form.dateDebut);
    const fin = new Date(this.form.dateFin);
    let total = 0;
    for (const date = new Date(debut); date <= fin; date.setDate(date.getDate() + 1)) {
      const day = date.getDay();
      if (day !== 0 && day !== 6) total++;
    }
    return Math.max(total, 0);
  }

  get soldeApres() {
    if (this.form.type !== 'Congé' || this.joursOuvres === null) return '-';
    return `${Math.max(this.soldeActuel - this.joursOuvres, 0)} / ${this.soldeTotal} j`;
  }

  enregistrerBrouillon() {}
  soumettreDemande() {}

  private formatDate(value: string) {
    return new Intl.DateTimeFormat('fr-FR', {
      day: '2-digit',
      month: 'short',
      year: 'numeric',
    }).format(new Date(value));
  }
}
