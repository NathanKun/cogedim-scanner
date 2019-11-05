import { Component, OnInit } from '@angular/core';
import {ProgramService} from '../service/program.service';

@Component({
  selector: 'app-program',
  templateUrl: './program.component.html',
  styleUrls: ['./program.component.css']
})
export class ProgramComponent implements OnInit {

  constructor(private programService: ProgramService) { }

  ngOnInit() {
  }

}
