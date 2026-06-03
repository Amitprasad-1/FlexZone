import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class EShopService {
  private apiUrl = 'http://localhost:8080/api/shop';

  constructor(private http: HttpClient) {}

  getProducts(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/products`);
  }

  getPlans(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/plans`);
  }

  checkout(paymentRequest: any): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/payment`, paymentRequest);
  }

  getMyOrders(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/orders`);
  }
}
