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
  paymentMethod: 'RAZORPAY' | 'PAYTM' = 'RAZORPAY';
  
  showCheckoutModal = false;
  checkoutProgress = 0;
  checkoutStatus: 'IDLE' | 'PROCESSING' | 'SUCCESS' | 'FAILED' = 'IDLE';

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
  }

  openPlanRenewal(plan: any): void {
    this.paymentType = 'MEMBERSHIP_RENEWAL';
    this.selectedPlan = plan;
    this.showCheckoutModal = true;
    this.checkoutStatus = 'IDLE';
    this.checkoutProgress = 0;
    this.errorMessage = '';
  }

  getCheckoutAmount(): number {
    if (this.paymentType === 'MEMBERSHIP_RENEWAL' && this.selectedPlan) {
      return this.selectedPlan.price;
    }
    return this.getCartTotal();
  }

  processMockPayment(): void {
    this.checkoutStatus = 'PROCESSING';
    this.checkoutProgress = 10;
    
    // Animate progress bar to simulate gateway connection
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
      },
      error: err => {
        this.checkoutStatus = 'FAILED';
        this.errorMessage = 'Checkout transaction failed: ' + (err.error || err.message);
      }
    });
  }

  closeCheckoutModal(): void {
    this.showCheckoutModal = false;
    this.checkoutStatus = 'IDLE';
  }
}
