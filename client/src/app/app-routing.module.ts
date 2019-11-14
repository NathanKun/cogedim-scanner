import {CommonModule} from '@angular/common';
import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {HTTP_INTERCEPTORS, HttpClientModule} from '@angular/common/http';
import {MatButtonModule} from '@angular/material/button';
import {MatCardModule} from '@angular/material/card';
import {MatIconModule} from '@angular/material/icon';
import {HomeComponent} from './home/home.component';
import {LoginComponent} from './login/login.component';
import {AuthInterceptor} from './interceptor/auth.interceptor';
import {ProgramComponent} from './program/program.component';
import {AuthGuard} from './service/authguard';
import {MatProgressSpinnerModule} from '@angular/material/progress-spinner';
import {GoogleMapsModule} from '@angular/google-maps';
import {BigmapComponent} from './bigmap/bigmap.component';

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
  declarations: [
    HomeComponent
  ],
  imports: [
    CommonModule,
    HttpClientModule,
    RouterModule.forRoot(routes),
    MatButtonModule,
    MatCardModule,
    MatIconModule,
    MatProgressSpinnerModule,
    GoogleMapsModule
  ],
  providers: [
    {provide: HTTP_INTERCEPTORS, useClass: AuthInterceptor, multi: true}
  ],
  exports: [RouterModule]
})
export class AppRoutingModule {
}
