import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';

interface Departement {
  id: number;
  nom: string;
  responsable: string;
  effectif: number;
}

@Component({
  selector: 'app-admin-departements',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './departements.component.html',
  styleUrls: ['./departements.component.scss'],
})
export class AdminDepartementsComponent {
  editingId?: number;
  form = this.emptyForm();
  departements: Departement[] = [
    { id: 1, nom: 'Informatique', responsable: 'Ahmed Benali', effectif: 28 },
    { id: 2, nom: 'Finance', responsable: 'Lina Mansouri', effectif: 14 },
    { id: 3, nom: 'Ressources Humaines', responsable: 'Nadia El Fassi', effectif: 8 },
  ];

  enregistrer(): void {
    if (!this.form.nom.trim()) return;

    if (this.editingId) {
      const departement = this.departements.find(item => item.id === this.editingId);
      if (departement) departement.nom = this.form.nom;
    } else {
      this.departements = [...this.departements, { id: Date.now(), nom: this.form.nom, responsable: '', effectif: 0 }];
    }
    this.reinitialiser();
  }

  modifier(departement: Departement): void {
    this.editingId = departement.id;
    this.form = { nom: departement.nom };
  }

  supprimer(departement: Departement): void {
    this.departements = this.departements.filter(item => item.id !== departement.id);
    if (this.editingId === departement.id) this.reinitialiser();
  }

  reinitialiser(): void {
    this.editingId = undefined;
    this.form = this.emptyForm();
  }

  private emptyForm(): { nom: string } {
    return { nom: '' };
  }
}
