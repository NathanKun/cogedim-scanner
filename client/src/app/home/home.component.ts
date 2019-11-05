import {Component, OnInit} from '@angular/core';
import {AuthService} from '../service/auth.service';
import {Router} from '@angular/router';
import {ProgramService} from '../service/program.service';
import {ProgramDateLot} from '../model/program-date-lot';

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.css']
})
export class HomeComponent implements OnInit {
  isAuthenticated: boolean;

  constructor(private router: Router,
              private authService: AuthService,
              private programService: ProgramService) {
  }

  async ngOnInit() {
    this.isAuthenticated = this.authService.isAuthenticated();
    await this.authGuard();

    // Subscribe to authentication state changes
    this.authService.$authenticationState.subscribe(
      async (isAuthenticated: boolean) => {
        this.isAuthenticated = isAuthenticated;
        await this.authGuard();
      }
    );

    this.programService.fetchPrograms().subscribe(
      programDateLots => {
        programDateLots.forEach(
          programDateLot => {
            console.log(programDateLot.program.programName);
          }
        );
      }
    );
  }

  private async authGuard() {
    if (!this.isAuthenticated) {
      await this.router.navigate(['/login']);
    }
  }
}
