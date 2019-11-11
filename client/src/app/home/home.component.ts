import {Component, ElementRef, OnInit, QueryList, Renderer2, ViewChild, ViewChildren} from '@angular/core';
import {ProgramService} from '../service/program.service';
import {ProgramDateLot} from '../model/program-date-lot';
import {GoogleMap, MapInfoWindow, MapMarker} from "@angular/google-maps";

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.css']
})
export class HomeComponent implements OnInit {
  @ViewChildren('programcard') programcards: QueryList<ElementRef>;
  programDateLots: ProgramDateLot[];

  zoom = 13;
  center: google.maps.LatLngLiteral;
  options: google.maps.MapOptions = {
    mapTypeId: 'roadmap',
    zoomControl: false,
    scrollwheel: true,
    disableDoubleClickZoom: true,
    maxZoom: 18,
    minZoom: 8,
  };

  @ViewChild(GoogleMap, {static: false}) map: GoogleMap;
  @ViewChild(MapInfoWindow, {static: false}) info: MapInfoWindow;
  @ViewChildren('markerElem') markerElements: QueryList<MapMarker>;
  markerConfigs: MapMarker[] = [];

  constructor(private renderer: Renderer2,
              private programService: ProgramService) {
  }

  async ngOnInit() {
    this.programService.getProgramDateLots().subscribe(
      async programDateLots => {
        this.programDateLots = programDateLots;

        for (const p of this.programDateLots) {
          // delivery info
          p.deliveryInfoHtml = await this.programService.getProgramPageDeliveryInfo(p.program.url).toPromise();

          // google map marker
          this.markerConfigs.push({
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

  // noinspection JSUnusedGlobalSymbols
  ngAfterViewInit() {
    const transitLayer = new google.maps.TransitLayer();
    transitLayer.setMap(this.map._googleMap);

    this.programcards.changes.subscribe(
      res => this.programcards = res
    );
  }

  markerClick(marker: MapMarker) {
    const index = this.programDateLots.findIndex((pdl) => pdl.program.programName === marker.getTitle());
    this.programcards.filter((item, i) => i === index)[0].nativeElement.scrollIntoView({
      behavior: 'smooth',
      block: 'center'
    });
    this.animateMarker(marker);
  }

  programCardLocationClick(programName) {
    this.animateMarker(this.markerElements.find(m => m.getTitle() === programName));
  }

  private animateMarker(marker: MapMarker) {
    this.map.panTo(marker.getPosition());
    this.map._googleMap.setZoom(13);
    marker._marker.setAnimation(google.maps.Animation.BOUNCE);
    setTimeout(() => marker._marker.setAnimation(null), 1500);
  }
}
