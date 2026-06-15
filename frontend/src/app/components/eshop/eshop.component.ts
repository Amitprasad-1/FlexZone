import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { EShopService } from '../../services/eshop.service';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-eshop',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './eshop.component.html'
})
export class EShopComponent implements OnInit {
  products: any[] = [];
  plans: any[] = [];
  cart: any[] = [];
  
  // Checkout flow state
  selectedPlan: any = null; // populated if renewing membership
  paymentType: 'MEMBERSHIP_RENEWAL' | 'PRODUCT_PURCHASE' = 'PRODUCT_PURCHASE';
  paymentMethod: 'RAZORPAY' | 'PAYTM' | 'CARD' | 'UPI' = 'RAZORPAY';
  
  showCheckoutModal = false;
  checkoutProgress = 0;
  checkoutStatus: 'IDLE' | 'PROCESSING' | 'SUCCESS' | 'FAILED' = 'IDLE';

  // Extended simulation state fields
  checkoutStep: 'SELECT' | 'UPI' | 'CARD' | 'OTP' = 'CARD';
  activeTab: 'CARD' | 'UPI' | 'RAZORPAY' | 'PAYTM' = 'CARD';
  cardForm = {
    holderName: '',
    cardNumber: '',
    expiry: '',
    cvv: ''
  };
  cardError = '';
  otpCode = '';
  otpError = '';
  otpTimer = 60;
  otpInterval: any = null;
  upiTimer = 45;
  upiInterval: any = null;
  selectedUpiApp = '';

  loading = true;
  errorMessage = '';
  successMessage = '';

  constructor(
    private eshopService: EShopService,
    public authService: AuthService
  ) {}

  ngOnInit(): void {
    this.loadCatalog();
  }

  loadCatalog(): void {
    this.loading = true;
    this.eshopService.getProducts().subscribe({
      next: data => {
        this.products = data;
        this.checkLoadingComplete();
      },
      error: err => {
        this.errorMessage = 'Failed to load shop catalog.';
        this.loading = false;
      }
    });

    this.eshopService.getPlans().subscribe({
      next: data => {
        this.plans = data;
        this.checkLoadingComplete();
      },
      error: err => {
        this.errorMessage = 'Failed to load membership plans.';
        this.loading = false;
      }
    });
  }

  private loadCount = 0;
  private checkLoadingComplete(): void {
    this.loadCount++;
    if (this.loadCount >= 2) {
      this.loading = false;
      this.loadCount = 0;
    }
  }

  // --- Cart Management ---
  addToCart(product: any): void {
    this.errorMessage = '';
    const existing = this.cart.find(item => item.id === product.id);
    if (existing) {
      if (existing.quantity >= product.stock) {
        this.errorMessage = `Cannot add more. Only ${product.stock} items left in stock.`;
        return;
      }
      existing.quantity++;
    } else {
      if (product.stock <= 0) {
        this.errorMessage = 'Out of stock!';
        return;
      }
      this.cart.push({
        id: product.id,
        name: product.name,
        price: product.price,
        quantity: 1,
        maxStock: product.stock
      });
    }
  }

  updateQuantity(item: any, amount: number): void {
    item.quantity += amount;
    if (item.quantity <= 0) {
      this.removeFromCart(item.id);
    } else if (item.quantity > item.maxStock) {
      item.quantity = item.maxStock;
      this.errorMessage = `Only ${item.maxStock} items available in stock.`;
    }
  }

  removeFromCart(productId: number): void {
    this.cart = this.cart.filter(item => item.id !== productId);
  }

  getCartTotal(): number {
    return this.cart.reduce((sum, item) => sum + (item.price * item.quantity), 0);
  }

  // --- Checkout Flows ---
  openProductCheckout(): void {
    if (this.cart.length === 0) {
      return;
    }
    this.paymentType = 'PRODUCT_PURCHASE';
    this.selectedPlan = null;
    this.showCheckoutModal = true;
    this.checkoutStatus = 'IDLE';
    this.checkoutProgress = 0;
    this.errorMessage = '';
    this.cleanupTimers();
  }

