import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class CheckInService {
  private apiUrl = 'http://localhost:8080/api/checkin';

  constructor(private http: HttpClient) {}

  scanCheckIn(memberId: number): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/scan`, { memberId });
  }

  getTodayCheckIns(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/today`);
  }
}
