import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterOutlet, Router, NavigationEnd } from '@angular/router';
import { NavbarComponent } from './components/navbar/navbar.component';
import { SidebarComponent } from './components/sidebar/sidebar.component';
import { AuthService } from './services/auth.service';
import { OfflineSyncService } from './services/offline-sync.service';
import { filter } from 'rxjs/operators';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, RouterOutlet, NavbarComponent, SidebarComponent],
  templateUrl: './app.component.html',
  styleUrls: ['./app.css']
})
export class AppComponent implements OnInit {
  title = 'FlexZone Gym Management';
  showSidebar = false;
  isOffline = false;
  pendingCount = 0;
  syncSuccess = false;
  
  constructor(
    public authService: AuthService,
    private router: Router,
    private offlineSyncService: OfflineSyncService
  ) {}

  ngOnInit(): void {
    // Connection status tracking
    this.offlineSyncService.isOffline$.subscribe(offline => {
      this.isOffline = offline;
    });
    this.offlineSyncService.pendingSyncCount$.subscribe(count => {
      this.pendingCount = count;
    });
    this.offlineSyncService.syncSuccess$.subscribe(success => {
      this.syncSuccess = success;
    });

    // Determine initial visibility
    this.updateSidebarVisibility(this.router.url);

    // Track routing changes
    this.router.events.pipe(
      filter((event): event is NavigationEnd => event instanceof NavigationEnd)
    ).subscribe((event: NavigationEnd) => {
      this.updateSidebarVisibility(event.urlAfterRedirects || event.url);
    });
  }

  private updateSidebarVisibility(url: string): void {
    const currentUrl = url.split('?')[0];
    const excludedRoutes = ['/', '/login', '/signup', ''];
    this.showSidebar = this.authService.isLoggedIn() && !excludedRoutes.includes(currentUrl);
  }
}
