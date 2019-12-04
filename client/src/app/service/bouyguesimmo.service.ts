import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {map, switchMap} from 'rxjs/operators';
import {BaseService} from './base.service';
import {Observable, of, Subscriber} from 'rxjs';
import {DomSanitizer} from '@angular/platform-browser';
import {AuthService} from './auth.service';

@Injectable({
  providedIn: 'root'
})
export class BouyguesimmoService extends BaseService {

  private searchCache: string[];
  private detailCache: Map<string, any>; // program nid  => json

  constructor(private http: HttpClient,
              private sanitizer: DomSanitizer,
              private authService: AuthService) {
    super();
    this.detailCache = new Map<string, any>();
  }

  fetchSearchResult(): Observable<string[]> {
    if (this.searchCache) {
      return of(this.searchCache);
    } else {
      return this.http.get<any>(
        this.baseurl + '/bouygues-immo/search'
      ).pipe(
        map(
          res => {
            this.searchCache = res.items as string[];
            return this.searchCache;
          }
        )
      );
    }
  }

  fetchDetail(nid: string): Observable<string> {
    if (this.detailCache.has(nid)) {
      return this.detailCache.get(nid);
    } else {
      this.http.get<any>(
        this.baseurl + '/bouygues-immo/detail/' + nid
      ).pipe(
        map(
          res => {
            const htmlStr = res.html;

            const doc = new DOMParser().parseFromString(htmlStr, 'text/html');
            const aTag = doc.querySelector('div.program-title > a');
            const title = aTag.textContent;
            const url = 'https://www.bouygues-immobilier.com' + aTag.getAttribute('href');


            this.detailCache.set(nid, htmlStr);
            return htmlStr;
          }
        )
      );
    }
  }

}
