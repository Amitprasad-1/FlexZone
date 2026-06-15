import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CheckInService } from '../../services/checkin.service';
import { AuthService } from '../../services/auth.service';
import { Html5Qrcode } from 'html5-qrcode';

@Component({
  selector: 'app-checkin',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './checkin.component.html'
})
export class CheckInComponent implements OnInit, OnDestroy {
  memberIdInput: string = '';
  todayCheckIns: any[] = [];
  loading = false;
  errorMessage = '';
  successMessage = '';

  isCameraActive = false;
  html5QrCode: any = null;

  constructor(
    private checkInService: CheckInService,
    public authService: AuthService
  ) {}

  ngOnInit(): void {
    this.loadTodayCheckIns();
  }

  ngOnDestroy(): void {
    this.stopCamera();
  }

  loadTodayCheckIns(): void {
    this.checkInService.getTodayCheckIns().subscribe({
      next: data => this.todayCheckIns = data,
      error: err => this.errorMessage = 'Failed to load attendance log.'
    });
  }

  onSubmitCheckIn(): void {
    if (!this.memberIdInput || !this.memberIdInput.trim()) {
      return;
    }
    this.loading = true;
    this.errorMessage = '';
    this.successMessage = '';

    this.checkInService.scanCheckIn(this.memberIdInput.trim()).subscribe({
      next: data => {
        this.loading = false;
        this.successMessage = `Attendance logged successfully for ${data.memberName}!`;
        this.memberIdInput = '';
        this.loadTodayCheckIns();
      },
      error: err => {
        this.loading = false;
        this.errorMessage = err.error || 'Failed to check in member. Check if membership is expired or ID is invalid.';
      }
    });
  }

  toggleCamera(): void {
    if (this.isCameraActive) {
      this.stopCamera();
    } else {
      this.startCamera();
    }
  }

  startCamera(): void {
    this.isCameraActive = true;
    this.errorMessage = '';
    this.successMessage = '';

    // Wait briefly for Angular lifecycle to render #reader element
    setTimeout(() => {
      try {
        this.html5QrCode = new Html5Qrcode("reader");
        this.html5QrCode.start(
          { facingMode: "environment" },
          {
            fps: 10,
            qrbox: (width: number, height: number) => {
              const size = Math.min(width, height) * 0.7;
              return { width: size, height: size };
            }
          },
          (decodedText: string) => {
            this.handleCameraScan(decodedText);
          },
          (error: string) => {
            // Ignore format parsing frames errors
          }
        ).catch((err: any) => {
          console.error("Webcam startup failed: ", err);
          this.errorMessage = "Failed to access webcam. Make sure camera permissions are granted.";
          this.isCameraActive = false;
        });
      } catch (e) {
        console.error("Scanner init error: ", e);
        this.errorMessage = "Webcam scanner initialization failed.";
        this.isCameraActive = false;
      }
    }, 100);
  }

  stopCamera(): void {
    if (this.html5QrCode) {
      this.html5QrCode.stop().then(() => {
        this.isCameraActive = false;
        this.html5QrCode = null;
      }).catch((err: any) => {
        console.error("Webcam teardown failed: ", err);
        this.isCameraActive = false;
        this.html5QrCode = null;
      });
    } else {
      this.isCameraActive = false;
    }
  }

  handleCameraScan(code: string): void {
    this.stopCamera();
    this.loading = true;
    this.errorMessage = '';
    this.successMessage = '';

    this.checkInService.scanCheckIn(code.trim()).subscribe({
      next: data => {
        this.loading = false;
        this.successMessage = `Attendance logged successfully for ${data.memberName}! (Scanned)`;
        this.loadTodayCheckIns();
      },
      error: err => {
        this.loading = false;
        this.errorMessage = err.error || 'Failed to check in scanned member. Check if membership is active.';
      }
    });
  }
}
