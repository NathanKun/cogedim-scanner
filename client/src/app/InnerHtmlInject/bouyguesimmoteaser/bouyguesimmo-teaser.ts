import {Component, Input, ViewEncapsulation} from '@angular/core';
import {SafeHtml} from '@angular/platform-browser';

@Component({
  selector: 'app-bouyguesimmo-teaser',
  template: '<div class="bouyguesimmo-teaser row" [innerHTML]="html"></div>',
  styleUrls: [
    './bouyguesimmo-teaser.component.scss',
    './bouyguesimmo-teaser.css'
  ],
  encapsulation: ViewEncapsulation.None
})
export class BouyguesImmoTeaserComponent {
  @Input() html: SafeHtml;
}