  openPlanRenewal(plan: any): void {
    this.paymentType = 'MEMBERSHIP_RENEWAL';
    this.selectedPlan = plan;
    this.showCheckoutModal = true;
    this.checkoutStatus = 'IDLE';
    this.checkoutProgress = 0;
    this.errorMessage = '';
    this.cleanupTimers();
  }

  getCheckoutAmount(): number {
    if (this.paymentType === 'MEMBERSHIP_RENEWAL' && this.selectedPlan) {
      return this.selectedPlan.price;
    }
    return this.getCartTotal();
  }

  selectMethod(method: 'RAZORPAY' | 'PAYTM' | 'CARD' | 'UPI'): void {
    this.paymentMethod = (method === 'CARD' || method === 'UPI') ? 'RAZORPAY' : method;
    if (method === 'UPI') {
      this.checkoutStep = 'UPI';
      this.startUpiTimer();
    } else if (method === 'CARD') {
      this.checkoutStep = 'CARD';
      this.cardError = '';
    } else {
      this.processMockPayment();
    }
  }

  // UPI Simulation
  getUpiQrUrl(): string {
    const amount = this.getCheckoutAmount();
    const upiLink = `upi://pay?pa=flexzone@ybl&pn=FlexZone%20Gym%20Checkout&am=${amount.toFixed(2)}&cu=INR`;
    return `https://api.qrserver.com/v1/create-qr-code/?size=180x180&color=251-191-36&bgcolor=3-7-18&data=${encodeURIComponent(upiLink)}`;
  }

  startUpiTimer(): void {
    this.upiTimer = 45;
    if (this.upiInterval) clearInterval(this.upiInterval);
    this.upiInterval = setInterval(() => {
      this.upiTimer--;
      if (this.upiTimer <= 0) {
        clearInterval(this.upiInterval);
      }
    }, 1000);
  }

  simulateUpiRedirect(app: string): void {
    this.selectedUpiApp = app;
    this.checkoutStatus = 'PROCESSING';
    this.checkoutProgress = 10;
    
    let count = 0;
    const interval = setInterval(() => {
      count += 20;
      this.checkoutProgress = count;
      if (count >= 100) {
        clearInterval(interval);
        setTimeout(() => {
          this.submitPaymentToBackend();
        }, 800);
      }
    }, 300);
  }

  // Card Payment validation & OTP flow
  validateCardAndPay(): void {
    this.cardError = '';
    const name = this.cardForm.holderName.trim();
    const num = this.cardForm.cardNumber.replace(/\s+/g, '');
    const expiry = this.cardForm.expiry.trim();
    const cvv = this.cardForm.cvv.trim();

    if (!name) {
      this.cardError = 'Cardholder name is required.';
      return;
    }
    if (num.length !== 16 || !/^\d+$/.test(num)) {
      this.cardError = 'Invalid card number. Must be 16 digits.';
      return;
    }
    if (!/^\d{2}\/\d{2}$/.test(expiry)) {
      this.cardError = 'Invalid expiry format. Use MM/YY.';
      return;
    }
    const [mm, yy] = expiry.split('/').map(Number);
    if (mm < 1 || mm > 12) {
      this.cardError = 'Invalid expiry month.';
      return;
    }
    if (cvv.length !== 3 || !/^\d+$/.test(cvv)) {
      this.cardError = 'Invalid CVV. Must be 3 digits.';
      return;
    }

    // Advance to simulated 3DS OTP step
    this.checkoutStep = 'OTP';
    this.otpCode = '';
    this.otpError = '';
    this.startOtpTimer();
  }

  startOtpTimer(): void {
    this.otpTimer = 60;
    if (this.otpInterval) clearInterval(this.otpInterval);
    this.otpInterval = setInterval(() => {
      this.otpTimer--;
      if (this.otpTimer <= 0) {
        clearInterval(this.otpInterval);
      }
    }, 1000);
  }

  resendOtp(): void {
    this.otpError = '';
    this.otpCode = '';
    this.startOtpTimer();
  }

  verifyOtpAndPay(): void {
    this.otpError = '';
    if (this.otpCode.length !== 6 || !/^\d+$/.test(this.otpCode)) {
      this.otpError = 'OTP must be a 6-digit number.';
      return;
    }

    this.checkoutStatus = 'PROCESSING';
    this.checkoutProgress = 10;

    const interval = setInterval(() => {
      if (this.checkoutProgress < 90) {
        this.checkoutProgress += 30;
      }
    }, 250);

    setTimeout(() => {
      clearInterval(interval);
      this.checkoutProgress = 100;
      this.submitPaymentToBackend();
    }, 1200);
  }

