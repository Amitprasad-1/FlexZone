import { Injectable, NgZone } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, firstValueFrom } from 'rxjs';

export interface OfflineRequest {
  url: string;
  method: string;
  body: any;
  headers?: any;
  timestamp: number;
  description?: string;
}

@Injectable({
  providedIn: 'root'
})
export class OfflineSyncService {
  private isOfflineSubject = new BehaviorSubject<boolean>(
    typeof navigator !== 'undefined' ? !navigator.onLine : false
  );
  public isOffline$ = this.isOfflineSubject.asObservable();

  private pendingSyncCountSubject = new BehaviorSubject<number>(0);
  public pendingSyncCount$ = this.pendingSyncCountSubject.asObservable();

  private syncSuccessSubject = new BehaviorSubject<boolean>(false);
  public syncSuccess$ = this.syncSuccessSubject.asObservable();

  private syncQueueKey = 'offline_pending_sync';
  private isSyncing = false;

  constructor(
    private http: HttpClient,
    private ngZone: NgZone
  ) {
    if (typeof window !== 'undefined') {
      window.addEventListener('online', () => this.updateOnlineStatus(true));
      window.addEventListener('offline', () => this.updateOnlineStatus(false));
      this.updatePendingCount();

      // Trigger initial sync if online and items exist
      if (navigator.onLine) {
        this.triggerSync();
      }
    }
  }

  private updateOnlineStatus(online: boolean): void {
    this.ngZone.run(() => {
      this.isOfflineSubject.next(!online);
      if (online) {
        this.triggerSync();
      }
    });
  }

  public getOfflineStatus(): boolean {
    return this.isOfflineSubject.value;
  }

  // Add request to offline sync queue
  public queueRequest(url: string, method: string, body: any): void {
    const queue = this.getQueue();
    
    // Add descriptive action name for status banner
    let description = 'Action logged';
    if (url.includes('/bmi/log')) description = 'BMI Log Entry';
    else if (url.includes('/bookings')) description = 'Class Slot Booking';
    else if (url.includes('/checkin/scan')) description = 'Member Check-In';
    else if (url.includes('/shop/payment')) description = 'E-Shop Purchase Order';

    const req: OfflineRequest = {
      url,
      method,
      body,
      timestamp: Date.now(),
      description
    };

    queue.push(req);
    this.saveQueue(queue);
    this.updatePendingCount();
  }

  public getQueue(): OfflineRequest[] {
    if (typeof localStorage === 'undefined') return [];
    const stored = localStorage.getItem(this.syncQueueKey);
    return stored ? JSON.parse(stored) : [];
  }

  private saveQueue(queue: OfflineRequest[]): void {
    if (typeof localStorage === 'undefined') return;
    localStorage.setItem(this.syncQueueKey, JSON.stringify(queue));
  }

  private updatePendingCount(): void {
    this.pendingSyncCountSubject.next(this.getQueue().length);
  }

  // Synchronize queued requests with backend
  public async triggerSync(): Promise<void> {
    if (this.isSyncing) return;
    const queue = this.getQueue();
    if (queue.length === 0) return;

    this.isSyncing = true;
    console.log(`Starting synchronization of ${queue.length} offline actions...`);

    const remainingQueue: OfflineRequest[] = [];
    let hasError = false;

    for (const req of queue) {
      if (hasError) {
        remainingQueue.push(req);
        continue;
      }

      try {
        // Run HTTP request sequentially
        await firstValueFrom(
          this.http.request(req.method, req.url, { body: req.body })
        );
        console.log(`Synced successfully: ${req.method} ${req.url}`);
      } catch (err) {
        console.error(`Failed to sync request: ${req.method} ${req.url}`, err);
        // Put back in queue to retry later
        remainingQueue.push(req);
        hasError = true;
      }
    }

    this.saveQueue(remainingQueue);
    this.updatePendingCount();
    this.isSyncing = false;

    if (!hasError && queue.length > 0) {
      this.syncSuccessSubject.next(true);
      setTimeout(() => {
        this.syncSuccessSubject.next(false);
      }, 5000);
    }
  }
}
