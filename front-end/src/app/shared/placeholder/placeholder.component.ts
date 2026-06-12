import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-placeholder',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="placeholder">
      <h2>Page en construction</h2>
      <p>Cette fonctionnalité sera disponible bientôt.</p>
    </div>
  `,
  styles: [
    `
      .placeholder { padding: 2rem; text-align: center; color: rgba(0,0,0,0.7); }
      .placeholder h2 { margin-bottom: .5rem; }
    `,
  ],
})
export class PlaceholderComponent {}
