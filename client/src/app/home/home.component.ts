import {AfterViewInit, Component, ElementRef, HostListener, OnInit, QueryList, Renderer2, ViewChild, ViewChildren} from '@angular/core';
import {ProgramService} from '../service/program.service';
import {ProgramDateLot} from '../model/programdatelot';
import {GoogleMap, MapMarker} from '@angular/google-maps';
import {CookieService} from 'ngx-cookie-service';
import {Title} from '@angular/platform-browser';
import {environment} from '../../environments/environment';
import {ScrollService} from '../service/scroll.service';
import {Router} from '@angular/router';

declare const MeasureTool: any;

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.scss']
})
export class HomeComponent implements OnInit, AfterViewInit {
  private hidProgramsCookieName = 'hid_programs';
  hideHidPrograms = true;

  @ViewChildren('programcard') programcards: QueryList<ElementRef>;
  programDateLots: ProgramDateLot[];

  zoom = 13;
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

  @ViewChild(GoogleMap) map: GoogleMap;
  @ViewChildren('markerElem') markerElements: QueryList<MapMarker>;
  markerConfigs: MapMarker[] = [];

  constructor(private renderer: Renderer2,
              private router: Router,
              private cookieService: CookieService,
              private titleService: Title,
              private programService: ProgramService,
              private scrollService: ScrollService) {
  }

  async ngOnInit() {
    this.titleService.setTitle(environment.title);

    this.programService.getProgramDateLots().subscribe(
      async programDateLots => {
        this.programDateLots = programDateLots;

        for (const p of this.programDateLots) {
          // hided program
          p.hided = this.cookieIsProgramHided(p.program.programNumber);

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

        // move all hided program to bottom
        let iTo = this.programDateLots.length;
        for (let i = 0; i < iTo; i++) {
          const pdl = this.programDateLots[i];
          if (pdl.hided) {
            iTo--;
            this.programDateLots.splice(i, 1);
            this.programDateLots.push(pdl);
          }
        }

        // request delivery info may take lots of time if the backend has no cache
        // so do this at the end of subscribe()
        for (const p of this.programDateLots) {
          // delivery info
          try {
            p.deliveryInfoHtml = await this.programService.getProgramPageDeliveryInfo(p.program.url).toPromise();
          } catch (e) {
            console.log('programService.getProgramPageDeliveryInfo(url) error, url = ' + p.program.url);
            console.log('flush and retry');
            await this.programService.flushUrl(p.program.url).toPromise();
            try {
              p.deliveryInfoHtml = await this.programService.getProgramPageDeliveryInfo(p.program.url).toPromise();
            } catch (e) {
              console.log('programService.getProgramPageDeliveryInfo(url) error, url = ' + p.program.url);
              console.log(e);
            }
          }
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

  ngAfterViewInit() {
    const transitLayer = new google.maps.TransitLayer();
    transitLayer.setMap(this.map._googleMap);

    this.programcards.changes.subscribe(
      res => this.programcards = res
    );

    // restore scroll position
    this.scrollService.scrollHome();

    // google maps mesure tool
    // tslint:disable-next-line:no-unused-expression
    new MeasureTool(this.map._googleMap, {
      contextMenu: true,
      showSegmentLength: true,
      tooltip: true,
      unit: MeasureTool.UnitTypeId.METRIC // metric, imperial, or nautical
    });
  }

  @HostListener('window:scroll', ['$event'])
  onScroll() {
    this.scrollService.recordHomePosition();
  }

  markerClick(marker: MapMarker) {
    const index = this.programDateLots.findIndex((pdl) => pdl.program.programName === marker.getTitle());
    this.programcards.filter((item, i) => i === index)[0].nativeElement.scrollIntoView({
      behavior: 'smooth',
      block: 'end'
    });
    this.animateMarker(marker);
  }

  programCardLocationClick(programName) {
    this.animateMarker(this.markerElements.find(m => m.getTitle() === programName));
  }

  hideProgramClick(pdl: ProgramDateLot) {
    pdl.hided = true;
    this.cookieSetProgramHided(pdl.program.programNumber, true);
  }

  unhideProgramClick(pdl: ProgramDateLot) {
    pdl.hided = false;
    this.cookieSetProgramHided(pdl.program.programNumber, false);
  }

  showAllHidedPrograms(show: boolean) {
    this.hideHidPrograms = !show;
  }

  private animateMarker(marker: MapMarker) {
    this.map.panTo(marker.getPosition());
    this.map._googleMap.setZoom(13);
    marker._marker.setAnimation(google.maps.Animation.BOUNCE);
    setTimeout(() => marker._marker.setAnimation(null), 1500);
  }

  private cookieIsProgramHided(programNumber: string) {
    if (this.cookieService.check(this.hidProgramsCookieName)) {
      const cookieStr = this.cookieService.get(this.hidProgramsCookieName);
      const hidePrograms = JSON.parse(cookieStr) as string[];
      return hidePrograms.indexOf(programNumber) >= 0;
    } else {
      return false;
    }
  }

  private cookieSetProgramHided(programNumber: string, setHided: boolean) {
    // read cookie
    let hidedPrograms: string[];
    if (this.cookieService.check(this.hidProgramsCookieName)) {
      const cookieStr = this.cookieService.get(this.hidProgramsCookieName);
      hidedPrograms = JSON.parse(cookieStr) as string[];
    } else {
      hidedPrograms = [];
    }

    if (setHided) {
      // hide program
      if (hidedPrograms.indexOf(programNumber) === -1) {
        hidedPrograms.push(programNumber);
      }
    } else {
      // unhide program
      const index = hidedPrograms.indexOf(programNumber);
      if (index >= 0) {
        hidedPrograms.splice(index, 1);
      }
    }

    // save
    this.cookieService.set(this.hidProgramsCookieName, JSON.stringify(hidedPrograms), 10 * 365, '/');
  }
}
