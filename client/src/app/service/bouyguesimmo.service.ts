import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {BaseService} from './base.service';
import {Observable, of} from 'rxjs';
import {BouyguesImmoProgram} from '../model/bouyguesimmoprogram';

@Injectable({
  providedIn: 'root'
})
export class BouyguesimmoService extends BaseService {

  private searchCache: BouyguesImmoProgram[];

  constructor(private http: HttpClient) {
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
