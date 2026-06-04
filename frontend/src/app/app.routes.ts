import { Routes } from '@angular/router';
import { LoginComponent } from './components/login/login.component';
import { SignupComponent } from './components/signup/signup.component';
import { ForgotPasswordComponent } from './components/forgot-password/forgot-password.component';
import { AdminDashboardComponent } from './components/admin-dashboard/admin-dashboard.component';
import { MemberPanelComponent } from './components/member-panel/member-panel.component';
import { BookingComponent } from './components/booking/booking.component';
import { CheckInComponent } from './components/checkin/checkin.component';
import { EShopComponent } from './components/eshop/eshop.component';
import { HomeComponent } from './components/home/home.component';
import { authGuard, roleGuard } from './auth.guard';

export const routes: Routes = [
  { path: '', component: HomeComponent },
  { path: 'login', component: LoginComponent },
  { path: 'signup', component: SignupComponent },
  { path: 'forgot-password', component: ForgotPasswordComponent },
  { 
    path: 'admin', 
    component: AdminDashboardComponent, 
    canActivate: [authGuard, roleGuard(['ADMIN'])] 
  },
  { 
    path: 'member', 
    component: MemberPanelComponent, 
    canActivate: [authGuard, roleGuard(['MEMBER'])] 
  },
  { 
    path: 'bookings', 
    component: BookingComponent, 
    canActivate: [authGuard] 
  },
  { 
    path: 'checkin', 
    component: CheckInComponent, 
    canActivate: [authGuard, roleGuard(['ADMIN', 'TRAINER'])] 
  },
  { 
    path: 'shop', 
    component: EShopComponent, 
    canActivate: [authGuard] 
  },
  { path: '**', redirectTo: '' }
];
