import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-directeur-general-profil',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './profil.component.html',
  styleUrls: ['./profil.component.scss'],
})
export class DirecteurGeneralProfilComponent {
  editMode = false;

  profil = {
    prenom: 'Directeur',
    nom: 'Général',
    email: 'direction.generale@corp.ma',
    telephone: '+212 5 22 00 00 00',
    poste: 'Directeur Général',
    departement: 'Direction Générale',
    dateEmbauche: '01/01/2020',
    matricule: 'DG-0001',
    manager: 'Conseil d’administration',
    avatar: 'DG',
  };

  pct(s: number, t: number) {
    return Math.round((s / t) * 100);
  }
}
