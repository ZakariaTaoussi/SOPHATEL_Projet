import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Agenda } from '../../../core/models/agenda.model';
import { JourCalendrier } from '../../../core/models/jour-calendrier.model';
import { CreateJourFerieRequest } from '../../../core/models/jour-ferie.model';
import { AgendaService } from '../../../core/services/agenda.service';
import { JourFerieService } from '../../../core/services/jour-ferie.service';

@Component({
  selector: 'app-admin-jour-ferie',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './jour-ferie.component.html',
  styleUrls: ['./jour-ferie.component.scss'],
})
export class AdminJourFerieComponent implements OnInit {
  agendas: Agenda[] = [];
  joursCalendrier: JourCalendrier[] = [];
  selectedYear = new Date().getFullYear();
  newYear = new Date().getFullYear();
  currentMonth = new Date().getMonth();
  selectedDay?: JourCalendrier;
  form: CreateJourFerieRequest = this.emptyForm();
  errorMessage = '';
  isLoading = false;

  readonly monthNames = ['Janvier', 'Fevrier', 'Mars', 'Avril', 'Mai', 'Juin', 'Juillet', 'Aout', 'Septembre', 'Octobre', 'Novembre', 'Decembre'];
  readonly weekDays = ['Lun', 'Mar', 'Mer', 'Jeu', 'Ven', 'Sam', 'Dim'];

  constructor(
    private readonly agendaService: AgendaService,
    private readonly jourFerieService: JourFerieService
  ) {}

  ngOnInit(): void {
    this.loadAgendas();
  }

  get monthDays(): Array<JourCalendrier | null> {
    const days = this.joursCalendrier.filter(jour => new Date(`${jour.date}T00:00:00`).getMonth() === this.currentMonth);
    const firstDate = new Date(this.selectedYear, this.currentMonth, 1);
    const startOffset = (firstDate.getDay() + 6) % 7;
    return [...Array(startOffset).fill(null), ...days];
  }

  loadAgendas(): void {
    this.agendaService.consulterAgendas().subscribe({
      next: agendas => {
        this.agendas = agendas.sort((a, b) => b.annee - a.annee);
        const currentYearAgenda = this.agendas.find(agenda => agenda.annee === new Date().getFullYear());
        const selectedAgenda = this.agendas.find(agenda => agenda.annee === this.selectedYear);
        const agendaToLoad = selectedAgenda ?? currentYearAgenda ?? this.agendas[0];

        if (agendaToLoad) {
          this.selectedYear = agendaToLoad.annee;
          this.newYear = agendaToLoad.annee;
          this.loadJoursCalendrier();
        } else {
          this.joursCalendrier = [];
        }
      },
      error: error => this.handleError(error),
    });
  }

  creerAgenda(): void {
    this.errorMessage = '';
    this.agendaService.creerAgenda({ annee: this.newYear }).subscribe({
      next: agenda => {
        this.selectedYear = agenda.annee;
        this.newYear = agenda.annee;
        this.currentMonth = 0;
        this.loadJoursCalendrier();
        this.loadAgendas();
      },
      error: error => this.handleError(error),
    });
  }

  changerAnnee(): void {
    this.currentMonth = 0;
    this.loadJoursCalendrier();
  }

  loadJoursCalendrier(): void {
    this.isLoading = true;
    this.agendaService.consulterJoursCalendrier(this.selectedYear).subscribe({
      next: jours => {
        this.joursCalendrier = jours;
        this.isLoading = false;
      },
      error: error => this.handleError(error),
    });
  }

  previousMonth(): void {
    if (this.currentMonth > 0) {
      this.currentMonth--;
    }
  }

  nextMonth(): void {
    if (this.currentMonth < 11) {
      this.currentMonth++;
    }
  }

  ouvrirModal(jour: JourCalendrier | null): void {
    if (!jour) return;
    this.selectedDay = jour;
    this.errorMessage = '';
    this.form = {
      nom: jour.jourFerieNom ?? '',
      dateDebut: jour.date,
      dateFin: jour.date,
      description: jour.description ?? '',
    };

    if (jour.jourFerieId) {
      this.jourFerieService.consulterJourFerie(jour.jourFerieId).subscribe({
        next: jourFerie => this.form = {
          nom: jourFerie.nom,
          dateDebut: jourFerie.dateDebut,
          dateFin: jourFerie.dateFin,
          description: jourFerie.description ?? '',
        },
        error: error => this.handleError(error),
      });
    }
  }

  fermerModal(): void {
    this.selectedDay = undefined;
    this.form = this.emptyForm();
  }

  enregistrerJourFerie(): void {
    if (!this.selectedDay || !this.form.nom.trim() || !this.form.dateDebut || !this.form.dateFin) return;

    const request = { ...this.form, nom: this.form.nom.trim() };
    const action = this.selectedDay.jourFerieId
      ? this.jourFerieService.modifierJourFerie(this.selectedDay.jourFerieId, request)
      : this.jourFerieService.creerJourFerie(request);

    action.subscribe({
      next: () => {
        this.fermerModal();
        this.loadJoursCalendrier();
      },
      error: error => this.handleError(error),
    });
  }

  supprimerJourFerie(): void {
    if (!this.selectedDay?.jourFerieId) return;

    this.jourFerieService.supprimerJourFerie(this.selectedDay.jourFerieId).subscribe({
      next: () => {
        this.fermerModal();
        this.loadJoursCalendrier();
      },
      error: error => this.handleError(error),
    });
  }

  dayNumber(jour: JourCalendrier): number {
    return new Date(`${jour.date}T00:00:00`).getDate();
  }

  private handleError(error: unknown): void {
    console.error('Erreur HTTP admin jour-ferie', error);
    this.isLoading = false;
    this.errorMessage = error instanceof HttpErrorResponse
      ? error.error?.message ?? 'Une erreur est survenue.'
      : 'Une erreur est survenue.';
  }

  private emptyForm(): CreateJourFerieRequest {
    const today = new Date().toISOString().slice(0, 10);
    return { nom: '', dateDebut: today, dateFin: today, description: '' };
  }
}
