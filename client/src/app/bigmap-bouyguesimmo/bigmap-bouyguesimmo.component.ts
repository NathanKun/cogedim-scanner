import {Component, OnInit} from '@angular/core';
import {MapMarker} from '@angular/google-maps';
import {CookieService} from 'ngx-cookie-service';
import {DomSanitizer, SafeHtml, Title} from '@angular/platform-browser';
import {environment} from '../../environments/environment';
import {ProgramService} from '../service/program.service';
import {MapInitService} from '../service/mapinit.service';
import {BigmapComponent} from '../bigmap/bigmap.component';
import {BouyguesimmoService} from '../service/bouyguesimmo.service';
import {BouyguesImmoProgram} from "../model/bouyguesimmoprogram";
import {BigMapPin} from "../model/bigmappin";

@Component({
  selector: 'app-bigmap-bouyguesimmo',
  templateUrl: './bigmap-bouyguesimmo.component.html',
  styleUrls: ['./bigmap-bouyguesimmo.component.css']
})
export class BigmapBouyguesimmoComponent extends BigmapComponent implements OnInit {

  hidPinsCookieName = 'hided_pins_bouyguesimmo';
  programs: BouyguesImmoProgram[];
  infoWindowProgram: BouyguesImmoProgram;
  mapInfoWindowInnerHtml: SafeHtml;

  constructor(protected cookieService: CookieService,
              protected titleService: Title,
              protected programService: ProgramService,
              protected bouyguesimmoService: BouyguesimmoService,
              protected mapInitService: MapInitService,
              private domSanitizer: DomSanitizer) {
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
      programs => {
        for (const p of programs) {
          p.hid = this.isPinHid(p.nid);
          this.markerConfigs.push({
              position: {
                lat: parseFloat(p.lat),
                lng: parseFloat(p.lng)
              },
              title: p.nid,
              options: {
                animation: google.maps.Animation.DROP,
                visible: !p.hid
              }
            } as MapMarker
          )
        }

        this.programs = programs;
      }
    );
  }

  markerClick(marker: MapMarker) {
    this.infoWindowLoaded = false;
    const p = this.programs.find(p => p.nid === marker.getTitle());
    this.infoWindowProgram = p;

    const article = new DOMParser().parseFromString(p.teaser, 'text/html');
    article.querySelectorAll("a")
      .forEach(
        a => {
          a.setAttribute(
            'href',
            'https://www.bouygues-immobilier.com' + a.getAttribute('href'));
          a.setAttribute('target', '_blank');
        }
      );
    const domArticle = article.querySelector('article');
    domArticle.setAttribute('class', domArticle.getAttribute('class') + ' row');

    this.mapInfoWindowInnerHtml = this.domSanitizer.bypassSecurityTrustHtml(article.body.innerHTML);
    this.infoWindowLoaded = true;
    this.infoWindow.open(marker);
  }

  setPinHidBouygues(pin: BouyguesImmoProgram, setHided: boolean) {
    // this update the opened info window
    pin.hid = !pin.hid;
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

  showMarkers(hideHid: boolean, animate: boolean) {
    this.markerConfigs.length = 0;

    this.programs.forEach(item => {
      if (!hideHid || !item.hid) {
        this.markerConfigs.push({
          position: {
            lat: parseFloat(item.lat),
            lng: parseFloat(item.lng)
          },
          title: item.nid,
          options: {
            animation: animate ? google.maps.Animation.DROP : null,
          },
        } as MapMarker);
      }
    });
  }

  changeHideState() {
    this.hideHidPins = !this.hideHidPins;
    this.showMarkers(this.hideHidPins, false);
  }
}
