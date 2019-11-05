import {Component, OnInit} from '@angular/core';
import {ProgramService} from '../service/program.service';
import {ProgramDateLot} from '../model/program-date-lot';

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.css']
})
export class HomeComponent implements OnInit {
  programDateLots: ProgramDateLot[];

  constructor(private programService: ProgramService) {
  }

  async ngOnInit() {
    this.programService.getProgramDateLots().subscribe(
      programDateLots => {
        this.programDateLots = programDateLots;
      }
    );
  }
}
