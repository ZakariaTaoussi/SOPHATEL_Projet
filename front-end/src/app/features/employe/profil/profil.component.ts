import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-profil',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './profil.component.html',
  styleUrls: ['./profil.component.scss'],
})
export class ProfilComponent {
  editMode = false;

  profil = {
    prenom: 'Ahmed',
    nom: 'Benali',
    email: 'ahmed.benali@corp.ma',
    telephone: '+212 6 12 34 56 78',
    poste: 'Développeur Full-Stack',
    departement: 'Informatique',
    dateEmbauche: '01/03/2022',
    matricule: 'EMP-0042',
    manager: 'Sara El Fassi',
    avatar: 'AB',
  };

 

  pct(s: number, t: number) { return Math.round((s / t) * 100); }
}
