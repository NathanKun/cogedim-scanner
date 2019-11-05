import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {tap} from 'rxjs/operators';
import {ProgramDateLot} from '../model/program-date-lot';
import {BaseService} from './base.service';

@Injectable({
  providedIn: 'root'
})
export class ProgramService extends BaseService {

  constructor(private http: HttpClient) {
    super();
  }


  public fetchPrograms(): Observable<ProgramDateLot[]> {
    return this.http.get<ProgramDateLot[]>(
      this.baseurl + '/programs')
      .pipe(
        tap(res => console.log(res))
      );
  }
}
