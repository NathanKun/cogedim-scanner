import {Component, OnInit} from '@angular/core';
import {AuthService} from './service/auth.service';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent implements OnInit {
  isAuthenticated: boolean;

  constructor(public authService: AuthService) {
  }

  async ngOnInit() {
    this.isAuthenticated = this.authService.isAuthenticated();
    // Subscribe to authentication state changes
    this.authService.$authenticationState.subscribe(
      (isAuthenticated: boolean) => this.isAuthenticated = isAuthenticated
    );
  }
}
