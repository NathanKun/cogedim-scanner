import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {map, switchMap} from 'rxjs/operators';
import {BaseService} from './base.service';
import {Observable, of, Subscriber} from 'rxjs';
import {DomSanitizer} from '@angular/platform-browser';
import {AuthService} from './auth.service';
import {BouyguesImmoProgram} from "../model/bouyguesimmoprogram";

@Injectable({
  providedIn: 'root'
})
export class BouyguesimmoService extends BaseService {

  private searchCache: BouyguesImmoProgram[];

  constructor(private http: HttpClient,
              private sanitizer: DomSanitizer,
              private authService: AuthService) {
    super();
  }

  fetchSearchResult(): Observable<BouyguesImmoProgram[]> {
    if (this.searchCache) {
      return of(this.searchCache);
    } else {
      return this.http.post<BouyguesImmoProgram[]>(
        this.baseurl + '/bouygues-immo/search',
        {}
      );
    }
  }
}
