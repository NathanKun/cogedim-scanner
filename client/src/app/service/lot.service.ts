import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {BaseService} from './base.service';
import {Lot} from '../model/lot';

@Injectable({
  providedIn: 'root'
})
export class LotService extends BaseService {

  constructor(private http: HttpClient) {
    super();
  }

  public putLotProperty(lot: Lot, remarkChanged: boolean, decisionChanged: boolean) {
    const body: any = {};
    if (remarkChanged) {
      body.remark = lot.remark;
    }
    if (decisionChanged) {
      body.decision = lot.decision;
    }
    this.http.put(this.baseurl + '/lot/' + lot.lotNumber, body)
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
