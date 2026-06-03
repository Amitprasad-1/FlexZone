import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { BookingService } from '../../services/booking.service';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-booking',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './booking.component.html'
})
export class BookingComponent implements OnInit {
  slots: any[] = [];
  classes: any[] = [];
  myBookings: any[] = [];
  selectedDate: string;

  loading = true;
  errorMessage = '';
  successMessage = '';

  constructor(
    private bookingService: BookingService,
    public authService: AuthService
  ) {
    this.selectedDate = new Date().toISOString().split('T')[0];
  }

  ngOnInit(): void {
    this.loadData();
  }

  clearAlerts(): void {
    this.errorMessage = '';
    this.successMessage = '';
  }

  loadData(): void {
    this.loading = true;
    this.bookingService.getTimeSlots().subscribe({
      next: data => {
        this.slots = data;
        this.checkLoadingComplete();
      },
      error: err => {
        this.errorMessage = 'Failed to load time slots.';
        this.loading = false;
      }
    });

    this.bookingService.getClassSchedules().subscribe({
      next: data => {
        this.classes = data;
        this.checkLoadingComplete();
      },
      error: err => {
        this.errorMessage = 'Failed to load class schedules.';
        this.loading = false;
      }
    });

    if (this.authService.isLoggedIn() && this.authService.getRole() === 'MEMBER') {
      this.bookingService.getMyBookings().subscribe({
        next: data => {
          this.myBookings = data;
          this.checkLoadingComplete();
        },
        error: err => {
          this.errorMessage = 'Failed to load bookings log.';
          this.loading = false;
        }
      });
    } else {
      this.myBookings = [];
      this.loading = false;
    }
  }

  private loadFinishedCount = 0;
  private checkLoadingComplete(): void {
    this.loadFinishedCount++;
    const expected = (this.authService.isLoggedIn() && this.authService.getRole() === 'MEMBER') ? 3 : 2;
    if (this.loadFinishedCount >= expected) {
      this.loading = false;
      this.loadFinishedCount = 0;
    }
  }

  bookSlot(slotId: number): void {
    this.clearAlerts();
    this.bookingService.createBooking('SLOT', slotId, this.selectedDate).subscribe({
      next: () => {
        this.successMessage = 'Hourly time slot booked successfully!';
        this.loadData();
      },
      error: err => {
        this.errorMessage = err.error || 'Failed to book slot. You may have already booked a slot for this date or capacity is full.';
      }
    });
  }

  bookClass(classId: number): void {
    this.clearAlerts();
    this.bookingService.createBooking('CLASS', classId).subscribe({
      next: () => {
        this.successMessage = 'Zumba/Yoga class session booked successfully!';
        this.loadData();
      },
      error: err => {
        this.errorMessage = err.error || 'Failed to book class. Already registered or capacity is full.';
      }
    });
  }

  cancelBooking(bookingId: number): void {
    if (confirm('Are you sure you want to cancel this booking?')) {
      this.clearAlerts();
      this.bookingService.cancelBooking(bookingId).subscribe({
        next: () => {
          this.successMessage = 'Booking cancelled successfully.';
          this.loadData();
        },
        error: err => {
          this.errorMessage = 'Failed to cancel booking: ' + (err.error || err.message);
        }
      });
    }
  }

  isSlotBooked(slotId: number): boolean {
    return this.myBookings.some(b => b.bookingType === 'SLOT' && b.timeSlotId === slotId && b.bookingDate === this.selectedDate);
  }

  isClassBooked(classId: number): boolean {
    return this.myBookings.some(b => b.bookingType === 'CLASS' && b.classScheduleId === classId);
  }
}
