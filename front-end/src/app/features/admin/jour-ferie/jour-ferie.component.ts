import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Agenda } from '../../../core/models/agenda.model';
import { JourCalendrier } from '../../../core/models/jour-calendrier.model';
import { CreateJourFerieRequest } from '../../../core/models/jour-ferie.model';
import { AgendaService } from '../../../core/services/agenda.service';
import { JourFerieService } from '../../../core/services/jour-ferie.service';

interface CalendarCell {
  date: string;
  dayNumber: number;
  inCurrentMonth: boolean;
  jour: JourCalendrier | null;
}

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
  modalErrorMessage = '';
  isLoading = false;

  readonly monthNames = ['Janvier', 'Fevrier', 'Mars', 'Avril', 'Mai', 'Juin', 'Juillet', 'Aout', 'Septembre', 'Octobre', 'Novembre', 'Decembre'];
  readonly weekDays = ['Dimanche', 'Lundi', 'Mardi', 'Mercredi', 'Jeudi', 'Vendredi', 'Samedi'];

  constructor(
    private readonly agendaService: AgendaService,
    private readonly jourFerieService: JourFerieService,
    private readonly cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.loadAgendas();
  }

  get selectedAgenda(): Agenda | undefined {
    return this.agendas.find(agenda => agenda.annee === this.selectedYear);
  }

  get holidayCount(): number {
    return new Set(this.joursCalendrier.filter(jour => jour.jourFerieId).map(jour => jour.jourFerieId)).size;
  }

  get calendarCells(): CalendarCell[] {
    const joursByDate = new Map(this.joursCalendrier.map(jour => [jour.date, jour]));
    const firstDate = new Date(this.selectedYear, this.currentMonth, 1);
    const gridStart = new Date(firstDate);
    gridStart.setDate(firstDate.getDate() - firstDate.getDay());

    return Array.from({ length: 42 }, (_, index) => {
      const date = new Date(gridStart);
      date.setDate(gridStart.getDate() + index);
      const isoDate = this.toIsoDate(date);
      return {
        date: isoDate,
        dayNumber: date.getDate(),
        inCurrentMonth: date.getMonth() === this.currentMonth,
        jour: joursByDate.get(isoDate) ?? null,
      };
    });
  }

  loadAgendas(yearToSelect = this.selectedYear): void {
    this.agendaService.consulterAgendas().subscribe({
      next: agendas => {
        this.agendas = agendas.sort((a, b) => b.annee - a.annee);
        const currentYearAgenda = this.agendas.find(agenda => agenda.annee === new Date().getFullYear());
        const selectedAgenda = this.agendas.find(agenda => agenda.annee === yearToSelect);
        const agendaToLoad = selectedAgenda ?? currentYearAgenda ?? this.agendas[0];

        if (agendaToLoad) {
          this.selectedYear = agendaToLoad.annee;
          this.newYear = agendaToLoad.annee;
          this.currentMonth = agendaToLoad.annee === new Date().getFullYear() ? new Date().getMonth() : 0;
          this.loadJoursCalendrier();
        } else {
          this.joursCalendrier = [];
          this.cdr.detectChanges();
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
        this.currentMonth = agenda.annee === new Date().getFullYear() ? new Date().getMonth() : 0;
        this.loadJoursCalendrier();
        this.loadAgendas(agenda.annee);
      },
      error: error => this.handleError(error),
    });
  }

  supprimerAgenda(): void {
    const agenda = this.selectedAgenda;
    if (!agenda) return;
    if (!window.confirm(`Supprimer l'agenda ${agenda.annee} et ses jours feries ?`)) return;

    this.errorMessage = '';
    this.agendaService.supprimerAgenda(agenda.id).subscribe({
      next: () => {
        this.joursCalendrier = [];
        this.loadAgendas();
      },
      error: error => this.handleError(error),
    });
  }

  changerAnnee(): void {
    this.currentMonth = this.selectedYear === new Date().getFullYear() ? new Date().getMonth() : 0;
    this.loadJoursCalendrier();
  }

  loadJoursCalendrier(): void {
    this.isLoading = true;
    this.cdr.detectChanges();
    this.agendaService.consulterJoursCalendrier(this.selectedYear).subscribe({
      next: jours => {
        this.joursCalendrier = jours;
        this.isLoading = false;
        this.cdr.detectChanges();
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

  ouvrirModal(cell: CalendarCell): void {
    const jour = cell.jour;
    if (!jour || !cell.inCurrentMonth) return;
    this.selectedDay = jour;
    this.errorMessage = '';
    this.modalErrorMessage = '';
    this.form = {
      nom: jour.jourFerieNom ?? '',
      dateDebut: jour.date,
      dateFin: jour.date,
      description: jour.description ?? '',
    };

    if (jour.jourFerieId) {
      this.jourFerieService.consulterJourFerie(jour.jourFerieId).subscribe({
        next: jourFerie => {
          this.form = {
            nom: jourFerie.nom,
            dateDebut: jourFerie.dateDebut,
            dateFin: jourFerie.dateFin,
            description: jourFerie.description ?? '',
          };
          this.cdr.detectChanges();
        },
        error: error => this.handleModalError(error),
      });
    }
  }

  fermerModal(): void {
    this.selectedDay = undefined;
    this.form = this.emptyForm();
    this.modalErrorMessage = '';
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
      error: error => this.handleModalError(error),
    });
  }

  supprimerJourFerie(): void {
    if (!this.selectedDay?.jourFerieId) return;

    this.jourFerieService.supprimerJourFerie(this.selectedDay.jourFerieId).subscribe({
      next: () => {
        this.fermerModal();
        this.loadJoursCalendrier();
      },
      error: error => this.handleModalError(error),
    });
  }

  isToday(cell: CalendarCell): boolean {
    return cell.date === this.toIsoDate(new Date());
  }

  private handleError(error: unknown): void {
    console.error('Erreur HTTP admin jour-ferie', error);
    this.isLoading = false;
    this.errorMessage = error instanceof HttpErrorResponse
      ? error.error?.message ?? 'Une erreur est survenue.'
      : 'Une erreur est survenue.';
    this.cdr.detectChanges();
  }

  private handleModalError(error: unknown): void {
    console.error('Erreur HTTP admin jour-ferie modal', error);
    this.modalErrorMessage = error instanceof HttpErrorResponse
      ? error.error?.message ?? 'Une erreur est survenue.'
      : 'Une erreur est survenue.';
    this.cdr.detectChanges();
  }

  private emptyForm(): CreateJourFerieRequest {
    const today = new Date().toISOString().slice(0, 10);
    return { nom: '', dateDebut: today, dateFin: today, description: '' };
  }

  private toIsoDate(date: Date): string {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  }
}
