import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {BaseService} from './base.service';
import {Lot} from '../model/lot';
import {Program} from '../model/program';

@Injectable({
  providedIn: 'root'
})
export class LotService extends BaseService {

  constructor(private http: HttpClient) {
    super();
  }

  public putLotProperty(lot: Lot, program: Program, remarkChanged: boolean, decisionChanged: boolean) {
    let body: string;
    if (remarkChanged) {
      body = 'remark=' + lot.remark;
    }
    if (decisionChanged) {
      body = body ? (body + '&') : '';
      body = body + 'decision=' + lot.decision;
    }
    this.http.put(this.baseurl + '/program/' + program.programNumber + '/lot/' + lot.lotNumber,
      body,
      {
        headers: {
          'Content-Type': 'application/x-www-form-urlencoded'
        }
      })
      .subscribe(
        res => {
          console.log('putLotProperty OK');
          console.log(res);
        },
        error => {
          console.log('putLotProperty error');
          console.log(error);
        }
      );
  }
}
