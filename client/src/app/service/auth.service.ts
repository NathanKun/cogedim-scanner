import {Injectable, isDevMode} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {tap} from 'rxjs/operators';
import {Observable, Observer} from 'rxjs';


@Injectable({
  providedIn: 'root'
})
export class AuthService {

  private accessToken: string = null;
  private baseurl: string;
  private observers: Observer<boolean>[];
  $authenticationState: Observable<boolean>;

  constructor(private http: HttpClient) {
    this.observers = [];
    this.baseurl = isDevMode() ? 'http://localhost:8080' : 'https://cogedimscannerapi.catprogrammer.com';
    this.$authenticationState = new Observable((observer: Observer<boolean>) => {
      this.observers.push(observer);
    });
  }

  public isAuthenticated(): boolean {
    return this.accessToken != null;
  }

  private emitAuthenticationState(state: boolean) {
    this.observers.forEach(observer => observer.next(state));
  }

  public login(u: string, p: string): Observable<string> {
    return this.http.post<string>(
      this.baseurl + '/auth/login',
      'username=' + u + '&password=' + p,
      {
        headers: {
          'Content-Type': 'application/x-www-form-urlencoded'
        },
        responseType: 'text' as 'json'
      })
      .pipe(
        tap(res => {
          this.accessToken = res as string;
          if (this.isAuthenticated()) {
            this.emitAuthenticationState(true);
          }
        })
      );
  }

  public logout() {
    this.accessToken = null;
    this.emitAuthenticationState(false);
  }

  public getAccessToken(): string {
    return this.accessToken;
  }
}
