import {Injectable, isDevMode} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {map} from 'rxjs/operators';


@Injectable({
  providedIn: 'root'
})
export class AuthService {

  private accessToken: string = null;
  private baseurl: string;

  constructor(private http: HttpClient) {
    this.baseurl = isDevMode() ? 'http://localhost:8080' : 'https://cogedimscannerapi.catprogrammer.com';
  }

  public isAuthenticated(): boolean {
    return this.accessToken != null;
  }

  public login(u: string, p: string) {
    return this.http.post(this.baseurl + '/auth/login', {
      username: u,
      password: p
    }).pipe(
      map(res => {
        console.log(res);
        this.accessToken = res as string;
      })
    );
  }

  public logout() {
    this.accessToken = null;
  }

  public getAccessToken(): string {
    return this.accessToken;
  }
}
