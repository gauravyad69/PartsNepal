import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-auth',
  templateUrl: './auth.component.html',
  styleUrls: [],
  standalone: true,
  imports: [CommonModule, RouterOutlet]
})
export class AuthComponent {
  constructor() { }

  ngOnInit() {
  }
}
