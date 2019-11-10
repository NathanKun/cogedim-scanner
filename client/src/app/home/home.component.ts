import {Component, OnInit, ViewChild} from '@angular/core';
import {ProgramService} from '../service/program.service';
import {ProgramDateLot} from '../model/program-date-lot';
import {GoogleMap, MapInfoWindow, MapMarker} from "@angular/google-maps";

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

  @ViewChild(GoogleMap, { static: false }) map: GoogleMap;
  @ViewChild(MapInfoWindow, { static: false }) info: MapInfoWindow;
  markers: MapMarker[] = [];

  constructor(private programService: ProgramService) {
  }

  async ngOnInit() {
    this.programService.getProgramDateLots().subscribe(
      async programDateLots => {
        this.programDateLots = programDateLots;

        for (const p of this.programDateLots) {
          // delivery info
          p.deliveryInfoHtml = await this.programService.getProgramPageDeliveryInfo(p.program.url).toPromise();

          // google map marker
          this.markers.push({
            position: {
              lat: parseFloat(p.program.latitude),
              lng: parseFloat(p.program.longitude)
            },
            title: p.program.programName,
            options: {
              animation: google.maps.Animation.DROP,
            },
          } as MapMarker);
        }
      }
    );

    navigator.geolocation.getCurrentPosition(position => {
      this.center = {
        lat: position.coords.latitude,
        lng: position.coords.longitude,
      };
    });
  }

  ngAfterViewInit(){
    const transitLayer = new google.maps.TransitLayer();
    transitLayer.setMap(this.map._googleMap);
  }
}
