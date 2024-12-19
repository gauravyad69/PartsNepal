import { Component, CUSTOM_ELEMENTS_SCHEMA, HostListener, OnInit } from '@angular/core';
import { AuthService } from '../../pages/auth/services/auth.service';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-header',
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.css'],
  schemas: [CUSTOM_ELEMENTS_SCHEMA],
  standalone: true,
  imports: [CommonModule, RouterModule]
})

export class HeaderComponent implements OnInit {
   
  sticky: boolean = false;
  loggedIn: boolean = false;
  constructor
    (
      private _auth: AuthService,
    ) { }

  @HostListener('window:scroll', ['$event'])
  handleScroll() {
    const windowScroll = window.pageYOffset;
    if (windowScroll >= 300) {
      this.sticky = true;
    } else {
      this.sticky = false;
    }
  }

  ngOnInit(): void {
    this.loggedIn = this._auth.loggedIn();
    console.log(this.loggedIn)
  }

}
