import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class AdminService {
  private apiUrl = 'http://localhost:8080/api/admin';

  constructor(private http: HttpClient) {}

  getAnalytics(): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/analytics`);
  }

  // Trainers CRUD
  getTrainers(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/trainers`);
  }

  getTrainer(id: number): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/trainers/${id}`);
  }

  updateTrainer(id: number, trainer: any): Observable<any> {
    return this.http.put<any>(`${this.apiUrl}/trainers/${id}`, trainer);
  }

  deleteTrainer(id: number): Observable<string> {
    return this.http.delete(`${this.apiUrl}/trainers/${id}`, { responseType: 'text' });
  }

  // Members CRUD
  getMembers(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/members`);
  }

  getMember(id: number): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/members/${id}`);
  }

  updateMember(id: number, member: any): Observable<any> {
    return this.http.put<any>(`${this.apiUrl}/members/${id}`, member);
  }

  deleteMember(id: number): Observable<string> {
    return this.http.delete(`${this.apiUrl}/members/${id}`, { responseType: 'text' });
  }

  // Plans CRUD
  getPlans(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/plans`);
  }

  createPlan(plan: any): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/plans`, plan);
  }

  updatePlan(id: number, plan: any): Observable<any> {
    return this.http.put<any>(`${this.apiUrl}/plans/${id}`, plan);
  }

  deletePlan(id: number): Observable<string> {
    return this.http.delete(`${this.apiUrl}/plans/${id}`, { responseType: 'text' });
  }
}
