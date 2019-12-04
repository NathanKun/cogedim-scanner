import {Component, Input, ViewEncapsulation} from '@angular/core';
import {SafeHtml} from '@angular/platform-browser';

@Component({
  selector: 'app-sales-info',
  template: '<div class="sales-info" [innerHTML]="html"></div>',
  styleUrls: ['./sales-info.component.scss'],
  encapsulation: ViewEncapsulation.None
})
export class SalesInfoComponent {
  @Input() html: SafeHtml;
}
