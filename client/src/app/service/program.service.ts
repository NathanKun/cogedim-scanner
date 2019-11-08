import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {map, tap} from 'rxjs/operators';
import {ProgramDateLot} from '../model/program-date-lot';
import {BaseService} from './base.service';
import {Observable, of} from 'rxjs';
import {DomSanitizer, SafeHtml} from "@angular/platform-browser";

@Injectable({
  providedIn: 'root'
})
export class ProgramService extends BaseService {

  private programDateLotCache: ProgramDateLot[];
  private programPageCache: Map<string, SafeHtml>; // program url  => html

  constructor(private http: HttpClient,
              private sanitizer: DomSanitizer) {
    super();
    this.programPageCache = new Map<string, string>()
  }

  public getProgramDateLots(): Observable<ProgramDateLot[]> {
    if (this.programDateLotCache) {
      return of(this.programDateLotCache);
    } else {
      return this.fetchProgramDateLots();
    }
  }

  private fetchProgramDateLots(): Observable<ProgramDateLot[]> {
    return this.http.get<ProgramDateLot[]>(
      this.baseurl + '/programs')
      .pipe(
        tap(res => this.programDateLotCache = res),
        map(res => {
          res.forEach(p => {
            // convert the object to a map
            p.dateMap = new Map(Object.entries(p.dateMap));

            // set the last day lot count prop
            const values = Array.from(p.dateMap.values());
            p.lastDayLotCount = values[values.length - 1].length;
            const lastDaySortedLots = values[values.length - 1].sort(
              (a, b) =>
                +a.price.replace(' ', '').replace('€', '')
                <
                +b.price.replace(' ', '').replace('€', '')
                  ?
                  -1 : 1
            );
            if (lastDaySortedLots.length) {
              p.lastDayMinPrice = lastDaySortedLots[0].price;
            }
          });
          return res;
        })
      );
  }

  public getProgramPageSalesInfo(url: string): Observable<SafeHtml> {
    if (this.programPageCache.has(url)) {
      return of(this.programPageCache.get(url));
    } else {
      return this.fetchProgramPageSalesInfo(url);
    }
  }

  private fetchProgramPageSalesInfo(url: string): Observable<SafeHtml> {
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

            // sanitize html
            const res = this.sanitizer.bypassSecurityTrustHtml(div.innerHTML);

            // cache result
            this.programPageCache.set(url, res);

            return res;
          }
        )
      );
  }
}
