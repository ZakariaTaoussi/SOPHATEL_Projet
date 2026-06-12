import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-rh-profil',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './profil.component.html',
  styleUrls: ['./profil.component.scss'],
})
export class RhProfilComponent {
  editMode = false;

  profil = {
    prenom: 'Samar',
    nom: 'Haddad',
    email: 'samar.haddad@corp.ma',
    telephone: '+212 6 45 67 89 10',
    poste: 'Responsable RH',
    departement: 'Ressources Humaines',
    dateEmbauche: '15/02/2021',
    matricule: 'RH-0007',
    manager: 'Direction Générale',
    avatar: 'RH',
  };

  pct(s: number, t: number) {
    return Math.round((s / t) * 100);
  }
}
