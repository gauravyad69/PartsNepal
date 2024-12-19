import { Component } from '@angular/core';
import { CommonModule} from '@angular/common';
import { RouterOutlet } from '@angular/router';

@Component({
    selector: 'app-orders',
    templateUrl: './orders.component.html',
    styleUrls: ['./orders.component.css'],
    standalone: true,
    imports: [CommonModule, RouterOutlet]
})

export class OrdersComponent {

    constructor() { }
}