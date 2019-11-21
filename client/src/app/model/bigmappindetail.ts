import {BigMapPin} from './bigmappin';
import {SafeHtml} from '@angular/platform-browser';

export interface BigMapPinDetail extends BigMapPin {
  imgUrl: string;
  programName: string;
  postalCode: string;
  addressLocality: string;
  summary: string;
  url: string;
  deliveryInfo: SafeHtml;
}
