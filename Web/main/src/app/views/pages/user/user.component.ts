import { Component, CUSTOM_ELEMENTS_SCHEMA, OnInit } from '@angular/core';
import { AuthService } from '../auth/services/auth.service';
import { UserService } from './services/user.service';
import { CommonModule } from '@angular/common';

@Component({
    selector: 'app-user',
    templateUrl: './user.component.html',
    styleUrls: ['./user.component.css'],
    schemas: [CUSTOM_ELEMENTS_SCHEMA],
    standalone: true,
    imports: [CommonModule]
})
export class UserComponent implements OnInit {

    profile:any
    constructor(
        private _auth: AuthService,
        private _userService:UserService
    ) { }
    
    logout(event:any){
        event.preventDefault();
        this._auth.logout();
    }
    ngOnInit(): void {
        this._userService.getUser().subscribe((user)=>{
            this.profile = user
        })

    }

}