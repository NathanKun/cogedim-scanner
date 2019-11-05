import {NgModule} from '@angular/core';
import {Routes, RouterModule} from '@angular/router';
import {ProgramComponent} from './program/program.component';
import {AuthGuard} from './service/authguard';

const routes: Routes = [
  {
    path: 'programs/:id',
    component: ProgramComponent,
    canActivate: [AuthGuard]
  }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule {
}
