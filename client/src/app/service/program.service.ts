import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {map, tap} from 'rxjs/operators';
import {ProgramDateLot} from '../model/programdatelot';
import {BaseService} from './base.service';
import {Observable, of} from 'rxjs';
import {DomSanitizer, SafeHtml} from '@angular/platform-browser';
import {AuthService} from './auth.service';
import {BigMapPin} from '../model/bigmappin';
import {BigMapPinDetail} from '../model/bigmappindetail';

@Injectable({
  providedIn: 'root'
})
export class ProgramService extends BaseService {

  constructor(private http: HttpClient,
              private sanitizer: DomSanitizer,
              private authService: AuthService) {
    super();
    this.programPageCache = new Map<string, string>();
    this.bigMapPinDetailCache = new Map<string, BigMapPinDetail>();
  }

  private programDateLotCache: ProgramDateLot[];
  private programPageCache: Map<string, string>; // program url  => html str
  private bigMapPinsCache: BigMapPin[];
  private bigMapPinDetailCache: Map<string, BigMapPinDetail>; // program number  => BigMapPinDetail object

  public getBigmapPinDetail(pin: BigMapPin): Observable<BigMapPinDetail> {
    if (this.bigMapPinDetailCache.has(pin.nid)) {
      return of(this.bigMapPinDetailCache.get(pin.nid));
    } else {
      return this.fetchBigmapPinDetail(pin);
    }
  }

  private fetchBigmapPinDetail(pin: BigMapPin): Observable<BigMapPinDetail> {
    return this.http.get<string>(
      this.convertUrlToBackendResourceUrl('/marker/' + pin.nid + '/', true),
      {responseType: 'text' as 'json'})
      .pipe(
        map(
          str => {
            const json: any = JSON.parse(str);
            const htmlStr = json.markup;
            const article = new DOMParser().parseFromString(htmlStr, 'text/html');

            let imgUrl = article.querySelector('img[src^="/sites"]').getAttribute('src').split('?')[0];
            imgUrl = this.convertUrlToBackendResourceUrl(imgUrl, true);

            const programNameElement = article.querySelector('h2');
            const programName = programNameElement != null ? programNameElement.textContent : '';

            const postalCodeElement = article.querySelector('span[itemprop="postalCode"]');
            const postalCode = postalCodeElement != null ? postalCodeElement.textContent : '';

            const addressLocalityElement = article.querySelector('span[itemprop="addressLocality"]');
            const addressLocality = addressLocalityElement != null ? addressLocalityElement.textContent : '';

            const summaryElement = article.querySelector('.summary');
            const summary = summaryElement != null ? summaryElement.innerHTML : '';

            const urlElement = article.querySelector('a');
            const url = urlElement != null ? ('https://www.cogedim.com/' + urlElement.getAttribute('href')) : '#';

            const bigMapPinDetail = {
              lat: pin.lat,
              lng: pin.lng,
              nid: pin.nid,
              hid: pin.hid,
              imgUrl,
              programName,
              postalCode,
              addressLocality,
              summary,
              url
            };
            this.bigMapPinDetailCache.set(pin.nid, bigMapPinDetail);

            return bigMapPinDetail;
          }
        )
      );
  }

  public getBigmapPins(): Observable<BigMapPin[]> {
    if (this.bigMapPinsCache) {
      return of(this.bigMapPinsCache);
    } else {
      return this.fetchBigmapPins();
    }
  }

  private fetchBigmapPins(): Observable<BigMapPin[]> {
    const url = 'https://www.cogedim.com/programme-immobilier-neuf/PgX6/';
    return this.http.get<string>(
      this.baseurl + '/program?url=' + url,
      {responseType: 'text' as 'json'})
      .pipe(
        map(
          str => {
            const doc = new DOMParser().parseFromString(str, 'text/html');
            const script = doc.querySelector('script[data-drupal-selector="drupal-settings-json"]');
            const json: any = JSON.parse(script.innerHTML);
            this.bigMapPinsCache = json.nearbyPrograms;
            return this.bigMapPinsCache;
          }
        )
      );
  }

  public getProgramDateLots(): Observable<ProgramDateLot[]> {
    if (this.programDateLotCache) {
      return of(this.programDateLotCache);
    } else {
      return this.fetchProgramDateLots();
    }
  }

