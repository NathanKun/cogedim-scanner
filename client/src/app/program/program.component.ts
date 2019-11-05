import {Component, OnInit} from '@angular/core';
import {ProgramService} from '../service/program.service';
import {ActivatedRoute, ParamMap} from '@angular/router';
import {map, switchMap} from 'rxjs/operators';
import {ProgramDateLot} from '../model/program-date-lot';
import {Observable} from 'rxjs';

@Component({
  selector: 'app-program',
  templateUrl: './program.component.html',
  styleUrls: ['./program.component.css']
})
export class ProgramComponent implements OnInit {

  programDateLot$: Observable<ProgramDateLot>;

  constructor(
    private route: ActivatedRoute,
    private programService: ProgramService) {
  }

  ngOnInit() {
    this.programDateLot$ = this.route.paramMap.pipe(
      switchMap(
        (params: ParamMap) =>
          this.programService.getProgramDateLots().pipe(
            map(
              programDateLots =>
                programDateLots.find(p => p.program.programNumber === params.get('programNumber'))
            ))
      ));
  }

}
