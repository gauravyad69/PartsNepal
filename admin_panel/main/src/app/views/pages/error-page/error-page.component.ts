import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterModule } from '@angular/router';

@Component({
  selector: 'app-error-page',
  templateUrl: './error-page.component.html',
  styleUrls: ['./error-page.component.css'],
  standalone: true,
  imports: [CommonModule, RouterModule]
})
export class ErrorPageComponent {

  type: any;
  title: any;
  desc: any;

  constructor(private route: ActivatedRoute) { }

  ngOnInit(): void {
    this.type = this.route.snapshot.paramMap.get('type');
    console.log(this.type);
    
    this.route.data.subscribe( param => {
      if(param.type) {
        this.type = param.type;
      }
      if(param.title) {
        this.title = param.title;
      }
      if(param.desc) {
        this.desc = param.desc
      }
    });

    switch(this.type) {
      case '404':
        if (!this.title) {
          this.title = 'Page Not Found'
        }
        if (!this.desc) {
          this.desc = 'Oopps!! The page you were looking for doesn\'t exist.'
        }
        break;
      case '500':
        if (!this.title) {
          this.title = 'Internal server error'
        }
        if (!this.desc) {
          this.desc = 'Oopps!! There wan an error. Please try agin later.'
        }
        break;
      default:
        // if (!this.type) {
          this.type = 'Ooops..';
        // }
        if (!this.title) {
          this.title = 'Something went wrong';
        }
        if (!this.desc) {
          this.desc = 'Looks like something went wrong.<br>' + 'We\'re working on it';
        }
    }
  }

}