  private fetchProgramDateLots(): Observable<ProgramDateLot[]> {
    return this.http.get<ProgramDateLot[]>(
      this.baseurl + '/programs')
      .pipe(
        tap(res => this.programDateLotCache = res),
        map(res => {
          res.forEach(p => {
            // convert the object to a map
            p.dateMap = new Map(Object.entries(p.dateMap));
            p.program.imgUrl = this.convertUrlToBackendResourceUrl(p.program.imgUrl, false);

            // set the last day lot count prop
            const values = Array.from(p.dateMap.values());
            p.lastDayLotCount = values[values.length - 1].length;
            const lastDaySortedLots = values[values.length - 1].sort(
              (a, b) =>
                +a.price.replace(' ', '').replace('€', '')
                <
                +b.price.replace(' ', '').replace('€', '')
                  ?
                  -1 : 1
            );
            if (lastDaySortedLots.length) {
              p.lastDayMinPrice = lastDaySortedLots[0].price;
            }
          });
          return res;
        })
      );
  }

  public getProgramPageDeliveryInfo(url: string): Observable<SafeHtml> {
    return this.getProgramPageMainInfo(url).pipe(
      map(
        html => {
          const doc = new DOMParser().parseFromString(html.toString(), 'text/html');
          const div = doc.querySelector('.informations');
          return this.sanitizer.bypassSecurityTrustHtml(div.innerHTML);
        }
      )
    );
  }

  public getProgramPageMainInfo(url: string): Observable<SafeHtml> {
    return this.fetchProgramPage(url).pipe(
      map(
        str => {
          const doc = new DOMParser().parseFromString(str, 'text/html');
          const div = doc.querySelector('#main_info');

          // convert <v-icon> to material icons
          div.querySelectorAll('v-icon').forEach(
            vIcon => {
              const iMatIcon = document.createElement('i');
              iMatIcon.innerHTML = vIcon.innerHTML;
              iMatIcon.setAttribute('class', 'material-icons icons');
              vIcon.parentNode.replaceChild(iMatIcon, vIcon);
            }
          );

          // convert <address> to span
          div.querySelectorAll('address').forEach(
            addr => {
              const span = document.createElement('span');
              span.innerHTML = addr.innerText;
              span.setAttribute('class', 'line');
              addr.parentNode.replaceChild(span, addr);
            }
          );

          // remove .event-button
          div.querySelectorAll('.event-button').forEach(b => b.remove());

          // remove v-btn
          div.querySelectorAll('v-btn').forEach(b => b.remove());

          // remove .hidden-xs, info may be duplicated with .hidden-xl
          div.querySelectorAll('.hidden-xs').forEach(b => b.remove());

          // remove .anchors
          div.querySelectorAll('.anchors').forEach(b => b.remove());

          return this.sanitizer.bypassSecurityTrustHtml(div.innerHTML);
        }
      )
    );
  }

  public getProgramPageSalesInfo(url: string): Observable<SafeHtml> {
    return this.fetchProgramPage(url).pipe(
      map(
        str => {
          const doc = new DOMParser().parseFromString(str, 'text/html');
          const div = doc.querySelector('.paragraphs-sales_office');

          // update images src, add cogedim's domain
          div.querySelectorAll('img[src^="/"]').forEach(
            e => e.setAttribute(
              'src',
              this.convertUrlToBackendResourceUrl(e.getAttribute('src'), true)
            )
          );

          // remove sales office section
          div.querySelector('.sales-office').remove();

          // sanitize html
          return this.sanitizer.bypassSecurityTrustHtml(div.innerHTML);
        }
      )
    );
  }

  private fetchProgramPage(url: string): Observable<string> {
    if (this.programPageCache.has(url)) {
      return of(this.programPageCache.get(url));
    } else {
      return this.http.get<string>(
        this.baseurl + '/program?url=' + url,
        {responseType: 'text' as 'json'}
      ).pipe(
        tap(
          res => {
            // cache result
            this.programPageCache.set(url, res);
            return res;
          }
        )
      );
    }
  }

  private convertUrlToBackendResourceUrl(url: string, addCogedimDomain: boolean): string {
    if (addCogedimDomain) {
      url = 'https://www.cogedim.com' + url;
    }

    return this.baseurl + '/resource?resourceUrl=' + url + '&token=' + this.authService.getAccessToken();
  }
}
