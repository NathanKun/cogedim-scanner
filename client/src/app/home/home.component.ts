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
  programDateLots: ProgramDateLot[];

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

    if (!this.isAuthenticated) {
      return;
    }

    this.programService.getPrograms().subscribe(
      programDateLots => {
        this.programDateLots = programDateLots;
        programDateLots.forEach(
          programDateLot => {
            const values = Array.from(programDateLot.dateMap.values());
            programDateLot.lastDayLotCount = values[values.length - 1].length;
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
