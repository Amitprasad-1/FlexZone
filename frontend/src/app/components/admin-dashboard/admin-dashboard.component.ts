import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AdminService } from '../../services/admin.service';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './admin-dashboard.component.html'
})
export class AdminDashboardComponent implements OnInit {
  activeTab: 'analytics' | 'members' | 'trainers' | 'plans' = 'analytics';
  analytics: any = null;
  loadingAnalytics = true;
  maxMonthlyRevenue = 20000;

  // Members Management
  members: any[] = [];
  selectedMember: any = null;
  showMemberModal = false;

  // Trainers Management
  trainers: any[] = [];
  newTrainer = { username: '', password: '', email: '', fullName: '', specialization: '', experienceYears: 0, bioText: '', photoUrl: 'trainer1.png' };
  selectedTrainer: any = null;
  showTrainerModal = false;
  showTrainerAddModal = false;

  // Plans Management
  plans: any[] = [];
  newPlan = { name: '', description: '', price: 0, durationDays: 30 };
  selectedPlan: any = null;
  showPlanModal = false;

  errorMessage = '';
  successMessage = '';

  constructor(
    private adminService: AdminService,
    public authService: AuthService
  ) {}

  ngOnInit(): void {
    this.loadAnalytics();
    this.loadMembers();
    this.loadTrainers();
    this.loadPlans();
  }

  // Reload utilities
  clearAlerts(): void {
    this.errorMessage = '';
    this.successMessage = '';
  }

  loadAnalytics(): void {
    this.loadingAnalytics = true;
    this.adminService.getAnalytics().subscribe({
      next: data => {
        this.analytics = data;
        this.loadingAnalytics = false;
        
        // Find max monthly revenue to scale the chart dynamically
        if (data && data.monthlyRevenue) {
          const values = Object.values(data.monthlyRevenue) as number[];
          const max = Math.max(...values);
          this.maxMonthlyRevenue = max > 0 ? max : 20000;
        }
      },
      error: err => {
        this.errorMessage = 'Failed to load analytics: ' + (err.error || err.message);
        this.loadingAnalytics = false;
      }
    });
  }

  loadMembers(): void {
    this.adminService.getMembers().subscribe({
      next: data => this.members = data,
      error: err => this.errorMessage = 'Failed to load members: ' + (err.error || err.message)
    });
  }

  loadTrainers(): void {
    this.adminService.getTrainers().subscribe({
      next: data => this.trainers = data,
      error: err => this.errorMessage = 'Failed to load trainers: ' + (err.error || err.message)
    });
  }

  loadPlans(): void {
    this.adminService.getPlans().subscribe({
      next: data => this.plans = data,
      error: err => this.errorMessage = 'Failed to load plans: ' + (err.error || err.message)
    });
  }

  // --- Members CRUD ---
  openMemberEdit(member: any): void {
    this.selectedMember = { ...member };
    this.showMemberModal = true;
    this.clearAlerts();
  }

  saveMember(): void {
    this.adminService.updateMember(this.selectedMember.id, this.selectedMember).subscribe({
      next: () => {
        this.successMessage = 'Member updated successfully!';
        this.showMemberModal = false;
        this.loadMembers();
        this.loadAnalytics();
      },
      error: err => this.errorMessage = 'Update failed: ' + (err.error || err.message)
    });
  }

  deleteMember(id: number): void {
    if (confirm('Are you sure you want to delete this member profile?')) {
      this.adminService.deleteMember(id).subscribe({
        next: () => {
          this.successMessage = 'Member deleted successfully!';
          this.loadMembers();
          this.loadAnalytics();
        },
        error: err => this.errorMessage = 'Deletion failed: ' + (err.error || err.message)
      });
    }
  }

  // --- Trainers CRUD ---
  openTrainerEdit(trainer: any): void {
    const parts = trainer.bio ? trainer.bio.split('||') : [];
    this.selectedTrainer = { 
      ...trainer, 
      bioText: parts[0] || '', 
      photoUrl: parts[1] || '' 
    };
    this.showTrainerModal = true;
    this.clearAlerts();
  }

