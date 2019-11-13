import {Program} from './program';
import {Lot} from './lot';
import {SafeHtml} from '@angular/platform-browser';

export interface ProgramDateLot {
  program: Program;
  dateMap: Map<string, Lot[]>;
  lastDayLotCount: number;
  lastDayMinPrice: string;
  deliveryInfoHtml: SafeHtml;
  hided: boolean;
}
