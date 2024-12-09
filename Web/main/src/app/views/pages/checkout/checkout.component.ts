import { Component } from '@angular/core';
import { CommonModule} from '@angular/common';
import { RouterOutlet } from '@angular/router';

@Component({
    selector: 'app-checkout',
    templateUrl: './checkout.component.html',
    styleUrls: ['./checkout.component.css'],
    standalone: true,
    imports: [CommonModule, RouterOutlet]
})

export class CheckoutComponent {

    constructor() { }
}