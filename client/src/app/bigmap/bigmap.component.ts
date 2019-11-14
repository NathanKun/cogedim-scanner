import {Component, OnInit, QueryList, ViewChild, ViewChildren} from '@angular/core';
import {GoogleMap, MapInfoWindow, MapMarker} from '@angular/google-maps';
import {CookieService} from 'ngx-cookie-service';
import {Title} from '@angular/platform-browser';
import {environment} from '../../environments/environment';
import {ProgramService} from '../service/program.service';
import {BigMapPin} from '../model/bigmappin';

@Component({
  selector: 'app-bigmap',
  templateUrl: './bigmap.component.html',
  styleUrls: ['./bigmap.component.css']
})
export class BigmapComponent implements OnInit {

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

  @ViewChild(GoogleMap) map: GoogleMap;
  @ViewChild(MapInfoWindow) infoWindow: MapInfoWindow;
  @ViewChildren('markerElem') markerElements: QueryList<MapMarker>;
  markerConfigs: MapMarker[] = [];
  infoContent = '';

  bigMapPins: BigMapPin[];

  constructor(private cookieService: CookieService,
              private titleService: Title,
              private programService: ProgramService) {
  }

  ngOnInit() {
    this.titleService.setTitle(environment.title + ' - Big Map');

    navigator.geolocation.getCurrentPosition(position => {
      this.center = {
        lat: position.coords.latitude,
        lng: position.coords.longitude,
      };
    });

    this.programService.getBigmapPins().subscribe(
      bps => {
        this.bigMapPins = bps;

        this.bigMapPins.forEach(item => {
          this.markerConfigs.push({
            position: {
              lat: item.lat,
              lng: item.lng
            },
            title: item.nid,
            options: {
              animation: google.maps.Animation.DROP,
            },
          } as MapMarker);
        });
      }
    );
  }

  markerClick(marker: MapMarker) {
    this.infoWindow.open(marker);
    this.infoContent = this.bigMapPins.find(p => p.nid === marker.getTitle()).nid + ' popup';
  }
}
