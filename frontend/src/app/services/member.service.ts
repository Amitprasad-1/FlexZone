import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { API_BASE_URL } from '../api-config';

@Injectable({
  providedIn: 'root'
})
export class MemberService {
  private apiUrl = `${API_BASE_URL}/api/member`;

  constructor(private http: HttpClient) {}

  getProfile(): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/profile`);
  }

  getBmiHistory(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/bmi/history`);
  }

  logBmi(heightCm: number, weightKg: number): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/bmi/log`, { heightCm, weightKg });
  }
}
