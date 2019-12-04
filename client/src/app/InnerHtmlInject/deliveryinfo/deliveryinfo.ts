import {Component, Input, ViewEncapsulation} from '@angular/core';
import {SafeHtml} from '@angular/platform-browser';

@Component({
  selector: 'app-delivery-info',
  template: '<div class="delivery-info" [innerHTML]="html"></div>',
  styleUrls: ['./delivery-info.component.scss'],
  encapsulation: ViewEncapsulation.None
})
export class DeliveryInfoComponent {
  @Input() html: SafeHtml;
}
