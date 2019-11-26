import {Decision} from './decision';

export interface Lot {
  id: number;
  lotNumber: string;
  surface: string;
  floor: string;
  price: string;
  blueprintId: string;
  pdfUrl: string;
  remark: string;
  decision: Decision;
  createdAt: string;
  modifiedAt: string;
}
