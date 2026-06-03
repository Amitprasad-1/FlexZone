import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CheckInService } from '../../services/checkin.service';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-checkin',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './checkin.component.html'
})
export class CheckInComponent implements OnInit {
  memberIdInput: number | null = null;
  todayCheckIns: any[] = [];
  loading = false;
  errorMessage = '';
  successMessage = '';

  constructor(
    private checkInService: CheckInService,
    public authService: AuthService
  ) {}

  ngOnInit(): void {
    this.loadTodayCheckIns();
  }

  loadTodayCheckIns(): void {
    this.checkInService.getTodayCheckIns().subscribe({
      next: data => this.todayCheckIns = data,
      error: err => this.errorMessage = 'Failed to load attendance log.'
    });
  }

  onSubmitCheckIn(): void {
    if (!this.memberIdInput) {
      return;
    }
    this.loading = true;
    this.errorMessage = '';
    this.successMessage = '';

    this.checkInService.scanCheckIn(this.memberIdInput).subscribe({
      next: data => {
        this.loading = false;
        this.successMessage = `Attendance logged successfully for ${data.memberName}!`;
        this.memberIdInput = null;
        this.loadTodayCheckIns();
      },
      error: err => {
        this.loading = false;
        this.errorMessage = err.error || 'Failed to check in member. Check if membership is expired or ID is invalid.';
      }
    });
  }
}
