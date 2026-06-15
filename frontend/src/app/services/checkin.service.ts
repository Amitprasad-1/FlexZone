import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { API_BASE_URL } from '../api-config';

@Injectable({
  providedIn: 'root'
})
export class CheckInService {
  private apiUrl = `${API_BASE_URL}/api/checkin`;

  constructor(private http: HttpClient) {}

  scanCheckIn(memberId: string): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/scan`, { memberId });
  }

  getTodayCheckIns(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/today`);
  }
}
