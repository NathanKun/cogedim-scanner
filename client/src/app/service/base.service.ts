import {Injectable, isDevMode} from '@angular/core';


@Injectable({
  providedIn: 'root'
})
export class BaseService {

  protected baseurl: string;
  protected googleMapApiKey: string;

  constructor() {
    this.baseurl = isDevMode() ? 'http://localhost:8080' : 'https://cogedimscannerapi.catprogrammer.com';
    if (isDevMode()) {
      // @ts-ignore
      import('./googlemaplocalhostkey').then(
        a => {
          this.googleMapApiKey = a.KEY;
        }
      )
    } else {
      this.googleMapApiKey = 'AIzaSyCRU_27Qpu6WznzmpbeRGGZ5zm5Ju74T8c';
    }

  }
}
