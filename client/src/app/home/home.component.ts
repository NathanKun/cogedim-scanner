import { Component, OnInit } from '@angular/core';
import {AuthService} from '../service/auth.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.css']
})
export class HomeComponent implements OnInit {
  isAuthenticated: boolean;

  constructor(private router: Router, private authService: AuthService) {
  }

  async ngOnInit() {
    this.isAuthenticated = this.authService.isAuthenticated();
    if (!this.isAuthenticated) {
      await this.router.navigate(['/login']);
    }
  }
}
