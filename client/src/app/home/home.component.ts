import {Component, OnInit} from '@angular/core';
import {ProgramService} from '../service/program.service';
import {ProgramDateLot} from '../model/program-date-lot';
import {MapMarker} from "@angular/google-maps";

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.css']
})
export class HomeComponent implements OnInit {
  programDateLots: ProgramDateLot[];

  zoom = 12;
  center: google.maps.LatLngLiteral;
  options: google.maps.MapOptions = {
    mapTypeId: 'roadmap',
    zoomControl: false,
    scrollwheel: true,
    disableDoubleClickZoom: true,
    maxZoom: 18,
    minZoom: 8,
  };

  markers: MapMarker[] = [];

  constructor(private programService: ProgramService) {
  }

  async ngOnInit() {
    this.programService.getProgramDateLots().subscribe(
      programDateLots => {
        this.programDateLots = programDateLots;

        this.programDateLots.forEach(
          p => this.markers.push({
            position: {
              lat: parseFloat(p.program.latitude),
              lng: parseFloat(p.program.longitude)
            },
            title: p.program.programName,
            options: {
              animation: google.maps.Animation.DROP
            },
          } as MapMarker)
        )
      }
    );

    navigator.geolocation.getCurrentPosition(position => {
      this.center = {
        lat: position.coords.latitude,
        lng: position.coords.longitude,
      }
    })
  }

  zoomIn() {
    if (this.zoom < this.options.maxZoom) this.zoom++
  }

  zoomOut() {
    if (this.zoom > this.options.minZoom) this.zoom--
  }
}