  saveTrainer(): void {
    this.selectedTrainer.bio = `${this.selectedTrainer.bioText || ''}||${this.selectedTrainer.photoUrl || ''}`;
    this.adminService.updateTrainer(this.selectedTrainer.id, this.selectedTrainer).subscribe({
      next: () => {
        this.successMessage = 'Trainer updated successfully!';
        this.showTrainerModal = false;
        this.loadTrainers();
        this.loadAnalytics();
      },
      error: err => this.errorMessage = 'Update failed: ' + (err.error || err.message)
    });
  }

  openTrainerAdd(): void {
    this.newTrainer = { username: '', password: '', email: '', fullName: '', specialization: '', experienceYears: 0, bioText: '', photoUrl: 'trainer1.png' };
    this.showTrainerAddModal = true;
    this.clearAlerts();
  }

  addTrainer(): void {
    const payload = { 
      ...this.newTrainer, 
      role: 'TRAINER',
      bio: `${this.newTrainer.bioText || ''}||${this.newTrainer.photoUrl || ''}`
    };
    this.authService.signup(payload).subscribe({
      next: () => {
        this.successMessage = 'Trainer created successfully!';
        this.showTrainerAddModal = false;
        this.loadTrainers();
        this.loadAnalytics();
      },
      error: err => this.errorMessage = 'Creation failed: ' + (err.error || err.message)
    });
  }

  getTrainerPhoto(trainer: any): string {
    if (!trainer.bio) return 'trainer1.png';
    const parts = trainer.bio.split('||');
    return parts[1] || 'trainer1.png';
  }

  getTrainerBioText(trainer: any): string {
    if (!trainer.bio) return '';
    const parts = trainer.bio.split('||');
    return parts[0] || '';
  }

  deleteTrainer(id: number): void {
    if (confirm('Are you sure you want to delete this trainer?')) {
      this.adminService.deleteTrainer(id).subscribe({
        next: () => {
          this.successMessage = 'Trainer profile deleted!';
          this.loadTrainers();
          this.loadAnalytics();
        },
        error: err => this.errorMessage = 'Deletion failed: ' + (err.error || err.message)
      });
    }
  }

  // --- Plans CRUD ---
  openPlanEdit(plan: any): void {
    this.selectedPlan = { ...plan };
    this.showPlanModal = true;
    this.clearAlerts();
  }

  savePlan(): void {
    this.adminService.updatePlan(this.selectedPlan.id, this.selectedPlan).subscribe({
      next: () => {
        this.successMessage = 'Membership plan updated!';
        this.showPlanModal = false;
        this.loadPlans();
      },
      error: err => this.errorMessage = 'Update failed: ' + (err.error || err.message)
    });
  }

  createPlan(): void {
    this.adminService.createPlan(this.newPlan).subscribe({
      next: () => {
        this.successMessage = 'New membership plan created!';
        this.newPlan = { name: '', description: '', price: 0, durationDays: 30 };
        this.loadPlans();
      },
      error: err => this.errorMessage = 'Creation failed: ' + (err.error || err.message)
    });
  }

  deletePlan(id: number): void {
    if (confirm('Are you sure you want to delete this plan?')) {
      this.adminService.deletePlan(id).subscribe({
        next: () => {
          this.successMessage = 'Membership plan deleted!';
          this.loadPlans();
        },
        error: err => this.errorMessage = 'Deletion failed: ' + (err.error || err.message)
      });
    }
  }

  // Helper getters for templates
  getMonthlyKeys(): string[] {
    return this.analytics && this.analytics.monthlyRevenue ? Object.keys(this.analytics.monthlyRevenue) : [];
  }

  getOccupancyKeys(): string[] {
    return this.analytics && this.analytics.slotOccupancyRates ? Object.keys(this.analytics.slotOccupancyRates) : [];
  }
}
