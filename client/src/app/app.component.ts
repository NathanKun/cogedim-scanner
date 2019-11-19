import {Component, OnInit} from '@angular/core';
import {AuthService} from './service/auth.service';
import {Router} from '@angular/router';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent implements OnInit {
  isAuthenticated = false;

  constructor(public authService: AuthService,
              public router: Router) {
  }

  ngOnInit() {
    // Subscribe to authentication state changes
    this.authService.$authenticationState.subscribe(
      async (isAuthenticated: boolean) => {
        this.isAuthenticated = isAuthenticated;
        await this.authGuard();
      }
    );

    this.authService.backendAuthCheck().subscribe(
      () => {
        this.isAuthenticated = this.authService.isAuthenticated();
      },
      () => {
        this.authService.logout();
      }
    );
  }

  private async authGuard() {
    if (!this.isAuthenticated) {
      await this.router.navigate(['/login']);
    }
  }
}
