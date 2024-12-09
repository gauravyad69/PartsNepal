import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-confirm-model',
  templateUrl: './confirm-model.component.html',
  styleUrls: ['./confirm-model.component.css'],
  standalone: true,
  imports: [CommonModule]
})
export class ConfirmModelComponent {
  @Input() isVisable!: boolean;
  @Input() message!: string;
  @Input() title!: string;
  @Input() confirmBtnText: string = 'Confirm';
  @Input() cancelBtnText: string = 'Cancel';

  @Output() confirm = new EventEmitter<boolean>();
  @Output() cancel = new EventEmitter<boolean>();



  delete() {
    this.confirm.emit(true);
    this.isVisable = false;
  }

  close() {
    this.isVisable = false;
    this.cancel.emit(false);
  }

}
