import {Injectable} from '@angular/core';
import {GoogleMap} from '@angular/google-maps';
import {ProgramService} from './program.service';

declare const MeasureTool: any;

@Injectable({
  providedIn: 'root'
})
export class MapInitService {

  constructor(private programService: ProgramService) {
  }

  public initGoogleMap(map: GoogleMap) {
    // google maps mesure tool
    // tslint:disable-next-line:no-unused-expression
    new MeasureTool(map._googleMap, {
      contextMenu: true,
      showSegmentLength: true,
      tooltip: true,
      unit: MeasureTool.UnitTypeId.METRIC // metric, imperial, or nautical
    });

    // MeasureTool init will setClickableIcons to false
    map._googleMap.setClickableIcons(true);

    // show secure zones
    this.programService.getGoodCities().subscribe(
      (data) => {
        map._googleMap.data.addGeoJson(data);
        map._googleMap.data.setStyle({
          fillColor: '#66CCFF',
          strokeWeight: 1,
          strokeOpacity: 0.5,
          strokeColor: '#CCCCCC',
          clickable: false
        });
      }
    );
  }
}
