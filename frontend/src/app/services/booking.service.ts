import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { API_BASE_URL } from '../api-config';

@Injectable({
  providedIn: 'root'
})
export class BookingService {
  private apiUrl = `${API_BASE_URL}/api/bookings`;

  constructor(private http: HttpClient) {}

  getTimeSlots(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/slots`);
  }

  getClassSchedules(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/classes`);
  }

  getMyBookings(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}`);
  }

  createBooking(bookingType: string, targetId: number, bookingDate?: string): Observable<any> {
    const payload: any = { bookingType, targetId };
    if (bookingDate) {
      payload.bookingDate = bookingDate;
    }
    return this.http.post<any>(this.apiUrl, payload);
  }

  cancelBooking(id: number): Observable<string> {
    return this.http.delete(`${this.apiUrl}/${id}`, { responseType: 'text' });
  }
}