  processMockPayment(): void {
    this.checkoutStatus = 'PROCESSING';
    this.checkoutProgress = 10;
    
    const interval = setInterval(() => {
      if (this.checkoutProgress < 90) {
        this.checkoutProgress += 20;
      }
    }, 400);

    setTimeout(() => {
      clearInterval(interval);
      this.checkoutProgress = 100;
      this.submitPaymentToBackend();
    }, 2000);
  }

  private submitPaymentToBackend(): void {
    const payload: any = {
      amount: this.getCheckoutAmount(),
      paymentType: this.paymentType,
      paymentMethod: this.paymentMethod
    };

    if (this.paymentType === 'MEMBERSHIP_RENEWAL' && this.selectedPlan) {
      payload.planId = this.selectedPlan.id;
    } else {
      payload.cartItems = this.cart.map(item => ({
        productId: item.id,
        quantity: item.quantity
      }));
    }

    this.eshopService.checkout(payload).subscribe({
      next: () => {
        this.checkoutStatus = 'SUCCESS';
        this.cart = [];
        this.loadCatalog(); // reload product stocks
        this.cleanupTimers();
      },
      error: err => {
        this.checkoutStatus = 'FAILED';
        this.errorMessage = 'Checkout transaction failed: ' + (err.error || err.message);
        this.cleanupTimers();
      }
    });
  }

  cleanupTimers(): void {
    if (this.otpInterval) clearInterval(this.otpInterval);
    if (this.upiInterval) clearInterval(this.upiInterval);
    this.cardForm = { holderName: '', cardNumber: '', expiry: '', cvv: '' };
    this.otpCode = '';
    this.checkoutStep = 'CARD';
    this.activeTab = 'CARD';
    this.selectedUpiApp = '';
  }

  setTab(tab: 'CARD' | 'UPI' | 'RAZORPAY' | 'PAYTM'): void {
    if (this.checkoutStep === 'OTP') {
      return;
    }
    this.activeTab = tab;
    if (this.upiInterval) clearInterval(this.upiInterval);
    this.upiTimer = 45;
    this.selectedUpiApp = '';
    this.cardError = '';
    this.otpError = '';
    
    this.paymentMethod = (tab === 'CARD' || tab === 'UPI') ? 'RAZORPAY' : tab;
    if (tab === 'UPI') {
      this.checkoutStep = 'UPI';
      this.startUpiTimer();
    } else if (tab === 'CARD') {
      this.checkoutStep = 'CARD';
    } else {
      this.checkoutStep = 'SELECT';
    }
  }

  formatCardNumber(): void {
    let value = this.cardForm.cardNumber.replace(/\D/g, '');
    value = value.substring(0, 16);
    const chunks = value.match(/.{1,4}/g);
    this.cardForm.cardNumber = chunks ? chunks.join(' ') : value;
  }

  formatExpiry(): void {
    let value = this.cardForm.expiry.replace(/\D/g, '');
    value = value.substring(0, 4);
    if (value.length > 2) {
      this.cardForm.expiry = value.substring(0, 2) + '/' + value.substring(2);
    } else {
      this.cardForm.expiry = value;
    }
  }

  formatCvv(): void {
    this.cardForm.cvv = this.cardForm.cvv.replace(/\D/g, '').substring(0, 3);
  }

  getCardType(): string {
    const num = this.cardForm.cardNumber.replace(/\s+/g, '');
    if (!num) return 'unknown';
    if (num.startsWith('4')) return 'visa';
    if (/^5[1-5]/.test(num)) return 'mastercard';
    if (/^(60|65|81|82|508)/.test(num)) return 'rupay';
    if (/^(5018|5020|5038|6304|6759|6761|6763)/.test(num)) return 'maestro';
    return 'unknown';
  }

  closeCheckoutModal(): void {
    this.showCheckoutModal = false;
    this.checkoutStatus = 'IDLE';
    this.cleanupTimers();
  }
}
