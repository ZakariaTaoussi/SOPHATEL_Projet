import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';

interface StatCard {
  label: string;
  value: string;
  detail: string;
}

interface Activity {
  title: string;
  detail: string;
  status: string;
}

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss'],
})
export class AdminDashboardComponent {
  stats: StatCard[] = [
    { label: 'Comptes actifs', value: '128', detail: '114 employes, 9 responsables, 3 RH, 2 DG' },
    { label: 'Departements', value: '7', detail: '2 departements sans responsable affecte' },
    { label: 'Jours feries 2026', value: '12', detail: 'Calendrier annuel configure' },
    { label: 'Regle conge', value: '1.5 j/mois', detail: '18 jours generes sur une annee complete' },
  ];

  activities: Activity[] = [
    { title: 'Nouveau compte cree', detail: 'Sara El Amrani - Marketing', status: 'Compte actif' },
    { title: 'Regle de preavis mise a jour', detail: 'Demande de conge requise 15 jours avant le depart', status: 'Appliquee' },
    { title: 'Jour ferie ajoute', detail: 'Fete du Travail - 01/05/2026', status: 'Calendrier' },
  ];

  health = [
    { label: 'Comptes sans departement', value: 3 },
    { label: 'Departements sans manager', value: 2 },
    { label: 'Profils incomplets', value: 5 },
  ];
}
