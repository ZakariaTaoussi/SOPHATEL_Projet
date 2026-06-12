import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-admin-regle',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './regle.component.html',
  styleUrls: ['./regle.component.scss'],
})
export class AdminRegleComponent {
  joursParMois = 1.5;
  preavisConge = 15;
  maxJoursConsecutifs = 20;
  reportAnnuel = 5;
  regleValidee = false;

  get totalAnnuel(): number {
    return this.joursParMois * 12;
  }

  get projection() {
    return Array.from({ length: 12 }, (_, index) => ({
      mois: index + 1,
      total: Number((this.joursParMois * (index + 1)).toFixed(1)),
    }));
  }

  validerRegle(): void {
    this.regleValidee = true;
    setTimeout(() => {
      this.regleValidee = false;
    }, 2500);
  }
}
