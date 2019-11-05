import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {map, tap} from 'rxjs/operators';
import {ProgramDateLot} from '../model/program-date-lot';
import {BaseService} from './base.service';
import {Observable, of} from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class ProgramService extends BaseService {

  private cache: ProgramDateLot[];

  constructor(private http: HttpClient) {
    super();
  }

  public getProgramDateLots(): Observable<ProgramDateLot[]> {
    if (this.cache) {
      return of(this.cache);
    } else {
      return this.fetchProgramDateLots();
    }
  }

  private fetchProgramDateLots(): Observable<ProgramDateLot[]> {
    return this.http.get<ProgramDateLot[]>(
      this.baseurl + '/programs')
      .pipe(
        tap(res => this.cache = res),
        map(res => {
          res.forEach(p => {
            // convert the object to a map
            p.dateMap = new Map(Object.entries(p.dateMap));

            // set the last day lot count prop
            const values = Array.from(p.dateMap.values());
            p.lastDayLotCount = values[values.length - 1].length;
          });
          return res;
        })
      );
  }
}
