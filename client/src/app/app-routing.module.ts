import {NgModule} from '@angular/core';
import {Routes, RouterModule} from '@angular/router';
import {CarListComponent} from './car-list/car-list.component';
import {CarEditComponent} from './car-edit/car-edit.component';
import {AuthGuard} from './service/authguard';

const routes: Routes = [
  {
    path: 'car-list',
    component: CarListComponent,
    canActivate: [AuthGuard]
  },
  {
    path: 'car-add',
    component: CarEditComponent,
    canActivate: [AuthGuard]
  },
  {
    path: 'car-edit/:id',
    component: CarEditComponent,
    canActivate: [AuthGuard]
  }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule {
}
