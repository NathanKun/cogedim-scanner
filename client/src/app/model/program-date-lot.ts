import {Program} from './program';
import {Lot} from './lot';

export interface ProgramDateLot {
  program: Program;
  dateMap: Map<string, Lot[]>;
  lastDayLotCount: number;
}
