import { Component, ElementRef, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-signup',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './signup.component.html'
})
export class SignupComponent {
  @ViewChild('cropCanvas') canvasRef!: ElementRef<HTMLCanvasElement>;

  showCropper = false;
  zoomScale = 1.0;
  posX = 0;
  posY = 0;
  sourceImage: HTMLImageElement | null = null;
  isDragging = false;
  startX = 0;
  startY = 0;
  user = {
    username: '',
    password: '',
    email: '',
    fullName: '',
    role: 'MEMBER',
    specialization: '',
    experienceYears: 0,
    profilePicture: ''
  };
  successMessage = '';
  errorMessage = '';
  loading = false;
  showPassword = false;

  constructor(private authService: AuthService, private router: Router) {
    if (this.authService.isLoggedIn()) {
      this.router.navigate(['/']);
    }
  }

  onSubmit(): void {
    this.loading = true;
    this.errorMessage = '';
    this.successMessage = '';

    const payload: any = { ...this.user };
    if (payload.role !== 'TRAINER') {
      delete payload.specialization;
      delete payload.experienceYears;
    }

    this.authService.signup(payload).subscribe({
      next: () => {
        this.loading = false;
        this.successMessage = 'Registration successful! Redirecting to login...';
        setTimeout(() => {
          this.router.navigate(['/login']);
        }, 2000);
      },
      error: err => {
        this.loading = false;
        if (err.error && typeof err.error === 'object' && err.error.message) {
          this.errorMessage = err.error.message;
        } else if (typeof err.error === 'string') {
          this.errorMessage = err.error;
        } else {
          this.errorMessage = err.message || 'Registration failed. Please check if username/email already exists.';
        }
      }
    });
  }

  onFileSelected(event: any): void {
    const file = event.target.files[0];
    if (file) {
      if (file.size > 5 * 1024 * 1024) {
        this.errorMessage = 'File is too large. Max size is 5MB.';
        return;
      }
      this.errorMessage = '';
      const reader = new FileReader();
      reader.onload = (e: any) => {
        const img = new Image();
        img.onload = () => {
          this.sourceImage = img;
          this.resetCropper();
          this.showCropper = true;
          setTimeout(() => this.initCanvas(), 100);
        };
        img.src = e.target.result;
      };
      reader.readAsDataURL(file);
    }
  }

  resetCropper(): void {
    this.zoomScale = 1.0;
    this.posX = 0;
    this.posY = 0;
    this.isDragging = false;
  }

  initCanvas(): void {
    if (!this.canvasRef) return;
    const canvas = this.canvasRef.nativeElement;
    canvas.width = 320;
    canvas.height = 320;
    this.redraw();
  }

  redraw(): void {
    if (!this.sourceImage || !this.canvasRef) return;
    const canvas = this.canvasRef.nativeElement;
    const ctx = canvas.getContext('2d');
    if (!ctx) return;

    ctx.clearRect(0, 0, canvas.width, canvas.height);
    ctx.save();

    const centerX = canvas.width / 2;
    const centerY = canvas.height / 2;

    ctx.translate(centerX + this.posX, centerY + this.posY);
    ctx.scale(this.zoomScale, this.zoomScale);

    const imgW = this.sourceImage.width;
    const imgH = this.sourceImage.height;
    
    const maxDim = Math.max(imgW, imgH);
    const displayScale = canvas.width / maxDim;
    const dWidth = imgW * displayScale;
    const dHeight = imgH * displayScale;

    ctx.drawImage(this.sourceImage, -dWidth / 2, -dHeight / 2, dWidth, dHeight);
    ctx.restore();
  }

  startDrag(event: MouseEvent | TouchEvent): void {
    event.preventDefault();
    this.isDragging = true;
    const clientX = 'touches' in event ? event.touches[0].clientX : event.clientX;
    const clientY = 'touches' in event ? event.touches[0].clientY : event.clientY;
    this.startX = clientX - this.posX;
    this.startY = clientY - this.posY;
  }

  drag(event: MouseEvent | TouchEvent): void {
    if (!this.isDragging) return;
    event.preventDefault();
    const clientX = 'touches' in event ? event.touches[0].clientX : event.clientX;
    const clientY = 'touches' in event ? event.touches[0].clientY : event.clientY;
    this.posX = clientX - this.startX;
    this.posY = clientY - this.startY;
    this.redraw();
  }

  endDrag(): void {
    this.isDragging = false;
  }

  closeCropper(): void {
    this.showCropper = false;
    this.sourceImage = null;
  }

  applyCrop(): void {
    if (!this.canvasRef) return;
    const canvas = this.canvasRef.nativeElement;

    const tempCanvas = document.createElement('canvas');
    tempCanvas.width = 200;
    tempCanvas.height = 200;
    const tempCtx = tempCanvas.getContext('2d');
    if (!tempCtx) return;

    const cropSize = canvas.width - 64;

    tempCtx.drawImage(
      canvas,
      32, 32, cropSize, cropSize,
      0, 0, 200, 200
    );

    this.user.profilePicture = tempCanvas.toDataURL('image/jpeg', 0.85);
    this.showCropper = false;
  }
}
