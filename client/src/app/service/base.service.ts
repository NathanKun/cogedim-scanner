import {Injectable, isDevMode} from '@angular/core';


@Injectable({
  providedIn: 'root'
})
export class BaseService {

  protected baseurl: string;

  constructor() {
    this.baseurl = isDevMode() ? 'http://localhost:8080' : 'https://cogedimscannerapi.catprogrammer.com';
  }
}
