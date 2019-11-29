import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {BigmapComponent} from './bigmap.component';

describe('BigmapComponent', () => {
  let component: BigmapComponent;
  let fixture: ComponentFixture<BigmapComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [BigmapComponent]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(BigmapComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
