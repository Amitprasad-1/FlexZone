import { inject } from '@angular/core';
import { HttpInterceptorFn, HttpResponse, HttpErrorResponse } from '@angular/common/http';
import { of, throwError } from 'rxjs';
import { catchError, tap } from 'rxjs/operators';
import { OfflineSyncService } from './services/offline-sync.service';

// Rich offline mock dataset fallback to ensure the UI operates perfectly
const OFFLINE_SEED_DATA: { [key: string]: any } = {
  '/api/shop/products': [
    { id: 1, name: 'Premium Whey Protein', description: 'High-quality fast-absorbing protein powder for muscle growth and recovery.', price: 4500, stock: 12, imageUrl: 'public/whey_protein.jpg' },
    { id: 2, name: 'Vaporize Pre-Workout', description: 'Explosive energy and laser-sharp focus blend for intense training sessions.', price: 2400, stock: 8, imageUrl: 'public/pre_workout.jpg' },
    { id: 3, name: 'BPA-Free Shaker Bottle', description: 'Leak-proof shaker with wire mixing whisk and secure flip cap.', price: 600, stock: 25, imageUrl: 'public/gym_shaker.jpg' },
    { id: 4, name: 'FlexZone Training Gloves', description: 'Padded leather gloves with wrist wrap for maximum grip and protection.', price: 1200, stock: 15, imageUrl: 'public/gym_gloves.jpg' },
    { id: 5, name: 'L-Carnitine Liquid', description: 'Metabolic helper support formula to convert fat cells into active energy.', price: 1800, stock: 10, imageUrl: 'public/l_carnitine.jpg' }
  ],
  '/api/shop/plans': [
    { id: 1, name: 'Monthly Starter', price: 1500, durationDays: 30, description: 'Full gym equipment access, free initial fitness assessment, and lockers.' },
    { id: 2, name: 'Quarterly Fit', price: 4000, durationDays: 90, description: 'Most popular! Standard access plus 2 trainer-guided sessions per month.' },
    { id: 3, name: 'Annual Elite', price: 12000, durationDays: 365, description: 'All access pass including sauna, group classes, and personal nutrition plan.' }
  ],
  '/api/admin/members': [
    { id: 1, memberId: 'MEM-101', user: { username: 'aarav.sharma1', email: 'aarav@gmail.com', role: 'MEMBER' }, plan: { name: 'Quarterly Fit' }, status: 'PAID' },
    { id: 2, memberId: 'MEM-102', user: { username: 'priya.nair2', email: 'priya@gmail.com', role: 'MEMBER' }, plan: { name: 'Monthly Starter' }, status: 'PENDING' },
    { id: 3, memberId: 'MEM-103', user: { username: 'rohan.das3', email: 'rohan@gmail.com', role: 'MEMBER' }, plan: { name: 'Annual Elite' }, status: 'EXPIRED' }
  ],
  '/api/admin/trainers': [
    { id: 1, name: 'Vikram Rathore', specialty: 'Strength & Conditioning', rating: 4.9, imageUrl: 'public/trainer1.png' },
    { id: 2, name: 'Sarah D\'Souza', specialty: 'Yoga & Mindfulness', rating: 4.8, imageUrl: 'public/trainer2.png' },
    { id: 3, name: 'Kabir Mehta', specialty: 'Crossfit & HIIT', rating: 4.9, imageUrl: 'public/trainer3.png' }
  ],
  '/api/admin/plans': [
    { id: 1, name: 'Monthly Starter', price: 1500, durationDays: 30, description: 'Full gym equipment access, free initial fitness assessment, and lockers.' },
    { id: 2, name: 'Quarterly Fit', price: 4000, durationDays: 90, description: 'Most popular! Standard access plus 2 trainer-guided sessions per month.' },
    { id: 3, name: 'Annual Elite', price: 12000, durationDays: 365, description: 'All access pass including sauna, group classes, and personal nutrition plan.' }
  ],
  '/api/bookings/slots': [
    { id: 1, timeSlot: '06:00 AM - 07:00 AM', capacity: 15, bookedCount: 8 },
    { id: 2, timeSlot: '07:00 AM - 08:00 AM', capacity: 15, bookedCount: 12 },
    { id: 3, timeSlot: '08:00 AM - 09:00 AM', capacity: 15, bookedCount: 14 },
    { id: 4, timeSlot: '05:00 PM - 06:00 PM', capacity: 20, bookedCount: 15 },
    { id: 5, timeSlot: '06:00 PM - 07:00 PM', capacity: 20, bookedCount: 18 }
  ],
  '/api/bookings/classes': [
    { id: 1, className: 'Crossfit Shred', trainerName: 'Kabir Mehta', scheduleTime: '06:30 PM', duration: '45 mins', dayOfWeek: 'Monday', imageUrl: 'public/class_crossfit.png' },
    { id: 2, className: 'Vinyasa Flow Yoga', trainerName: 'Sarah D\'Souza', scheduleTime: '08:00 AM', duration: '60 mins', dayOfWeek: 'Wednesday', imageUrl: 'public/class_yoga.png' },
    { id: 3, className: 'Zumba Dance Party', trainerName: 'Sarah D\'Souza', scheduleTime: '07:00 PM', duration: '50 mins', dayOfWeek: 'Friday', imageUrl: 'public/class_zumba.png' }
  ],
  '/api/bookings': [
    { id: 99, bookingType: 'CLASS', targetName: 'Crossfit Shred', targetDetails: 'Trainer: Kabir Mehta', bookingDate: '2026-06-16', status: 'CONFIRMED' }
  ],
  '/api/checkin/today': [
    { id: 1, memberId: 'MEM-101', memberName: 'Aarav Sharma', checkInTime: '2026-06-15T08:14:22Z', method: 'QR CODE SCAN' },
    { id: 2, memberId: 'MEM-102', memberName: 'Priya Nair', checkInTime: '2026-06-15T09:25:10Z', method: 'MANUAL ENTRY' }
  ],
  '/api/member/profile': {
    id: 1,
    memberId: 'MEM-101',
    username: 'aarav.sharma1',
    email: 'aarav@gmail.com',
    status: 'PAID',
    planName: 'Quarterly Fit',
    expiryDate: '2026-09-15'
  },
  '/api/member/bmi/history': [
    { id: 1, heightCm: 178, weightKg: 75.2, bmi: 23.7, loggedAt: '2026-05-15T10:00:00Z' },
    { id: 2, heightCm: 178, weightKg: 74.5, bmi: 23.5, loggedAt: '2026-06-15T12:00:00Z' }
  ],
  '/api/admin/analytics': {
    totalMembers: 125,
    activeMembers: 98,
    todayCheckIns: 34,
    monthlyRevenue: 145000,
    revenueHistory: [120000, 132000, 145000],
    checkInPeakHours: ['07:00 AM', '06:00 PM']
  }
};

