import {Component, ElementRef, EventEmitter, Input, Output, ViewChild} from '@angular/core';

@Component({
  selector: 'app-edit-input',
  templateUrl: './edit-input.component.html',
  styleUrls: ['./edit-input.component.scss'],
})
export class EditInputComponent {
  @Input() data: string;
  @Output() focusOut: EventEmitter<string> = new EventEmitter<string>();

  @ViewChild('input') input: ElementRef;

  editMode = false;

  constructor() {
  }

  onClick() {
    this.editMode = true;
    setTimeout(() => this.input.nativeElement.focus(), 0);
  }

  onFocusOut() {
    this.editMode = false;
    this.focusOut.emit(this.data);
  }
}
