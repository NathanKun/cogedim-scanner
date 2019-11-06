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

  public fetchProgramPageSalesInfo(url: string): Observable<string> {
    return this.http.get<string>(
      this.baseurl + '/program?url=' + url,
      {responseType: 'text' as 'json'})
      .pipe(
        map(
          str => {
            const doc = new DOMParser().parseFromString(str, 'text/html');
            const div = doc.querySelector('.paragraphs-sales_office');

            // update images src, add cogedim's domain
            div.querySelectorAll('img[src^="/"]').forEach(
              e => e.setAttribute('src', 'https://www.cogedim.com/' + e.getAttribute('src'))
            );

            // remove sales office section
            div.querySelector('.sales-office').remove();

            return div.innerHTML;
          }
        )
      );
  }
}
