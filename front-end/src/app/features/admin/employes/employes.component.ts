import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';

interface Employe {
  id: number;
  matricule: string;
  nom: string;
  email: string;
  role: string;
  departement: string;
  statut: 'Actif' | 'Suspendu';
}

type EmployeForm = Omit<Employe, 'id'>;

@Component({
  selector: 'app-admin-employes',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './employes.component.html',
  styleUrls: ['./employes.component.scss'],
})
export class AdminEmployesComponent {
  searchTerm = '';
  editingId?: number;

  departements = ['Informatique', 'Finance', 'Commercial', 'Ressources Humaines', 'Marketing'];
  roles = ['Employe', 'Responsable', 'RH', 'Directeur General', 'Administrateur'];

  employes: Employe[] = [
    { id: 1, matricule: 'EMP-0042', nom: 'Ahmed Benali', email: 'ahmed.benali@demo.ma', role: 'Employe', departement: 'Informatique', statut: 'Actif' },
    { id: 2, matricule: 'EMP-0057', nom: 'Lina Mansouri', email: 'lina.mansouri@demo.ma', role: 'Responsable', departement: 'Finance', statut: 'Actif' },
    { id: 3, matricule: 'EMP-0024', nom: 'Nadia El Fassi', email: 'nadia.elfassi@demo.ma', role: 'RH', departement: 'Ressources Humaines', statut: 'Actif' },
  ];

  form: EmployeForm = this.emptyForm();

  get employesFiltres(): Employe[] {
    const term = this.searchTerm.trim().toLowerCase();
    return this.employes.filter(employe => !term || employe.nom.toLowerCase().includes(term));
  }

  enregistrer(): void {
    if (!this.form.nom.trim() || !this.form.email.trim()) {
      return;
    }

    if (this.editingId) {
      const employe = this.employes.find(item => item.id === this.editingId);
      if (employe) {
        Object.assign(employe, this.form);
      }
    } else {
      this.employes = [...this.employes, { ...this.form, id: Date.now() }];
    }

    this.reinitialiser();
  }

  modifier(employe: Employe): void {
    this.editingId = employe.id;
    this.form = { matricule: employe.matricule, nom: employe.nom, email: employe.email, role: employe.role, departement: employe.departement, statut: employe.statut };
  }

  supprimer(employe: Employe): void {
    this.employes = this.employes.filter(item => item.id !== employe.id);
    if (this.editingId === employe.id) {
      this.reinitialiser();
    }
  }

  reinitialiser(): void {
    this.editingId = undefined;
    this.form = this.emptyForm();
  }

  private emptyForm(): EmployeForm {
    return { matricule: '', nom: '', email: '', role: 'Employe', departement: this.departements[0], statut: 'Actif' };
  }
}
