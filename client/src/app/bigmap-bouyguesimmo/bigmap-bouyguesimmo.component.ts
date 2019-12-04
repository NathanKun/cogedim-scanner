import {AfterViewInit, Component, ElementRef, OnInit, QueryList, ViewChild, ViewChildren} from '@angular/core';
import {GoogleMap, MapInfoWindow, MapMarker} from '@angular/google-maps';
import {CookieService} from 'ngx-cookie-service';
import {Title} from '@angular/platform-browser';
import {environment} from '../../environments/environment';
import {ProgramService} from '../service/program.service';
import {BigMapPin} from '../model/bigmappin';
import {BigMapPinDetail} from '../model/bigmappindetail';
import {MapInitService} from '../service/mapinit.service';
import {BigmapComponent} from '../bigmap/bigmap.component';
import {BouyguesimmoService} from '../service/bouyguesimmo.service';

@Component({
  selector: 'app-bigmap-bouyguesimmo',
  templateUrl: './bigmap-bouyguesimmo.component.html',
  styleUrls: ['./bigmap-bouyguesimmo.component.css']
})
export class BigmapBouyguesimmoComponent extends BigmapComponent implements OnInit {

  hidPinsCookieName = 'hided_pins_bouyguesimmo';

  private nids: string[];

  constructor(protected cookieService: CookieService,
              protected titleService: Title,
              protected programService: ProgramService,
              protected bouyguesimmoService: BouyguesimmoService,
              protected mapInitService: MapInitService) {
    super(cookieService, titleService, programService, mapInitService);
  }

  ngOnInit() {
    this.titleService.setTitle(environment.title + ' - Big Map Bouygues Immobilier');
    this.readCookie();

    navigator.geolocation.getCurrentPosition(position => {
      this.center = {
        lat: position.coords.latitude,
        lng: position.coords.longitude,
      };
    });

    this.bouyguesimmoService.fetchSearchResult().subscribe(
      nids => {
        this.nids = nids;

        for (const nid of this.nids) {
          this.bouyguesimmoService.fetchDetail(nid).subscribe(
            html => {

            }
          );
        }
      }
    );
  }

  markerClick(marker: MapMarker) {
    this.infoWindowLoaded = false;
    this.infoWindow.open(marker);
    const pin = this.bigMapPins.find(p => p.nid === marker.getTitle());
    this.programService.getBigmapPinDetail(pin).subscribe(
      obj => {
        this.infoWindowBigMapPinDetail = obj;
        this.infoWindowLoaded = true;
        this.infoWindow.close();
        this.infoWindow.open(marker);
      }
    );
  }

  changeHideState() {
    this.hideHidPins = !this.hideHidPins;
    this.showMarkers(this.hideHidPins, false);
  }
}
