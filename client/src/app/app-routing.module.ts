import {CommonModule} from '@angular/common';
import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {MatButtonModule} from '@angular/material/button';
import {MatCardModule} from '@angular/material/card';
import {MatIconModule} from '@angular/material/icon';
import {HomeComponent} from './home/home.component';
import {LoginComponent} from './login/login.component';
import {ProgramComponent} from './program/program.component';
import {AuthGuard} from './service/authguard';
import {MatProgressSpinnerModule} from '@angular/material/progress-spinner';
import {GoogleMapsModule} from '@angular/google-maps';
import {BigmapComponent} from './bigmap/bigmap.component';
import {LazyLoadImageModule} from 'ng-lazyload-image';

const routes: Routes = [
  {path: '', redirectTo: '/home', pathMatch: 'full'},
  {
    path: 'login',
    component: LoginComponent
  },
  {
    path: 'home',
    component: HomeComponent,
    canActivate: [AuthGuard]
  },
  {
    path: 'programs/:programNumber',
    component: ProgramComponent,
    canActivate: [AuthGuard]
  },
  {
    path: 'bigmap',
    component: BigmapComponent,
    canActivate: [AuthGuard]
  }
];

@NgModule({
  declarations: [],
  imports: [
    CommonModule,
    RouterModule.forRoot(routes),
    MatButtonModule,
    MatCardModule,
    MatIconModule,
    MatProgressSpinnerModule,
    GoogleMapsModule,
    LazyLoadImageModule
  ],
  providers: [],
  exports: [RouterModule]
})
export class AppRoutingModule {
}
