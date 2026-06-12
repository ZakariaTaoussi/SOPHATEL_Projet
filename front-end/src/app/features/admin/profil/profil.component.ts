import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-admin-profil',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './profil.component.html',
  styleUrls: ['./profil.component.scss'],
})
export class AdminProfilComponent {
  editMode = false;
  profil = {
    prenom: 'Admin',
    nom: 'Systeme',
    email: 'admin@demo.ma',
    telephone: '+212 600 000 000',
    poste: 'Administrateur',
    departement: 'Administration',
    matricule: 'ADM-0001',
    dateEmbauche: '01/01/2024',
    manager: 'Direction Generale',
    avatar: 'AD',
  };
}
