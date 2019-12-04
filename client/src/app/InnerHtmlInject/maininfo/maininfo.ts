import {Component, Input, ViewEncapsulation} from '@angular/core';
import {SafeHtml} from '@angular/platform-browser';

@Component({
  selector: 'app-main-info',
  template: '<div class="main-info" [innerHTML]="html"></div>',
  styleUrls: ['./main-info.component.scss'],
  encapsulation: ViewEncapsulation.None
})
export class MainInfoComponent {
  @Input() html: SafeHtml;
}
