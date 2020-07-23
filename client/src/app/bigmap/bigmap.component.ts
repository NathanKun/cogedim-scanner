import {AfterViewInit, Component, ElementRef, OnInit, ViewChild} from '@angular/core';
import {GoogleMap, MapInfoWindow, MapMarker} from '@angular/google-maps';
import {CookieService} from 'ngx-cookie-service';
import {Title} from '@angular/platform-browser';
import {environment} from '../../environments/environment';
import {ProgramService} from '../service/program.service';
import {BigMapPin} from '../model/bigmappin';
import {BigMapPinDetail} from '../model/bigmappindetail';
import {MapInitService} from '../service/mapinit.service';
import {MarkerConfig} from '../model/markerconfig';

@Component({
  selector: 'app-bigmap',
  templateUrl: './bigmap.component.html',
  styleUrls: ['./bigmap.component.css']
})
export class BigmapComponent implements OnInit, AfterViewInit {

  hideHidPins = true;
  @ViewChild(GoogleMap) map: GoogleMap;
  zoom = 12;
  center: google.maps.LatLngLiteral;
  options: google.maps.MapOptions = {
    mapTypeId: 'roadmap',
    zoomControl: true,
    scaleControl: true,
    scrollwheel: true,
    disableDoubleClickZoom: true,
    maxZoom: 18,
    minZoom: 8,
  };
  markerConfigs: MarkerConfig[] = [];
  @ViewChild(MapInfoWindow) infoWindow: MapInfoWindow;
  infoWindowConfig: google.maps.InfoWindowOptions = {
    disableAutoPan: false
  };
  infoWindowBigMapPinDetail: BigMapPinDetail;
  infoWindowLoaded = false;
  bigMapPins: BigMapPin[];
  @ViewChild('changeHideStateButton') changeHideStateButton: ElementRef;
  hidPins: string[];
  hidPinsCookieName = 'hided_pins';

  constructor(protected cookieService: CookieService,
              protected titleService: Title,
              protected programService: ProgramService,
              protected mapInitService: MapInitService) {
  }

  ngOnInit() {
    this.titleService.setTitle(environment.title + ' - Big Map');
    this.readCookie();

    navigator.geolocation.getCurrentPosition(position => {
      this.center = {
        lat: position.coords.latitude,
        lng: position.coords.longitude,
      };
    });

    this.programService.getBigmapPins().subscribe(
      bps => {
        this.bigMapPins = bps;
        this.bigMapPins.forEach(p => p.hid = this.isPinHid(p.nid));
        this.showMarkers(true, true);
      }
    );
  }

  ngAfterViewInit(): void {
    this.map.googleMap.controls[google.maps.ControlPosition.BOTTOM_CENTER].push(this.changeHideStateButton.nativeElement);
    this.changeHideStateButton.nativeElement.setAttribute('class', ''); // remove the d-none class

    const transitLayer = new google.maps.TransitLayer();
    transitLayer.setMap(this.map.googleMap);

    // init google map
    this.mapInitService.initGoogleMap(this.map);
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

  setPinHid(pin: BigMapPin, setHided: boolean) {
    // this update the opened info window
    pin.hid = !pin.hid;

    // update the marker map
    const pinInArray = this.bigMapPins.find(p => p.nid === pin.nid);
    pinInArray.hid = !pinInArray.hid;
    this.showMarkers(this.hideHidPins, false);

    // update cookie
    if (setHided) {
      // hide
      if (this.hidPins.indexOf(pin.nid) === -1) {
        this.hidPins.push(pin.nid);
      }
    } else {
      // unhide
      const index = this.hidPins.indexOf(pin.nid);
      if (index >= 0) {
        this.hidPins.splice(index, 1);
      }
    }

    this.cookieService.set(this.hidPinsCookieName, JSON.stringify(this.hidPins), 10 * 365, '/');
  }

  changeHideState() {
    this.hideHidPins = !this.hideHidPins;
    this.showMarkers(this.hideHidPins, false);
  }

  protected isPinHid(nid: string) {
    return this.hidPins.indexOf(nid) >= 0;
  }

  protected showMarkers(hideHid: boolean, animate: boolean) {
    this.markerConfigs.length = 0;

    this.bigMapPins.forEach(item => {
      if (!hideHid || !item.hid) {
        this.markerConfigs.push({
          position: {
            lat: item.lat,
            lng: item.lng
          },
          title: item.nid,
          options: {
            animation: animate ? google.maps.Animation.DROP : null,
          },
        } as MarkerConfig);
      }
    });
  }

  protected readCookie() {
    if (this.cookieService.check(this.hidPinsCookieName)) {
      const cookieStr = this.cookieService.get(this.hidPinsCookieName);
      this.hidPins = JSON.parse(cookieStr) as string[];
    } else {
      this.hidPins = [];
    }
  }
}
