import {BigMapPin} from './bigmappin';

export interface BigMapPinDetail extends BigMapPin {
  imgUrl: string;
  programName: string;
  postalCode: string;
  addressLocality: string;
  summary: string;
  url: string;
}
