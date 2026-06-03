import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './login.component.html'
})
export class LoginComponent {
  credentials = { username: '', password: '' };
  errorMessage = '';
  loading = false;

  constructor(private authService: AuthService, private router: Router) {
    if (this.authService.isLoggedIn()) {
      this.redirectByRole();
    }
  }

  onSubmit(): void {
    this.loading = true;
    this.errorMessage = '';
    this.authService.login(this.credentials).subscribe({
      next: () => {
        this.loading = false;
        this.redirectByRole();
      },
      error: err => {
        this.loading = false;
        this.errorMessage = err.error || 'Invalid credentials. Please try again.';
      }
    });
  }

  private redirectByRole(): void {
    const role = this.authService.getRole();
    if (role === 'ADMIN') {
      this.router.navigate(['/admin']);
    } else if (role === 'MEMBER') {
      this.router.navigate(['/member']);
    } else if (role === 'TRAINER') {
      this.router.navigate(['/checkin']);
    } else {
      this.router.navigate(['/']);
    }
  }
}