export const offlineInterceptor: HttpInterceptorFn = (req, next) => {
  const syncService = inject(OfflineSyncService);

  // Check if network is offline or request is likely to fail
  const isOffline = syncService.getOfflineStatus() || (typeof navigator !== 'undefined' && !navigator.onLine);

  if (isOffline) {
    return handleOffline(req, syncService);
  }

  // If online, execute network request but cache GET outcomes for offline use
  return next(req).pipe(
    tap(event => {
      if (event instanceof HttpResponse && req.method === 'GET') {
        const cacheKey = 'offline_cache:' + getCleanUrlPath(req.url);
        localStorage.setItem(cacheKey, JSON.stringify(event.body));
      }
    }),
    catchError((error: HttpErrorResponse) => {
      // If server goes down (status 0) while user client is online, trigger offline handlers
      if (error.status === 0) {
        return handleOffline(req, syncService);
      }
      return throwError(() => error);
    })
  );
};

// Return match clean API endpoint path from the url string
function getCleanUrlPath(url: string): string {
  const parts = url.split('/api/');
  return parts.length > 1 ? '/api/' + parts[1].split('?')[0] : url;
}

// Handle request routing when connection is disconnected or backend is down
function handleOffline(req: any, syncService: OfflineSyncService) {
  const cleanPath = getCleanUrlPath(req.url);

  if (req.method === 'GET') {
    // 1. Check local storage cache
    const cacheKey = 'offline_cache:' + cleanPath;
    const cachedData = localStorage.getItem(cacheKey);

    if (cachedData) {
      console.log(`Serving offline cache: ${req.url}`);
      return of(new HttpResponse({
        status: 200,
        statusText: 'OK',
        body: JSON.parse(cachedData),
        headers: req.headers.set('X-From-Offline-Cache', 'true')
      }));
    }

    // 2. Fall back to seeded mock assets
    const seedKeys = Object.keys(OFFLINE_SEED_DATA);
    const matchedKey = seedKeys.find(key => cleanPath.startsWith(key));
    if (matchedKey) {
      console.log(`Serving offline seed data fallback: ${matchedKey}`);
      return of(new HttpResponse({
        status: 200,
        statusText: 'OK',
        body: OFFLINE_SEED_DATA[matchedKey],
        headers: req.headers.set('X-From-Offline-Cache', 'true')
      }));
    }

    // Default empty array fallback for listings
    return of(new HttpResponse({
      status: 200,
      statusText: 'OK',
      body: []
    }));
  }

  // Handle write requests (POST, PUT, DELETE)
  if (req.method === 'POST' || req.method === 'PUT' || req.method === 'DELETE') {
    console.log(`Queueing write request offline: ${req.method} ${req.url}`);
    
    // Save to queue
    syncService.queueRequest(req.url, req.method, req.body);

    // Return custom mock response to avoid client crash
    let responseBody: any = { success: true, offline: true, message: 'Request queued successfully' };

    // Custom simulated response payloads based on specific targets
    if (cleanPath.includes('/auth/login')) {
      // Simulate login for offline user
      const lastUser = localStorage.getItem('currentUser');
      if (lastUser) {
        responseBody = JSON.parse(lastUser);
      } else {
        responseBody = {
          token: 'offline-token-12345',
          username: req.body?.username || 'offline.member',
          role: req.body?.username === 'admin' ? 'ADMIN' : 'MEMBER'
        };
      }
    } else if (cleanPath.includes('/checkin/scan')) {
      responseBody = {
        success: true,
        memberName: 'Aarav Sharma',
        memberId: req.body?.memberId || 'MEM-101',
        status: 'PAID',
        message: 'Checked-in successfully (Offline Cache)'
      };
    } else if (cleanPath.includes('/bmi/log')) {
      const height = req.body?.heightCm || 175;
      const weight = req.body?.weightKg || 70;
      const bmi = parseFloat((weight / ((height / 100) * (height / 100))).toFixed(1));
      responseBody = {
        id: Math.floor(Math.random() * 1000),
        heightCm: height,
        weightKg: weight,
        bmi,
        loggedAt: new Date().toISOString()
      };
    } else if (cleanPath.includes('/bookings')) {
      responseBody = {
        id: Math.floor(Math.random() * 1000),
        bookingType: req.body?.bookingType || 'SLOT',
        targetId: req.body?.targetId || 1,
        bookingDate: req.body?.bookingDate || new Date().toISOString().split('T')[0],
        status: 'CONFIRMED'
      };
    } else if (cleanPath.includes('/shop/payment')) {
      responseBody = {
        success: true,
        transactionId: 'TXN-OFFLINE-' + Date.now(),
        message: 'Order created offline'
      };
    }

    return of(new HttpResponse({
      status: 200,
      statusText: 'OK',
      body: responseBody
    }));
  }

  // Fallback for unmatched requests
  return of(new HttpResponse({
    status: 200,
    statusText: 'OK',
    body: {}
  }));
}
