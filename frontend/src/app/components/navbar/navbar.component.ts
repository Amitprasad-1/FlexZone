import { Component, HostListener, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './navbar.component.html'
})
export class NavbarComponent implements OnInit {
  deferredPrompt: any = null;
  canInstall = false;
  showMobileBanner = true;

  constructor(public authService: AuthService, private router: Router) {}

  ngOnInit(): void {
    const isDismissed = localStorage.getItem('flexzone-pwa-banner-dismissed') === 'true';
    this.showMobileBanner = !isDismissed;

    // Check if the event was already captured globally
    if ((window as any).deferredPrompt) {
      this.deferredPrompt = (window as any).deferredPrompt;
      this.canInstall = true;
    }

    // Listen for custom event if captured during page load
    window.addEventListener('pwa-prompt-available', () => {
      this.deferredPrompt = (window as any).deferredPrompt;
      this.canInstall = true;
    });
  }

  @HostListener('window:beforeinstallprompt', ['$event'])
  onBeforeInstallPrompt(e: Event): void {
    e.preventDefault();
    this.deferredPrompt = e;
    this.canInstall = true;
    (window as any).deferredPrompt = e;
  }

  @HostListener('window:appinstalled', ['$event'])
  onAppInstalled(e: Event): void {
    console.log('FlexZone App installed successfully!');
    this.deferredPrompt = null;
    this.canInstall = false;
    (window as any).deferredPrompt = null;
  }

  installApp(): void {
    if (!this.deferredPrompt) return;

    this.deferredPrompt.prompt();

    this.deferredPrompt.userChoice.then((choiceResult: any) => {
      if (choiceResult.outcome === 'accepted') {
        console.log('User accepted the PWA install prompt');
      } else {
        console.log('User dismissed the PWA install prompt');
      }
      this.deferredPrompt = null;
      this.canInstall = false;
      (window as any).deferredPrompt = null;
    });
  }

  dismissBanner(): void {
    this.showMobileBanner = false;
    localStorage.setItem('flexzone-pwa-banner-dismissed', 'true');
  }

  onLogout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
