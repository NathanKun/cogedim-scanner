import {HttpEvent, HttpHandler, HttpInterceptor, HttpRequest} from '@angular/common/http';
import {from, Observable} from 'rxjs';
import {Injectable} from '@angular/core';
import {AuthService} from '../service/auth.service';
import {environment} from '../../environments/environment';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {

  constructor(private authService: AuthService) {
  }

  intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    return from(this.handleAccess(request, next));
  }

  private handleAccess(request: HttpRequest<any>, next: HttpHandler): Promise<HttpEvent<any>> {
    // Only add to known domains since we don't want to send your tokens to just anyone
    if (request.urlWithParams.indexOf(environment.baseUrl) > -1) {
      const accessToken = this.authService.getAccessToken();
      if (accessToken != null) {
        request = request.clone({
          setHeaders: {
            Authorization: accessToken
          }
        });
      }
    }
    return next.handle(request).toPromise();
  }
}
