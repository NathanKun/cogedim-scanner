import {Injectable, isDevMode} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {tap} from 'rxjs/operators';
import {Observable, Observer} from 'rxjs';


@Injectable({
  providedIn: 'root'
})
export class BaseService {

  protected baseurl: string;

  constructor() {
    this.baseurl = isDevMode() ? 'http://localhost:8080' : 'https://cogedimscannerapi.catprogrammer.com';
  }
}
