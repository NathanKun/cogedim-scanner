import {Injectable} from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class ScrollService {

  private homeScrollOffset = 0;

  constructor() {
  }

  recordHomePosition() {
    this.homeScrollOffset = window.pageYOffset;
  }

  scrollHome() {
    setTimeout(() => window.scrollTo(0, this.homeScrollOffset), 200);
  }
}
