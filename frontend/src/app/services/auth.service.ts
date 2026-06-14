import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable } from 'rxjs';
import { map } from 'rxjs/operators';

import { API_BASE_URL } from '../api-config';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private apiUrl = `${API_BASE_URL}/api/auth`;
  private currentUserSubject = new BehaviorSubject<any>(null);
  public currentUser$ = this.currentUserSubject.asObservable();

  constructor(private http: HttpClient) {
    const cachedUser = localStorage.getItem('currentUser');
    if (cachedUser) {
      this.currentUserSubject.next(JSON.parse(cachedUser));
    }
  }

  public get currentUserValue(): any {
    return this.currentUserSubject.value;
  }

  login(credentials: any): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/login`, credentials).pipe(
      map(user => {
        if (user && user.token) {
          localStorage.setItem('currentUser', JSON.stringify(user));
          this.currentUserSubject.next(user);
        }
        return user;
      })
    );
  }

  signup(user: any): Observable<string> {
    return this.http.post(`${this.apiUrl}/signup`, user, { responseType: 'text' });
  }

  logout(): void {
    localStorage.removeItem('currentUser');
    this.currentUserSubject.next(null);
  }

  getToken(): string {
    const user = this.currentUserValue;
    return user ? user.token : '';
  }

  isLoggedIn(): boolean {
    return !!this.currentUserValue;
  }

  getRole(): string {
    const user = this.currentUserValue;
    return user ? user.role : '';
  }

  getUserName(): string {
    const user = this.currentUserValue;
    return user ? user.username : '';
  }

  getFullName(): string {
    const user = this.currentUserValue;
    return user ? user.username : ''; // fallback
  }

  getProfilePicture(): string {
    const user = this.currentUserValue;
    return user ? user.profilePicture : '';
  }

  forgotPassword(email: string): Observable<string> {
    return this.http.post(`${this.apiUrl}/forgot-password`, { email }, { responseType: 'text' });
  }

  resetPassword(email: string, newPassword: string): Observable<string> {
    return this.http.post(`${this.apiUrl}/reset-password`, { email, newPassword }, { responseType: 'text' });
  }
}
