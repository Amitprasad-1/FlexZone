import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-forgot-password',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './forgot-password.component.html'
})
export class ForgotPasswordComponent {
  email = '';
  newPassword = '';
  confirmPassword = '';
  step = 1; // 1: Verify Email, 2: Reset Password, 3: Success
  loading = false;
  errorMessage = '';
  successMessage = '';

  constructor(private authService: AuthService) {}

  verifyEmail(): void {
    if (!this.email) {
      this.errorMessage = 'Please enter your email address.';
      return;
    }

    this.loading = true;
    this.errorMessage = '';
    this.authService.forgotPassword(this.email).subscribe({
      next: (response) => {
        this.loading = false;
        this.step = 2;
      },
      error: (err) => {
        this.loading = false;
        this.errorMessage = err.error || 'Email verification failed. Please try again.';
      }
    });
  }

  resetPassword(): void {
    if (!this.newPassword || !this.confirmPassword) {
      this.errorMessage = 'Please fill out all fields.';
      return;
    }

    if (this.newPassword !== this.confirmPassword) {
      this.errorMessage = 'Passwords do not match.';
      return;
    }

    if (this.newPassword.length < 6) {
      this.errorMessage = 'Password must be at least 6 characters long.';
      return;
    }

    this.loading = true;
    this.errorMessage = '';
    this.authService.resetPassword(this.email, this.newPassword).subscribe({
      next: (response) => {
        this.loading = false;
        this.successMessage = response || 'Password reset successfully!';
        this.step = 3;
      },
      error: (err) => {
        this.loading = false;
        this.errorMessage = err.error || 'Failed to reset password. Please try again.';
      }
    });
  }
}
